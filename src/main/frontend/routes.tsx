import {RouterConfigurationBuilder} from '@vaadin/hilla-file-router/runtime.js';
import App from './App';
export const {router,routes}=new RouterConfigurationBuilder().withReactRoutes([
 {path:'/',element:<App/>},
 {path:'/login',element:<App/>}
]).build();
