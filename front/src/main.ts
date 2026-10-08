import { createApp } from 'vue'
import App from './App.vue'

import {createRouter,createWebHistory,createWebHashHistory} from 'vue-router'
import { getForumUser } from './utils/forumUser'
import { canAccessAdminMenu, getDefaultAdminPath, isAdminUser } from './utils/adminPermissions'
//1、路由配置规则
const routes=[
  {path: '/', redirect: '/homePage'},
  {path: '/homePage', component: () => import('./views/homePage.vue')},
  {path: '/people', component: () => import('./views/people.vue')},
  {path: '/research', component: () => import('./views/research.vue')},
  {path: '/researcher/:id', component: () => import('./views/peopleDetail.vue')},
  {path: '/publications', component: () => import('./views/publications.vue')},
  {path: '/equipment', component: () => import('./views/equipment.vue')},
  {path: '/gallery', redirect: '/equipment'},
  {path: '/gallery-detail/:id', redirect: '/news'},
  {path: '/news-detail/:id', component: () => import('./views/newsDetail.vue')},
  {path: '/news', component: () => import('./views/news.vue')},
  {path: '/teaching', component: () => import('./views/teaching.vue')},
  {path: '/lecture/:id', component: () => import('./views/lectureDetail.vue')},
  {path: '/community', component: () => import('./views/forum/Community.vue')},
  {path: '/community/post/:id', component: () => import('./views/forum/ForumDetail.vue')},
  {path: '/community/editor', component: () => import('./views/forum/ForumEditor.vue')},
  {path: '/community/editor/:id', component: () => import('./views/forum/ForumEditor.vue')},
  {path: '/community/mine', component: () => import('./views/forum/MyForum.vue')},
  {path: '/contact', component: () => import('./views/contact.vue')},
  {path: '/test', component: () => import('./views/testCode.vue')},
  // 个人信息管理页面
  {path: '/personal-info', component: () => import('./views/researcher/PersonalInfo.vue')},
  // 管理员端路由
  {
    path: '/admin',
    component: () => import('./views/admin/AdminLayout.vue'),
    redirect: () => getDefaultAdminPath(getForumUser()),
    children: [
      {path: 'lab-info', component: () => import('./views/admin/LabInfo.vue'), meta: { adminPermission: 'lab-info' }},
      {path: 'researchers', component: () => import('./views/admin/Researchers.vue'), meta: { adminPermission: 'researchers' }},
      {path: 'research-directions', component: () => import('./views/admin/ResearchDirections.vue'), meta: { adminPermission: 'research-directions' }},
      {path: 'publications', component: () => import('./views/admin/Publications.vue'), meta: { adminPermission: 'publications' }},
      {path: 'equipment', component: () => import('./views/admin/Equipment.vue'), meta: { adminPermission: 'equipment' }},
      {path: 'gallery', redirect: '/admin/equipment'},
      {path: 'news', component: () => import('./views/admin/News.vue'), meta: { adminPermission: 'news' }},
      {path: 'lectures', component: () => import('./views/admin/Lectures.vue'), meta: { adminPermission: 'lectures' }},
      {path: 'carousel', component: () => import('./views/admin/Carousel.vue'), meta: { adminPermission: 'carousel' }},
      {path: 'forum', component: () => import('./views/admin/ForumManagement.vue'), meta: { adminPermission: 'forum' }},
      {path: 'resolution-settings', component: () => import('./views/admin/ResolutionSettings.vue'), meta: { adminPermission: 'resolution-settings' }}
    ]
  }
]
//2、创建路由器
const router=createRouter({
  history:createWebHistory(),//路由工作模式
  routes:routes,//路由配置规则
  scrollBehavior: () => ({ top: 0, left: 0 })
})

router.beforeEach((to) => {
  if (!to.path.startsWith('/admin')) return true
  const user = getForumUser()
  if (!isAdminUser(user)) return '/homePage'
  const permission = to.meta.adminPermission as string | undefined
  if (permission && !canAccessAdminMenu(user, permission)) {
    return getDefaultAdminPath(user)
  }
  return true
})
//3、加载路由器
const app=createApp(App)
app.use(router)

import {createPinia} from 'pinia'
const pinia=createPinia()
app.use(pinia)

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
app.use(ElementPlus)

import axios from 'axios'
app.config.globalProperties.$axios = axios

app.mount('#app')
