import {type Task,type Filters,type Project,type User,today} from './types';

export function matches(task:Task,f:Filters):boolean {
 const haystack=`${task.title} ${task.description} ${task.tags.join(' ')}`.toLowerCase();
 return (!f.search||f.search.toLowerCase().split(/\s+/).every(word=>haystack.includes(word))) &&
 (!f.status||task.status===f.status)&&(!f.priority||task.priority===f.priority)&&(!f.tag||task.tags.includes(f.tag))&&
 (!f.assignee||task.assigneeId===(f.assignee==='UNASSIGNED'?'':f.assignee))&&(!f.projectId||task.projectId===f.projectId)&&
 (!f.dueFrom||!!task.dueDate&&task.dueDate>=f.dueFrom)&&(!f.dueTo||!!task.dueDate&&task.dueDate<=f.dueTo)&&
 (!f.recurring||(task.recurrence.frequency!=='NONE')===(f.recurring==='YES'))&&
 (!f.completedFrom||!!task.completedAt&&task.completedAt.slice(0,10)>=f.completedFrom)&&(!f.completedTo||!!task.completedAt&&task.completedAt.slice(0,10)<=f.completedTo);
}
export function viewTasks(tasks:Task[],projects:Project[],user:User,view:string,f:Filters):Task[] {
 const date=today(user.timezone),projectMap=new Map(projects.map(p=>[p.id,p]));
 return tasks.filter(t=>{
  const p=projectMap.get(t.projectId);if(!p)return false;
  if(view==='trash')return !!t.deletedAt&&matches(t,f);
  if(t.deletedAt||p.archived&&view!==p.id)return false;
  const isDone=t.status==='DONE';
  let visible=true;
  switch(view){
   case 'inbox':visible=p.inbox;break;
   case 'myday':visible=t.myDay===date;break;
   case 'today':visible=t.dueDate===date&&!isDone;break;
   case 'upcoming':visible=t.dueDate>date&&!isDone;break;
   case 'overdue':visible=!!t.dueDate&&t.dueDate<date&&!isDone;break;
   case 'assigned':visible=t.assigneeId===user.id&&!isDone;break;
   case 'shared':visible=!p.inbox&&projects.some(project=>project.id===p.id);break;
   case 'completed':visible=isDone;break;
   case 'all':break;
   case 'saved':break;
   default:visible=t.projectId===view;
  }
  return visible&&matches(t,f);
 }).sort((a,b)=>Number(a.status==='DONE')-Number(b.status==='DONE')||a.position-b.position||a.id.localeCompare(b.id));
}
