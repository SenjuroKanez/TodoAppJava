/// <reference lib="webworker" />
import {precacheAndRoute} from 'workbox-precaching';
import {registerRoute,NavigationRoute} from 'workbox-routing';
import {NetworkFirst} from 'workbox-strategies';
declare const self:ServiceWorkerGlobalScope & {__WB_MANIFEST:Array<{url:string;revision:string|null}>};
precacheAndRoute(self.__WB_MANIFEST.filter(entry=>!['.','/','index.html','sw.js'].includes(entry.url)));
// Vaadin's manifest contains assets, not its dynamically served HTML shell.
// Prefer a fresh shell and session metadata online; reuse the last shell offline.
registerRoute(new NavigationRoute(new NetworkFirst({cacheName:'todoapp-navigation',networkTimeoutSeconds:3}),{denylist:[/^\/auth\//,/^\/connect\//,/^\/actuator\//,/^\/logout/]}));
self.addEventListener('activate',event=>event.waitUntil(self.clients.claim()));
self.addEventListener('message',event=>{if(event.data?.type==='SKIP_WAITING')self.skipWaiting();});
self.addEventListener('push',event=>{
 const message=event.data?.json()??{};
 event.waitUntil(self.registration.showNotification(message.title??'TodoApp',{body:message.body??'You have a workspace update.',icon:'/icons/app.svg',badge:'/icons/app.svg',data:{url:'/'}}));
});
self.addEventListener('notificationclick',event=>{
 event.notification.close();event.waitUntil((async()=>{const windows=await self.clients.matchAll({type:'window',includeUncontrolled:true});for(const client of windows){if('focus'in client)return client.focus();}return self.clients.openWindow('/');})());
});
// Foreground sync is universal; supporting browsers can additionally wake an open client.
self.addEventListener('sync',((event:ExtendableEvent & {tag:string})=>{if(event.tag==='todoapp-sync')event.waitUntil((async()=>{const windows=await self.clients.matchAll({type:'window',includeUncontrolled:true});for(const client of windows)client.postMessage({type:'SYNC'});})());}) as EventListener);
