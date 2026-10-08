export interface ForumUser {
  id?: number
  username: string
  name?: string
  identity?: string
  isAdmin?: boolean
  adminPermissions?: string
}

const USER_KEY = 'lab_forum_user'
export const FORUM_USER_EVENT = 'lab-forum-user-changed'

const notifyUserChange = (user: ForumUser | null) => {
  window.dispatchEvent(new CustomEvent(FORUM_USER_EVENT, { detail: user }))
}

export const saveForumUser = (user: ForumUser) => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
  notifyUserChange(user)
}

export const getForumUser = (): ForumUser | null => {
  try {
    const value = localStorage.getItem(USER_KEY)
    return value ? JSON.parse(value) : null
  } catch {
    return null
  }
}

export const getForumUsername = () => getForumUser()?.username || ''

export const clearForumUser = () => {
  localStorage.removeItem(USER_KEY)
  notifyUserChange(null)
}
