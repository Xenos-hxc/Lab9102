import type { ForumUser } from './forumUser'

export const ADMIN_MENU_OPTIONS = [
  { key: 'lab-info', label: '实验室信息', path: '/admin/lab-info' },
  { key: 'researchers', label: '科研人员', path: '/admin/researchers' },
  { key: 'research-directions', label: '研究方向', path: '/admin/research-directions' },
  { key: 'publications', label: '科研成果', path: '/admin/publications' },
  { key: 'equipment', label: '团队设备', path: '/admin/equipment' },
  { key: 'news', label: '新闻记录', path: '/admin/news' },
  { key: 'lectures', label: '报告讲座', path: '/admin/lectures' },
  { key: 'carousel', label: '走马灯管理', path: '/admin/carousel' },
  { key: 'forum', label: '研学社区管理', path: '/admin/forum' },
  { key: 'resolution-settings', label: '分辨率设置', path: '/admin/resolution-settings' }
] as const

export type AdminMenuKey = typeof ADMIN_MENU_OPTIONS[number]['key']

export const isRootAdmin = (user: ForumUser | null) => user?.username === 'admin'

export const isAdminUser = (user: ForumUser | null) => {
  return Boolean(user && (isRootAdmin(user) || user.isAdmin))
}

export const getAdminPermissionKeys = (user: ForumUser | null): string[] => {
  if (!user) return []
  if (isRootAdmin(user) || user.adminPermissions === '*') {
    return ADMIN_MENU_OPTIONS.map(item => item.key)
  }
  return (user.adminPermissions || '')
    .split(',')
    .map(item => item.trim())
    .filter(Boolean)
}

export const canAccessAdminMenu = (user: ForumUser | null, key?: string) => {
  if (!isAdminUser(user)) return false
  if (!key) return true
  return getAdminPermissionKeys(user).includes(key)
}

export const getDefaultAdminPath = (user: ForumUser | null) => {
  const keys = getAdminPermissionKeys(user)
  return ADMIN_MENU_OPTIONS.find(item => keys.includes(item.key))?.path || '/homePage'
}
