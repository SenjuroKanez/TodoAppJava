export type User = {id:string; username:string; email:string; theme:string; timezone:string};
export type Project = {id:string;name:string;color:string;archived:boolean;inbox:boolean;role:string;version:number};
export type Member = {projectId:string;userId:string;username:string;role:string};
export type Invitation = {id:string;projectId:string;projectName:string;role:string};
export type Recurrence = {frequency:string;interval:number;weekdays:number[];endDate:string;remaining:number};
export type Task = {
 id:string;projectId:string;title:string;description:string;status:string;priority:string;assigneeId:string;tags:string[];
 dueDate:string;dueTime:string;timezone:string;estimateMinutes:number;myDay:string;recurrence:Recurrence;reminders:string[];
 steps:{id:string;title:string;done:boolean}[];comments:{id:string;authorId:string;author:string;text:string;createdAt:string}[];
 activity:{id:string;actor:string;action:string;createdAt:string}[];position:number;version:number;createdAt:string;updatedAt:string;
 completedAt:string;deletedAt:string;conflictOf:string;
};
export type Mutation = {operationId:string;kind:string;baseVersion:number;task:Task};
export type Outcome = {operationId:string;status:string;message:string;taskId:string};
export type Notice = {id:string;projectId:string;taskId:string;message:string;createdAt:string;read:boolean};
export type SavedFilter={id:string;name:string;query:string};
export type Snapshot={cursor:number;tasks:Task[];projects:Project[];members:Member[];invitations:Invitation[];notifications:Notice[];filters:SavedFilter[]};
export type SyncResponse=Snapshot & {removedTaskIds:string[];outcomes:Outcome[]};
export type Filters={search:string;status:string;priority:string;tag:string;assignee:string;dueFrom:string;dueTo:string;recurring:string;completedFrom:string;completedTo:string;projectId:string};
export const emptyFilters:Filters={search:'',status:'',priority:'',tag:'',assignee:'',dueFrom:'',dueTo:'',recurring:'',completedFrom:'',completedTo:'',projectId:''};
export const emptySnapshot:Snapshot={cursor:0,tasks:[],projects:[],members:[],invitations:[],notifications:[],filters:[]};
export function newTask(projectId:string,timezone:string,title=''):Task {
 return {id:crypto.randomUUID(),projectId,title,description:'',status:'OPEN',priority:'MEDIUM',assigneeId:'',tags:[],dueDate:'',dueTime:'',timezone,
 estimateMinutes:0,myDay:'',recurrence:{frequency:'NONE',interval:1,weekdays:[],endDate:'',remaining:0},reminders:[],steps:[],comments:[],activity:[],
 position:Date.now(),version:-1,createdAt:'',updatedAt:'',completedAt:'',deletedAt:'',conflictOf:''};
}
export function today(zone:string):string {return new Intl.DateTimeFormat('en-CA',{timeZone:zone,year:'numeric',month:'2-digit',day:'2-digit'}).format(new Date());}
