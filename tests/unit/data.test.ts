import 'fake-indexeddb/auto';
import {beforeEach,describe,it,expect} from 'vitest';
import {enqueue,pending,snapshot,acceptSync,clearLocal,rejected} from '../../src/main/frontend/data';
import {emptySnapshot,newTask,type SyncResponse,emptyFilters} from '../../src/main/frontend/types';
import {matches} from '../../src/main/frontend/filters';
const project={id:'p',name:'Personal',color:'#6655aa',inbox:true,archived:false,role:'OWNER',version:0};
const response=(extra:Partial<SyncResponse>={}):SyncResponse=>({...emptySnapshot,projects:[project],outcomes:[],removedTaskIds:[],...extra});
beforeEach(async()=>{await clearLocal();});
describe('durable offline queue',()=>{
 it('preserves later in-flight edits for review after an earlier conflict',async()=>{const t={...newTask('p','UTC','Original'),version:0};await enqueue({...t,title:'First offline edit'});const sent=await pending();await enqueue({...t,version:1,title:'Later offline edit'});await acceptSync(response({tasks:[{...t,version:1,title:'Server edit'}],outcomes:[{operationId:sent[0].operationId,status:'CONFLICT',message:'Conflict copy saved',taskId:'copy'}]}),sent);expect((await snapshot()).tasks[0].title).toBe('Server edit');expect(await pending()).toEqual([]);expect((await rejected())[0].mutation.task.title).toBe('Later offline edit');});
 it('keeps earlier comments while a new comment is queued offline',async()=>{const t={...newTask('p','UTC','Discuss'),version:0,comments:[{id:'first',authorId:'u',author:'User',text:'Existing',createdAt:''}]};await acceptSync(response({tasks:[t]}),[]);await enqueue({...t,comments:[{id:'second',authorId:'u',author:'User',text:'Offline reply',createdAt:''}]},'COMMENT');expect((await snapshot()).tasks[0].comments.map(c=>c.text)).toEqual(['Existing','Offline reply']);await acceptSync(response({tasks:[t]}),[]);expect((await snapshot()).tasks[0].comments).toHaveLength(2);});
 it('persists edits and expected versions atomically',async()=>{const t=newTask('p','UTC','Write');await enqueue(t);expect((await pending())[0].baseVersion).toBe(-1);const saved=(await snapshot()).tasks[0];expect(saved.version).toBe(0);await enqueue({...saved,title:'Write more'});expect((await pending())[1].baseVersion).toBe(0);expect((await snapshot()).tasks[0].title).toBe('Write more');});
 it('does not overwrite a newer edit queued during sync',async()=>{const t=newTask('p','UTC','First');await enqueue(t);const sent=await pending();const local=(await snapshot()).tasks[0];await enqueue({...local,title:'Second'});await acceptSync(response({tasks:[{...t,version:0}],outcomes:[{operationId:sent[0].operationId,status:'APPLIED',message:'',taskId:t.id}]}),sent);expect((await snapshot()).tasks[0].title).toBe('Second');expect((await pending()).length).toBe(1);});
 it('keeps rejected work available for export and purges revoked project',async()=>{const t=newTask('p','UTC','Keep my work');await enqueue(t);const sent=await pending();await acceptSync(response({projects:[],outcomes:[{operationId:sent[0].operationId,status:'REJECTED',message:'Access removed',taskId:t.id}]}),sent);expect((await snapshot()).tasks.length).toBe(0);expect((await rejected())[0].mutation.task.title).toBe('Keep my work');expect(await pending()).toEqual([]);});
 it('clears data and pending work on logout',async()=>{await enqueue(newTask('p','UTC','Private'));await clearLocal();expect((await snapshot()).tasks).toEqual([]);expect(await pending()).toEqual([]);});
});
describe('structured filters',()=>{
 it('combines search, tag, date, and status',()=>{const t={...newTask('p','UTC','Write report'),tags:['work'],dueDate:'2026-10-01'};expect(matches(t,{...emptyFilters,search:'report',tag:'work',dueFrom:'2026-09-30',status:'OPEN'})).toBe(true);expect(matches(t,{...emptyFilters,dueTo:'2026-09-30'})).toBe(false);});
 it('does not match undated tasks against date ranges',()=>{expect(matches(newTask('p','UTC'),{...emptyFilters,dueTo:'2026-10-01'})).toBe(false);});
});
