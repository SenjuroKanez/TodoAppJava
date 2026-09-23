import {openDB, type DBSchema} from 'idb';
import {emptySnapshot, type User,type Snapshot,type Mutation,type Outcome,type Task,type SyncResponse} from './types';

interface Store extends DBSchema {
 state:{key:string;value:Snapshot|User|string};
 outbox:{key:string;value:Mutation & {sequence:number}};
 rejected:{key:string;value:{mutation:Mutation;message:string}};
}
const db=()=>openDB<Store>('todoapp-v2',1,{upgrade(d){d.createObjectStore('state');d.createObjectStore('outbox',{keyPath:'operationId'});d.createObjectStore('rejected',{keyPath:'mutation.operationId'});}});
export async function currentLocalUser(){return (await (await db()).get('state','user')) as User|undefined;}
export async function setLocalUser(user:User){const d=await db();const old=await currentLocalUser();if(old&&old.id!==user.id)await clearLocal();await d.put('state',user,'user');}
export async function snapshot():Promise<Snapshot>{return (await (await db()).get('state','snapshot') as Snapshot|undefined)??structuredClone(emptySnapshot);}
export async function pending(){return (await (await db()).getAll('outbox')).sort((a,b)=>a.sequence-b.sequence);}
export async function rejected(){return (await (await db()).getAll('rejected'));}
export async function dismissRejected(id:string){await (await db()).delete('rejected',id);}
export async function clearLocal(){const d=await db();const tx=d.transaction(['state','outbox','rejected'],'readwrite');await Promise.all([tx.objectStore('state').clear(),tx.objectStore('outbox').clear(),tx.objectStore('rejected').clear()]);await tx.done;}
/** Snapshot and outbox change atomically, so a refresh cannot lose a queued edit. */
export async function enqueue(task:Task,kind='SAVE') {
 const d=await db();const tx=d.transaction(['state','outbox'],'readwrite');
 const state=(await tx.objectStore('state').get('snapshot') as Snapshot|undefined)??structuredClone(emptySnapshot);
 const entries=await tx.objectStore('outbox').getAll();
 const sequence=entries.reduce((max,e)=>Math.max(max,e.sequence),0)+1;
 const operationId=crypto.randomUUID();
 await tx.objectStore('outbox').put({operationId,kind,baseVersion:task.version,task:structuredClone(task),sequence});
 const previous=state.tasks.find(t=>t.id===task.id);
 const optimistic={...task,comments:kind==='COMMENT'?[...(previous?.comments??[]),...task.comments.slice(-1)]:task.comments,version:task.version+1,deletedAt:kind==='DELETE'?new Date().toISOString():kind==='RESTORE'?'':task.deletedAt};
 state.tasks=[...state.tasks.filter(t=>t.id!==task.id),optimistic];
 await tx.objectStore('state').put(state,'snapshot');await tx.done;return operationId;
}
/** Apply deltas without overwriting edits queued while the request was in flight. */
export async function acceptSync(response:SyncResponse,sent:Mutation[]):Promise<Outcome[]> {
 const d=await db();const tx=d.transaction(['state','outbox','rejected'],'readwrite');
 const previous=(await tx.objectStore('state').get('snapshot') as Snapshot|undefined)??structuredClone(emptySnapshot);
 const allowed=new Set(response.projects.map(p=>p.id));
 const tasks=new Map(previous.tasks.filter(t=>allowed.has(t.projectId)&&!response.removedTaskIds.includes(t.id)).map(t=>[t.id,t]));
 for(const outcome of response.outcomes){
  await tx.objectStore('outbox').delete(outcome.operationId);
  if(outcome.status!=='APPLIED'){
   const mutation=sent.find(m=>m.operationId===outcome.operationId);
   if(mutation){tasks.delete(mutation.task.id);if(outcome.status==='REJECTED')await tx.objectStore('rejected').put({mutation,message:outcome.message});}
  }
 }
 for(const task of response.tasks)tasks.set(task.id,task);
 const remaining=(await tx.objectStore('outbox').getAll()).sort((a,b)=>a.sequence-b.sequence);
 const blocked=new Set(response.outcomes.filter(o=>o.status!=='APPLIED').map(o=>sent.find(m=>m.operationId===o.operationId)?.task.id));
 for(const m of remaining){
  if(blocked.has(m.task.id)){
   await tx.objectStore('rejected').put({mutation:m,message:'An earlier edit conflicted. This later edit was preserved instead of overwriting the server.'});await tx.objectStore('outbox').delete(m.operationId);continue;
  }
  if(!allowed.has(m.task.projectId)){
   await tx.objectStore('rejected').put({mutation:m,message:'Access to this project was removed. Export your unsynced edit before dismissing it.'});await tx.objectStore('outbox').delete(m.operationId);continue;
  }
  const previous=tasks.get(m.task.id);
  tasks.set(m.task.id,{...m.task,comments:m.kind==='COMMENT'?[...(previous?.comments??[]),...m.task.comments.slice(-1)]:m.task.comments,version:m.baseVersion+1,deletedAt:m.kind==='DELETE'?new Date().toISOString():m.kind==='RESTORE'?'':m.task.deletedAt});
 }
 await tx.objectStore('state').put({...response,tasks:[...tasks.values()]},'snapshot');await tx.done;
 return response.outcomes;
}
