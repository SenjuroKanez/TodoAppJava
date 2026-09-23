import {createRoot} from 'react-dom/client';
import {RouterProvider} from 'react-router/dom';
import {router} from './routes';
const root=document.getElementById('outlet');
if(root)createRoot(root).render(<RouterProvider router={router}/>);
if('serviceWorker' in navigator)navigator.serviceWorker.register('/sw.js').catch(()=>{/* Offline before the first installation: the workspace explains its cached-data limits. */});
