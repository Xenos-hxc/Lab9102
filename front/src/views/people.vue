<template>
  <div class="people-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <!--      <h1 class="page-title">科研人员</h1>-->
      <!--      <p class="page-subtitle">智能控制与先进系统实验室团队成员介绍</p>-->
    </div>

    <div class="layout-container">
      <!-- 侧边栏导航 -->
      <div class="sidebar">
        <!-- 成员分类菜单 -->
        <div class="category-menu">
          <div
            v-for="category in categories"
            :key="category.value"
            :class="['menu-item', { active: activeCategory === category.value }]"
            @click="handleCategoryChange(category.value)"
          >
            <span class="menu-icon">
              <el-icon v-if="category.value === 'director'"><User /></el-icon>
              <el-icon v-if="category.value === 'mentor'"><Guide /></el-icon>
              <el-icon v-if="category.value === 'phd'"><School /></el-icon>
              <el-icon v-if="category.value === 'master'"><Reading /></el-icon>
              <el-icon v-if="category.value === 'graduate'"><Trophy /></el-icon>
            </span>
            <span class="menu-label">{{ category.label }}</span>
            <span class="menu-count">{{ getCategoryCount(category.value) }}</span>
          </div>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="main-content">
        <!-- 顶部操作栏：搜索和排序 -->
        <div class="top-actions">
          <el-input
            v-model="searchText"
            placeholder="请输入姓名进行搜索"
            clearable
            class="search-input"
            :prefix-icon="Search"
            size="large"
            @input="handleSearch"
          />
          <el-button
            class="sort-button"
            @click="toggleSortOrder"
            size="large"
          >
            <el-icon style="margin-right: 8px"><Sort /></el-icon>
            {{ sortOrder === 'asc' ? '年份升序' : '年份降序' }}
          </el-button>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="6" animated />
        </div>

        <!-- 成员卡片展示 -->
        <div v-else class="members-container">
          <el-row :gutter="24">
            <el-col
              v-for="member in filteredMembers"
              :key="member.id"
              :xs="24"
              :sm="12"
              :md="8"
              :lg="8"
              class="equal-height-col"
            >
              <el-card class="member-card" shadow="hover">
                <!-- 个人照片区域 -->
                <div class="photo-section">
                  <div class="photo-container">
                    <img
                      v-if="member.pictureurl"
                      :src="getImageUrl(member.pictureurl)"
                      :alt="member.name"
                      class="member-photo"
                      @error="handleImageError"
                    />
                    <div v-else class="photo-placeholder">
                      <el-icon><User /></el-icon>
                      <span>暂无照片</span>
                    </div>
                    <div class="photo-overlay">
                      <div class="category-badge">{{ getIdentityLabel(member.identity) }}</div>
                    </div>
                  </div>
                </div>

                <!-- 成员基本信息 -->
                <div class="member-info">
                  <h3 class="member-name">{{ member.name }}</h3>
                  <p class="member-intro">{{ member.introduction }}</p>

                  <!-- 研究方向 -->
                  <div class="research-area">
                    <el-icon><Compass /></el-icon>
                    <span class="area-content">{{ getResearchAreas(member.id) }}</span>
                  </div>

                  <!-- 联系信息 -->
                  <div class="contact-info">
                    <div class="email-section">
                      <el-icon><Message /></el-icon>
                      <span class="email">{{ member.email }}</span>
                    </div>
                  </div>

                  <!-- 时间信息 -->
                  <div class="time-info">
                    <div class="join-time">
                      <el-icon><Calendar /></el-icon>
                      <span>{{ formatEntryTime(member.entrytime) }}</span>
                    </div>
                  </div>

                  <!-- 毕业去向 -->
                  <div class="workplace-info" v-if="member.identity === 'graduate' && member.workplace">
                    <div class="workplace">
                      <el-icon><OfficeBuilding /></el-icon>
                      <span>{{ member.workplace }}</span>
                    </div>
                  </div>
                </div>

                <!-- 操作按钮 -->
                <div class="card-actions">
                  <el-button type="primary" text @click="viewDetail(member)">
                    <el-icon><View /></el-icon>
                    查看详情
                  </el-button>
                  <el-button type="success" text @click="sendEmail(member)">
                    <el-icon><Message /></el-icon>
                    联系
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 空状态 -->
          <div v-if="filteredMembers.length === 0 && !loading" class="empty-state">
            <el-empty description="暂无相关成员信息" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Search, Message, Calendar, School, View, User, Guide,
  Reading, Trophy, Compass, OfficeBuilding, Sort
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Request from "@/utils/request.ts";
import { getApiBaseUrl } from '@/utils/apiConfig'
// 路由实例
const router = useRouter()

// 成员类型定义
interface Member {
  id: number
  name: string
  identity: string
  introduction: string
  entrytime: string
  email: string
  pictureurl: string
  username: string
  password: string
  workplace?: string
}

// 研究方向关联
interface MemberDirection {
  id: number
  memberid: number
  directionid: number
}

// 研究方向
interface Direction {
  id: number
  name: string
}

// 响应式数据
const searchText = ref('')
const sortOrder = ref('asc')
const activeCategory = ref('mentor') // 修改为默认显示导师列表
const loading = ref(false)
const allMembers = ref<Member[]>([])
const memberDirections = ref<MemberDirection[]>([])
const directions = ref<Direction[]>([])

// 成员分类映射
const identityMap = {
  'mentor': '导师',
  'phd': '博士研究生',
  'master': '硕士研究生',
  'graduate': '毕业生'
}

// 成员分类
const categories = [
  { label: '导师', value: 'mentor' },
  { label: '博士研究生', value: 'phd' },
  { label: '硕士研究生', value: 'master' },
  { label: '毕业生', value: 'graduate' }
]

// 计算属性：过滤成员
const filteredMembers = computed(() => {
  let filtered = allMembers.value.filter(member => {
    const identityLabel = getIdentityLabel(member.identity)
    return identityLabel === identityMap[activeCategory.value as keyof typeof identityMap]
  })

  if (searchText.value) {
    filtered = filtered.filter(member =>
      member.name.toLowerCase().includes(searchText.value.toLowerCase())
    )
  }

  // 排序
  filtered.sort((a, b) => {
    const timeA = parseInt(a.entrytime) || 0
    const timeB = parseInt(b.entrytime) || 0
    return sortOrder.value === 'asc' ? timeA - timeB : timeB - timeA
  })

  return filtered
})

// 方法
const getCategoryCount = (category: string) => {
  const identityLabel = identityMap[category as keyof typeof identityMap]
  return allMembers.value.filter(member =>
    getIdentityLabel(member.identity) === identityLabel
  ).length
}

const getIdentityLabel = (identity: string) => {
  return identityMap[identity as keyof typeof identityMap] || identity
}

const getResearchAreas = (memberid: number) => {
  const memberDir = memberDirections.value.filter(md => md.memberid == memberid)
  const dirNames = memberDir.map(md => {
    const direction = directions.value.find(d => d.id == md.directionid)
    return direction ? direction.name : ''
  }).filter(name => name)

  return dirNames.join('、') || '暂无研究方向'
}

const formatEntryTime = (entrytime: string) => {
  if (!entrytime) return '未知时间'
  let suffix = '入学'
  if (activeCategory.value === 'mentor') {
    suffix = '入职'
  } else if (activeCategory.value === 'graduate') {
    suffix = '毕业'
  }
  return `${entrytime}年${suffix}`
}

const getImageUrl = (pictureurl: string) => {
  if (!pictureurl) return ''
  if (pictureurl.startsWith('http')) return pictureurl
  return `${getApiBaseUrl()}${pictureurl.startsWith('/') ? '' : '/'}${pictureurl}`
}

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
img.parentElement?.querySelector('.photo-placeholder')?.classList.remove('hidden')
}

const handleCategoryChange = (category: string) => {
  activeCategory.value = category
}

const toggleSortOrder = () => {
  sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
}

const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

// API调用方法
const fetchMembers = async () => {
  try {
    loading.value = true
    const result = await Request(`/member/list`)
    console.log(result)

    if (result.code == 200) {
      allMembers.value = result.data
    } else {
      ElMessage.error('获取成员数据失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请检查后端服务是否启动')
  } finally {
    loading.value = false
  }
}

const fetchDirections = async () => {
  try {
    const result = await Request(`/direction/list`)
    console.log(result.data)

    if (result.code == 200) {
      directions.value = result.data
    }
  } catch (error) {
    console.error('获取研究方向数据失败')
  }
}

const fetchMemberDirections = async () => {
  try {
    // 调用后端API获取成员研究方向关联数据
    const result = await Request(`/memberdirection/list`)

    if (result.code == 200) {
      memberDirections.value = result.data
      console.log('获取成员研究方向关联成功:', result.data)
    } else {
      console.error('获取成员研究方向关联失败:', result.message)
      ElMessage.error('获取成员研究方向关联失败')
    }
  } catch (error) {
    console.error('获取成员研究方向关联失败:', error)
    ElMessage.error('网络错误，请检查后端服务是否启动')
  }
}

// 修改查看详情方法，添加路由跳转
const viewDetail = (member: Member) => {
  // 跳转到研究人员详情页面
  router.push(`/researcher/${member.id}`)
}

const sendEmail = (member: Member) => {
  window.location.href = `mailto:${member.email}`
}

onMounted(() => {
  fetchMembers()
  fetchDirections()
  fetchMemberDirections()
})
</script>

<style scoped>
/* 原有的样式保持不变，只添加新的样式 */
.hidden {
  display: none;
}

.loading-state {
  padding: 20px;
}

/* 其他原有样式保持不变 */
.people-page {
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 2.5rem;
  color: #2c3e50;
  margin-bottom: 10px;
  font-weight: 600;
}

.page-subtitle {
  font-size: 1.1rem;
  color: #7f8c8d;
  margin: 0;
}

.layout-container {
  display: flex;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 侧边栏样式 */
.sidebar {
  width: 280px;
  flex-shrink: 0;
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  height: fit-content;
  position: sticky;
  top: 20px;
}

.top-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  align-items: center;
}

.search-input {
  flex: 1;
}

.sort-button {
  min-width: 140px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.category-menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.menu-item:hover {
  background: #f8fafc;
  border-color: #e2e8f0;
}

.menu-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: #667eea;
}

.menu-icon {
  margin-right: 12px;
  font-size: 18px;
}

.menu-label {
  flex: 1;
  font-weight: 500;
}

.menu-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  min-width: 24px;
  text-align: center;
}

.menu-item.active .menu-count {
  background: rgba(255, 255, 255, 0.3);
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  min-width: 0;
}

.members-container {
  padding: 0;
}

.member-card {
  margin-bottom: 24px;
  border-radius: 12px;
  transition: all 0.3s ease;
  border: none;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: calc(100% - 24px); /* Subtract margin-bottom */
  width: 100%; /* Ensure full width */
}

:deep(.equal-height-col) {
  display: flex;
}

.member-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* 照片区域样式 */
.photo-section {
  position: relative;
  margin: -20px -20px 20px -20px;
}

.photo-container {
  position: relative;
  height: 0;
  padding-bottom: 75%; /* 4:3 宽高比，可以根据需要调整 */
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.member-photo {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.member-card:hover .member-photo {
  transform: scale(1.05);
}

.photo-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
}

.photo-placeholder .el-icon {
  font-size: 3rem;
  margin-bottom: 8px;
  opacity: 0.7;
}

.photo-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
}

.category-badge {
  background: rgba(255, 255, 255, 0.9);
  color: #2c3e50;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
}

/* 成员信息样式 */
.member-info {
  padding: 0 8px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.member-name {
  font-size: 1.4rem;
  color: #2c3e50;
  margin-bottom: 12px;
  font-weight: 600;
  text-align: center;
  /* Fixed height for alignment */
  height: 32px;
  line-height: 32px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.member-intro {
  color: #7f8c8d;
  font-size: 0.9rem;
  line-height: 1.5;
  margin-bottom: 16px;
  text-align: justify;
  /* Fixed height for alignment (3 lines) */
  height: 65px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.research-area {
  display: flex;
  align-items: flex-start; /* Align to top */
  margin: 12px 0;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  color: #5a6c7d;
  /* Fixed height for alignment */
  height: 68px;
  box-sizing: border-box;
  overflow: hidden;
}

.research-area .el-icon {
  margin-right: 8px;
  color: #667eea;
  margin-top: 3px; /* Align icon with first line of text */
}

.area-content {
  font-size: 0.9rem;
  line-height: 1.4;
  /* Limit to 2 lines */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.contact-info {
  margin: 8px 0;
  /* Fixed height for alignment */
  height: 40px;
  overflow: hidden;
}

.time-info {
  margin: 8px 0;
  /* Fixed height for alignment */
  height: 24px;
  overflow: hidden;
}

.email-section, .join-time, .graduate-time, .workplace {
  display: flex;
  align-items: center;
  margin: 6px 0;
  color: #7f8c8d;
  font-size: 0.9rem;
}

.email-section .el-icon, .join-time .el-icon, .graduate-time .el-icon, .workplace .el-icon {
  margin-right: 8px;
  color: #3498db;
}

.workplace-info {
  margin: 8px 0;
  height: 40px;
  overflow: hidden;
}

.email {
  word-break: break-all;
}

/* 操作按钮 */
.card-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .layout-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    position: static;
  }

  .top-actions {
    flex-direction: column;
    gap: 12px;
  }

  .sort-button {
    width: 100%;
  }

  .category-menu {
    flex-direction: row;
    overflow-x: auto;
    padding-bottom: 8px;
  }

  .menu-item {
    flex-shrink: 0;
    min-width: 120px;
  }
}

@media (max-width: 768px) {
  .people-page {
    padding: 10px;
  }

  .page-title {
    font-size: 2rem;
  }

  .sidebar {
    padding: 16px;
  }

  .member-card {
    margin-bottom: 16px;
  }

  .photo-container {
    height: 160px;
  }

  .card-actions {
    flex-direction: column;
    gap: 8px;
  }

  .card-actions .el-button {
    width: 100%;
  }
}

/* 动画效果 */
.member-card {
  animation: fadeInUp 0.6s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
