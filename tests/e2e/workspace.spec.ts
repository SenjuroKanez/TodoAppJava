import {test,expect,type Page} from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

const password='A-long-test-password-2026';
async function account(page:Page,name='person'){
 const username=name+'_'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);
 const csrf=await (await page.request.get('/auth/csrf')).json();
 const reg=await page.request.post('/auth/register',{headers:{[csrf.headerName]:csrf.token},data:{username,email:username+'@example.test',password,timezone:'UTC'}});
 expect(reg.status()).toBe(201);
 const login=await page.request.post('/login',{form:{username,password,[csrf.parameterName]:csrf.token}});expect(login.ok()).toBeTruthy();
 await page.goto('/');await expect(page.getByRole('heading',{name:'My Day',exact:true})).toBeVisible();
 await expect(page.getByText('All changes saved',{exact:true})).toBeVisible();
 // Wait until initial project metadata is persisted, not merely an empty initial render.
 await expect.poll(()=>page.evaluate(async()=>{const request=indexedDB.open('todoapp-v2');return new Promise<number>(resolve=>{request.onsuccess=()=>{const db=request.result;const read=db.transaction('state').objectStore('state').get('snapshot');read.onsuccess=()=>{resolve(read.result?.projects?.length??0);db.close();};};});})).toBeGreaterThan(0);
 return username;
}
test('creates an account through the sign-up form',async({page})=>{
 await page.goto('/login');await page.getByRole('button',{name:'Create an account',exact:true}).click();
 const username='signup_'+Date.now().toString(36);
 await page.getByLabel('Username',{exact:true}).fill(username);await page.getByLabel('Email',{exact:true}).fill(username+'@example.test');await page.getByLabel('Password',{exact:true}).fill(password);
 await page.getByRole('button',{name:'Create workspace'}).click();await expect(page.getByRole('heading',{name:'My Day',exact:true})).toBeVisible();
});
test('registration, daily planning, edit, completion, trash, and restore',async({page})=>{
 await account(page);
 await page.getByLabel('Quick add task').fill('Prepare the weekly review');await page.getByLabel('Quick add task').press('Enter');
 await expect(page.getByText('Prepare the weekly review',{exact:true})).toBeVisible();
 await page.getByRole('button',{name:'Edit Prepare the weekly review',exact:true}).click();
 await page.getByLabel('Description',{exact:true}).fill('Review progress and choose the next small step.');
 await page.getByLabel('New step').fill('Collect notes');await page.getByLabel('New step').press('Enter');
 await page.getByRole('button',{name:'Save task',exact:true}).click();
 await expect(page.getByRole('dialog')).not.toBeVisible();
 await page.getByRole('button',{name:'Complete Prepare the weekly review',exact:true}).click();
 await expect(page.getByRole('button',{name:'Reopen Prepare the weekly review',exact:true})).toBeVisible();
 await page.getByRole('button',{name:'Edit Prepare the weekly review',exact:true}).click();
 await page.getByRole('button',{name:'Move to Trash',exact:true}).click();
 await page.getByRole('button',{name:'Trash',exact:true}).click();
 await expect(page.getByText('Prepare the weekly review',{exact:true})).toBeVisible();
 await page.getByRole('button',{name:'Restore',exact:true}).click();
 await expect(page.getByText('Prepare the weekly review',{exact:true})).not.toBeVisible();
});
test('opens offline, retains an edit over reload, and syncs it once',async({page,context})=>{
 await account(page,'offline');
 await page.evaluate(async()=>{await navigator.serviceWorker.ready;});
 await page.reload();await expect(page.getByLabel('Quick add task')).toBeVisible();
 await context.setOffline(true);
 await page.getByLabel('Quick add task').fill('Written offline');await page.getByLabel('Quick add task').press('Enter');
 await expect(page.getByText('Written offline',{exact:true})).toBeVisible();
 await page.reload();await expect(page.getByText('Written offline',{exact:true})).toBeVisible();
 await context.setOffline(false);await page.getByRole('button',{name:/pending|Offline|Syncing|All changes saved/}).click();
 await expect(page.getByText('All changes saved',{exact:true})).toBeVisible();
 await page.reload();await expect(page.getByText('Written offline',{exact:true})).toHaveCount(1);
});
test('project invitations grant read-only access and owner can promote editor',async({page,browser})=>{
 await account(page,'owner');const otherContext=await browser.newContext({baseURL:process.env.TEST_BASE_URL??'http://localhost:8080'});const other=await otherContext.newPage();const collaborator=await account(other,'viewer');
 await page.getByRole('button',{name:'New project',exact:true}).click();await page.getByLabel('Project name').fill('Shared launch');await page.getByRole('button',{name:'Create project',exact:true}).click();
 await expect(page.getByRole('heading',{name:'Shared launch',exact:true})).toBeVisible();
 await page.getByLabel('Quick add task').fill('Shared task');await page.getByLabel('Quick add task').press('Enter');
 await page.getByRole('button',{name:'Share & manage'}).click();await page.getByLabel('Exact username or email').fill(collaborator);await page.getByLabel('Invite role').selectOption('VIEWER');await page.getByRole('button',{name:'Send invitation'}).click();
 await other.reload();await other.getByRole('button',{name:'Notifications',exact:true}).click();await expect(other.getByText('You’re invited to Shared launch')).toBeVisible();await other.getByRole('button',{name:'Accept',exact:true}).click();await other.getByRole('button',{name:'Close dialog',exact:true}).click();
 await other.getByRole('navigation',{name:'Projects',exact:true}).getByRole('button',{name:/Shared launch/}).click();await expect(other.getByRole('button',{name:'Complete Shared task',exact:true})).toBeDisabled();
 await page.getByRole('button',{name:'Close dialog',exact:true}).click();await page.reload();await page.getByRole('navigation',{name:'Projects',exact:true}).getByRole('button',{name:/Shared launch/}).click();await page.getByRole('button',{name:'Share & manage'}).click();await page.getByLabel(`Role for ${collaborator}`).selectOption('EDITOR');
 await other.reload();await other.getByRole('navigation',{name:'Projects',exact:true}).getByRole('button',{name:/Shared launch/}).click();await expect(other.getByRole('button',{name:'Complete Shared task',exact:true})).toBeEnabled();await otherContext.close();
});
test('search, calendar, keyboard capture, and mobile fit',async({page})=>{
 await account(page,'mobile');await page.keyboard.press('n');await expect(page.getByLabel('Quick add task')).toBeFocused();await page.keyboard.type('Read design notes');await page.keyboard.press('Enter');await expect(page.getByText('Read design notes',{exact:true})).toBeVisible();
 await page.getByLabel('Search tasks').fill('notfound');await expect(page.getByText('No tasks match this view.')).toBeVisible();await page.getByLabel('Search tasks').fill('');
 await page.getByRole('button',{name:'Calendar',exact:true}).click();await expect(page.locator('.calendar-grid')).toBeVisible();
 await page.setViewportSize({width:390,height:844});await expect.poll(()=>page.evaluate(()=>document.documentElement.scrollWidth<=window.innerWidth)).toBe(true);await page.getByRole('button',{name:'Open navigation'}).click();await page.getByRole('navigation',{name:'Task views'}).getByRole('button',{name:/^Inbox/}).click();await expect(page.getByRole('heading',{name:'Inbox',exact:true})).toBeVisible();
});
test('rejects missing CSRF tokens and uses HttpOnly session cookies',async({page})=>{
 const invalid=await page.request.post('/auth/register',{data:{username:'blocked',email:'blocked@example.test',password,timezone:'UTC'}});expect(invalid.status()).toBe(403);
 await account(page,'secure');const cookies=await page.context().cookies();const session=cookies.find(c=>c.name==='JSESSIONID');expect(session?.httpOnly).toBe(true);expect(session?.sameSite).toBe('Lax');
 const missingCsrf=await page.request.post('/logout');expect(missingCsrf.status()).toBe(403);
});
test('workspace and task dialog meet automated accessibility checks',async({page})=>{
 await account(page,'accessible');await page.screenshot({path:'test-results/workspace-desktop.png',fullPage:true});const results=await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21aa']).analyze();expect(results.violations.map(v=>({id:v.id,nodes:v.nodes.map(n=>({target:n.target,summary:n.failureSummary}))}))).toEqual([]);
 await page.getByRole('button',{name:'Add task',exact:true}).last().click();const dialog=await new AxeBuilder({page}).include('dialog').withTags(['wcag2a','wcag2aa','wcag21aa']).analyze();expect(dialog.violations).toEqual([]);
});
