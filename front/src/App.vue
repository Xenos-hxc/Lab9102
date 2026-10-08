<template>
  <div class="app">
    <!-- 顶部图片 -->
    <div v-if="!isAdminPage" class="top-banner">
      <img src="/src/assets/img.png" alt="智能控制与先进系统实验室" class="banner-image">
    </div>

    <!-- 导航栏 -->
    <div v-if="!isAdminPage" class="nav-container">
      <el-menu
        :default-active="activeIndex"
        class="nav-menu"
        mode="horizontal"
        @select="handleSelect"
        background-color="#1e3a8a"
        text-color="#fff"
        active-text-color="#ffd700"
      >
        <el-menu-item index="/homePage">主页</el-menu-item>
        <el-menu-item index="/people">科研人员</el-menu-item>
        <el-menu-item index="/research">研究方向</el-menu-item>
        <el-menu-item index="/publications">科研成果</el-menu-item>
        <el-menu-item index="/equipment">团队设备</el-menu-item>
        <el-menu-item index="/news">新闻记录</el-menu-item>
        <el-menu-item index="/teaching">报告讲座</el-menu-item>
        <el-menu-item index="/community">研学社区</el-menu-item>
        <el-menu-item index="/contact">联系我们</el-menu-item>
        <el-menu-item v-if="currentUserIsAdmin" index="/admin">管理后台</el-menu-item>
      </el-menu>

      <!-- 登录状态 -->
      <div class="login-btn-container">
        <el-button
          v-if="!currentUser"
          type="primary"
          @click="showLoginDialog = true"
          class="login-btn"
        >
          <el-icon><User /></el-icon>
          登录
        </el-button>
        <el-dropdown
          v-else
          trigger="click"
          placement="bottom-end"
          :fallback-placements="['bottom-end', 'bottom-start']"
          popper-class="member-dropdown-popper"
          @command="handleUserCommand"
        >
          <el-button class="member-menu-btn">
            <el-icon><User /></el-icon>
            <span class="member-name">{{ memberDisplayName }}</span>
            <el-icon class="member-arrow"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人信息
              </el-dropdown-item>
              <el-dropdown-item v-if="currentUserIsAdmin" command="admin">
                <el-icon><Setting /></el-icon>
                管理后台
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <main class="main-content" :class="{ 'admin-main': isAdminPage }">
      <router-view></router-view>
    </main>

    <!-- 底部联系方式 -->
    <footer v-if="!isAdminPage" class="footer">
      <div class="footer-content">
        <el-row :gutter="40">
          <el-col :span="12">
            <div class="contact-info">
              <h4>联系方式</h4>
              <p><strong>学院：</strong>{{ basicInfo.college || '信息科学与技术学院' }}</p>
              <p><strong>Email：</strong>{{ basicInfo.email || 'contact@example.com' }}</p>
<!--              <p><strong>电话：</strong>{{ basicInfo.phone || '028-8760XXXX' }}</p>-->
            </div>
          </el-col>
          <el-col :span="12">
            <div class="address-info">
              <h4>通讯地址</h4>
              <p><strong>实验室名称：</strong>{{ basicInfo.labName || '智能控制与先进系统实验室' }}</p>
              <p><strong>实验室地址：</strong>{{ basicInfo.address || '西南交通大学智能系统与先进控制实验室<br>四川省成都市郫都区犀安路999号<br>中国，成都' }}</p>
            </div>
          </el-col>
        </el-row>
      </div>
    </footer>

    <!-- 美化后的登录对话框 -->
    <el-dialog
      v-model="showLoginDialog"
      title=""
      width="420px"
      :before-close="handleClose"
      class="login-dialog"
    >
      <div class="login-header">
        <div class="login-icon">
          <el-icon><User /></el-icon>
        </div>
<!--        <h3>实验室管理系统</h3>-->
<!--        <p>请输入管理员账号信息</p>-->
      </div>

      <el-form
        :model="loginForm"
        :rules="loginRules"
        ref="loginFormRef"
        label-width="0"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            clearable
            show-password
          />
        </el-form-item>
      </el-form>

<!--      <div class="login-tips">-->
<!--        <el-icon><InfoFilled /></el-icon>-->
<!--        <span>提示：只有管理员账号（username为admin）可以登录</span>-->
<!--      </div>-->

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showLoginDialog = false" size="large">取消</el-button>
          <el-button
            type="primary"
            @click="handleLogin"
            :loading="loginLoading"
            size="large"
            class="login-submit-btn"
          >
            {{ loginLoading ? '登录中...' : '登录' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowDown, User, Lock, SwitchButton, Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import {
  clearForumUser,
  FORUM_USER_EVENT,
  getForumUser,
  saveForumUser,
  type ForumUser
} from '@/utils/forumUser'
import { getDefaultAdminPath, isAdminUser } from '@/utils/adminPermissions'
import { refreshDisplayScale } from '@/utils/displayScale'

const router = useRouter()
const route = useRoute()
const activeIndex = ref('/homePage')
const showLoginDialog = ref(false)
const loginLoading = ref(false)
const loginFormRef = ref()
const currentUser = ref<ForumUser | null>(getForumUser())

const memberDisplayName = computed(() => {
  return currentUser.value?.name?.trim() || currentUser.value?.username || '成员'
})

const currentUserIsAdmin = computed(() => isAdminUser(currentUser.value))

// 计算属性：判断当前是否在管理员页面
const isAdminPage = computed(() => {
  return route.path.startsWith('/admin')
})

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

// 登录验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 1, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 实验室基本信息
interface BasicInfo {
  labName: string
  university: string
  college: string
  phone: string
  address: string
  email: string
}

const basicInfo = ref<BasicInfo>({
  labName: '',
  university: '',
  college: '',
  phone: '',
  address: '',
  email: ''
})

// 获取实验室基本信息
const fetchBasicInfo = async () => {
  try {
    const response = await request.get('/labinfo/basic')

    if (response.code == 200) {
      const data = response.data
      basicInfo.value = {
        labName: data.labName || '智能控制与先进系统实验室',
        university: data.university || '西南交通大学',
        college: data.college || '信息科学与技术学院',
        phone: data.phone || '028-8760XXXX',
        address: data.address || '四川省成都市郫都区犀安路999号',
        email: data.email || 'contact@example.com'
      }
    } else {
      // 使用默认数据作为后备
      basicInfo.value = {
        labName: '智能控制与先进系统实验室',
        university: '西南交通大学',
        college: '信息科学与技术学院',
        phone: '028-8760XXXX',
        address: '四川省成都市郫都区犀安路999号',
        email: 'contact@example.com'
      }
    }
  } catch (error) {
    console.error('获取实验室基本信息出错:', error)
    // 使用默认数据作为后备
    basicInfo.value = {
      labName: '智能控制与先进系统实验室',
      university: '西南交通大学',
      college: '信息科学与技术学院',
      phone: '028-8760XXXX',
      address: '四川省成都市郫都区犀安路999号',
      email: 'contact@example.com'
    }
  }
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    // 验证表单
    await loginFormRef.value.validate()
    loginLoading.value = true
    console.log('登录请求:', loginForm)
    // 发送登录请求
    const response = await request.post('/auth/login', loginForm)
    console.log('登录响应:', response)
    if (response.code == 200) {
      ElMessage.success('登录成功')
      const loginUser = {
        id: response.data.id,
        username: response.data.username,
        name: response.data.name,
        identity: response.data.identity,
        isAdmin: response.data.isAdmin,
        adminPermissions: response.data.adminPermissions
      }
      saveForumUser(loginUser)
      currentUser.value = loginUser
      showLoginDialog.value = false
      // 重置表单
      loginForm.username = ''
      loginForm.password = ''
      console.log('登录用户名:', loginForm.username)
      // 根据用户名判断跳转页面
      if (isAdminUser(loginUser)) {
        // 管理员跳转到管理员页面
        router.push(getDefaultAdminPath(loginUser))
      } else {
        //普通成员跳转到个人信息管理页面
        router.push('/personal-info')
      }
    } else {
      ElMessage.error(response.message || '登录失败')
    }
  } catch (error: any) {
    if (error.response?.status === 401) {
      ElMessage.error('用户名或密码错误')
    } else if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('登录失败，请稍后重试')
    }
    console.error('登录出错:', error)
  } finally {
    loginLoading.value = false
  }
}

const handleUserCommand = async (command: string) => {
  if (command === 'admin') {
    router.push(getDefaultAdminPath(currentUser.value))
    return
  }
  if (command === 'profile') {
    router.push('/personal-info')
    return
  }
  if (command !== 'logout') return

  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.post('/auth/logout')
    clearForumUser()
    currentUser.value = null
    ElMessage.success('退出登录成功')
    router.push('/homePage')
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('退出登录失败:', error)
      ElMessage.error('退出登录失败，请稍后重试')
    }
  }
}

const handleStoredUserChange = (event: Event) => {
  currentUser.value = (event as CustomEvent<ForumUser | null>).detail
}

// 关闭对话框
const handleClose = (done: () => void) => {
  if (loginForm.username || loginForm.password) {
    ElMessageBox.confirm('确定要关闭登录窗口吗？未保存的输入将会丢失', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      loginForm.username = ''
      loginForm.password = ''
      done()
    }).catch(() => {
      // 取消关闭
    })
  } else {
    done()
  }
}

// 监听路由变化，更新激活的菜单项
watch(() => route.path, (newPath) => {
  activeIndex.value = newPath.startsWith('/community') ? '/community' : newPath
})

const handleSelect = (key: string) => {
  activeIndex.value = key
  router.push(key)
}

// 组件挂载时获取数据
onMounted(() => {
  window.addEventListener(FORUM_USER_EVENT, handleStoredUserChange)
  fetchBasicInfo()
  refreshDisplayScale()
})

onUnmounted(() => {
  window.removeEventListener(FORUM_USER_EVENT, handleStoredUserChange)
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.6;
  color: #333;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.top-banner {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.nav-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #1e3a8a;
  padding: 0 20px;
}

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.nav-menu .el-menu-item {
  font-size: 16px; /* 增加导航栏字体大小 */
  font-weight: 500;
  padding: 0 15px;
}

.login-btn-container {
  margin-left: 20px;
  padding-right: 4px;
  flex: 0 0 auto;
  min-width: 0;
}

.member-dropdown-popper {
  max-width: calc(100vw - 24px);
}

.login-btn {
  background-color: #ffd700;
  border-color: #ffd700;
  color: #1e3a8a;
  font-weight: bold;
}

.login-btn:hover {
  background-color: #ffed4a;
  border-color: #ffed4a;
}

.member-menu-btn {
  min-width: 108px;
  height: 36px;
  padding: 0 15px;
  color: #1e3a8a;
  border-color: #ffd700;
  border-radius: 18px;
  background: #ffd700;
  font-weight: 600;
}

.member-menu-btn:hover,
.member-menu-btn:focus {
  color: #17316f;
  border-color: #ffed4a;
  background: #ffed4a;
}

.member-name {
  display: inline-block;
  overflow: hidden;
  max-width: 92px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-arrow {
  margin-left: 2px;
  font-size: 12px;
}

.main-content {
  flex: 1;
  padding: 20px 0;
}

/* 管理员页面的主内容区域样式 */
.main-content.admin-main {
  padding: 0;
}

.footer {
  background-color: #2d3748;
  color: white;
  padding: 40px 20px;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
}

.contact-info h4, .address-info h4 {
  color: #ffd700;
  margin-bottom: 15px;
}

.contact-info p, .address-info p {
  margin-bottom: 10px;
  line-height: 1.5;
}

.copyright {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #4a5568;
  color: #a0aec0;
}

/* 美化登录对话框 */
.login-dialog .el-dialog__header {
  border-bottom: none;
  padding-bottom: 0;
}

.login-dialog .el-dialog__body {
  padding: 30px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
}

.login-icon .el-icon {
  font-size: 28px;
  color: white;
}

.login-header h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #303133;
}

.login-header p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.login-form {
  margin-bottom: 20px;
}

.login-form .el-input__wrapper {
  border-radius: 8px;
}

.login-tips {
  background-color: #f0f9ff;
  border: 1px solid #e1f3ff;
  border-radius: 6px;
  padding: 12px 15px;
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #409EFF;
  margin-bottom: 20px;
}

.login-tips .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.login-submit-btn {
  min-width: 100px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer {
    padding: 20px 10px;
  }

  .nav-container {
    flex-direction: column;
    padding: 10px;
  }

  .nav-menu {
    width: 100%;
    justify-content: flex-start;
    overflow-x: auto;
  }

  .login-btn-container {
    margin-left: 0;
    margin-top: 10px;
  }

  .login-dialog {
    width: 90% !important;
    max-width: 400px;
  }
}
</style>
