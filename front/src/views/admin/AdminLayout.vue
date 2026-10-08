<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <el-container class="layout-container">
      <el-aside width="250px" class="sidebar">
        <div class="sidebar-header">
          <h3>实验室管理系统</h3>
          <p>管理员面板</p>
        </div>

        <!-- 用户信息 -->
        <div class="user-info">
          <div class="user-avatar">
            <el-icon><User /></el-icon>
          </div>
          <div class="user-details">
            <div class="user-name">{{ userInfo.name || '管理员' }}</div>
            <div class="user-role">{{ userInfo.identity || '系统管理员' }}</div>
          </div>
        </div>

        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <el-menu-item v-if="canAccess('lab-info')" index="/admin/lab-info">
            <el-icon><OfficeBuilding /></el-icon>
            <span>实验室信息</span>
          </el-menu-item>

          <el-menu-item v-if="canAccess('researchers')" index="/admin/researchers">
            <el-icon><User /></el-icon>
            <span>科研人员</span>
          </el-menu-item>

          <el-menu-item v-if="canAccess('research-directions')" index="/admin/research-directions">
            <el-icon><Compass /></el-icon>
            <span>研究方向</span>
          </el-menu-item>

          <el-menu-item v-if="canAccess('publications')" index="/admin/publications">
            <el-icon><Document /></el-icon>
            <span>科研成果</span>
          </el-menu-item>

          <el-menu-item v-if="canAccess('equipment')" index="/admin/equipment">
            <el-icon><Cpu /></el-icon>
            <span>团队设备</span>
          </el-menu-item>

          <el-menu-item v-if="canAccess('news')" index="/admin/news">
            <el-icon><Calendar /></el-icon>
            <span>新闻记录</span>
          </el-menu-item>

          <el-menu-item v-if="canAccess('lectures')" index="/admin/lectures">
            <el-icon><Microphone /></el-icon>
            <span>报告讲座</span>
          </el-menu-item>

          <el-menu-item v-if="canAccess('carousel')" index="/admin/carousel">
            <el-icon><PictureRounded /></el-icon>
            <span>走马灯管理</span>
          </el-menu-item>

          <el-menu-item v-if="canAccess('forum')" index="/admin/forum">
            <el-icon><ChatLineSquare /></el-icon>
            <span>研学社区管理</span>
          </el-menu-item>

          <el-menu-item v-if="canAccess('resolution-settings')" index="/admin/resolution-settings">
            <el-icon><Monitor /></el-icon>
            <span>分辨率设置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区域 -->
      <el-container>
        <el-header class="header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>管理员面板</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <span class="welcome-text">欢迎，{{ userInfo.name || '管理员' }}</span>
            <el-button @click="returnToSite">
              <el-icon><Back /></el-icon>
              返回用户端
            </el-button>
            <el-button type="primary" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-button>
          </div>
        </el-header>

        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  OfficeBuilding, User, Compass, Document, Calendar, Microphone,
  SwitchButton, PictureRounded, ChatLineSquare, Cpu, Monitor, Back
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { clearForumUser, getForumUser } from '@/utils/forumUser'
import { canAccessAdminMenu } from '@/utils/adminPermissions'

const router = useRouter()
const route = useRoute()

// 用户信息
interface UserInfo {
  id: number
  username: string
  name: string
  identity: string
}

const userInfo = ref<UserInfo>({
  id: 0,
  username: '',
  name: '',
  identity: ''
})

// 计算当前激活的菜单项
const activeMenu = computed(() => {
  return route.path
})

const canAccess = (permission: string) => canAccessAdminMenu(getForumUser(), permission)

const returnToSite = () => router.push('/homePage')

// 处理退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 清除用户信息
    userInfo.value = {
      id: 0,
      username: '',
      name: '',
      identity: ''
    }
    clearForumUser()

    ElMessage.success('退出登录成功')
    router.push('/')
  } catch (error) {
    // 用户取消退出
  }
}

// 组件挂载时，可以在这里获取用户信息
onMounted(() => {
  const storedUser = getForumUser()
  if (!storedUser) {
    router.push('/homePage')
    return
  }
  userInfo.value = {
    id: storedUser.id || 0,
    username: storedUser.username,
    name: storedUser.name || storedUser.username,
    identity: storedUser.username === 'admin' ? '系统管理员' : '授权管理员'
  }
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  background-color: #f0f2f5;
}

.layout-container {
  height: 100%;
}

.sidebar {
  background-color: #304156;
  color: #fff;
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #475669;
}

.sidebar-header h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
  color: #fff;
}

.sidebar-header p {
  margin: 0;
  font-size: 12px;
  color: #bfcbd9;
}

.user-info {
  padding: 20px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #475669;
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.user-avatar .el-icon {
  font-size: 20px;
  color: white;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.user-role {
  font-size: 12px;
  color: #bfcbd9;
}

.sidebar-menu {
  border: none;
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.welcome-text {
  margin-right: 15px;
  color: #606266;
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.main-content {
  padding: 20px;
  background-color: #f0f2f5;
}
</style>
