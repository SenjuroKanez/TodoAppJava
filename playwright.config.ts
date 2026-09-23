import {defineConfig} from '@playwright/test';
export default defineConfig({
 testDir:'tests/e2e',timeout:60000,expect:{timeout:15000},fullyParallel:false,workers:1,
 reporter:[['list'],['html',{open:'never'}]],
 use:{baseURL:process.env.TEST_BASE_URL??'http://localhost:8080',headless:true,trace:'retain-on-failure',screenshot:'only-on-failure',viewport:{width:1440,height:1000}},
 projects:[{name:'chromium',use:{browserName:'chromium',channel:process.env.PLAYWRIGHT_CHANNEL||undefined}}]
});
