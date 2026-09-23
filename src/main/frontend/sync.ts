import {SyncService} from './generated/endpoints';
import {pending,snapshot,acceptSync} from './data';
import type {SyncResponse} from './types';

let running:Promise<string[]>|undefined;
export async function synchronize():Promise<string[]> {
 if(running)return running;
 const work=async()=>{
  const sent=(await pending()).slice(0,100);const state=await snapshot();
  // Pull from zero when replaying edits: rejected/conflicted optimistic rows need their authoritative version.
  const response=await SyncService.sync({cursor:sent.length?0:state.cursor,mutations:sent,knownProjectIds:state.projects.map(p=>p.id)}) as SyncResponse;
  const outcomes=await acceptSync(response,sent);
  return outcomes.filter(o=>o.status!=='APPLIED').map(o=>o.message);
 };
 // A browser-wide lock prevents duplicate multi-tab replay; the server also deduplicates operation IDs.
 running=(navigator.locks?navigator.locks.request('todoapp-sync',work):work()).finally(()=>{running=undefined;});
 return running;
}
