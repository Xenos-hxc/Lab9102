<template>
  <div class="gallery-page">
    <!-- 页面标题 -->


    <div class="layout-container">
      <!-- 侧边导航栏 -->
      <div class="sidebar">
        <!-- 搜索框 -->
        <div class="search-section">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索团建主题..."
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- 年份导航菜单 -->
        <div class="nav-menu">
          <el-menu
            :default-active="activeYear"
            class="nav-menu-list"
            @select="handleYearSelect"
          >
            <el-menu-item
              v-for="year in availableYears"
              :key="year"
              :index="year.toString()"
            >
              <div class="menu-item-content">
                <el-icon><Calendar /></el-icon>
                <span class="menu-title">{{ year }}年</span>
              </div>
            </el-menu-item>
          </el-menu>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="main-content">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="10" animated />
        </div>

        <!-- 活动列表 -->
        <div v-else class="activities-section">
          <div class="section-header">
<!--            <h2 class="section-title">{{ activeYear }}年团建活动</h2>-->
            <p class="section-subtitle">共 {{ filteredActivities.length }} 个活动</p>
          </div>

          <!-- 活动网格 -->
          <div class="activities-grid">
            <div
              v-for="activity in filteredActivities"
              :key="activity.id"
              class="activity-card-wrapper"
            >
              <el-card shadow="hover" class="activity-item">
                <!-- 活动图片 -->
                <div class="activity-image">
                  <img
                    :src="getImageUrl(activity.pictureurl)"
                    :alt="activity.title"
                    @error="handleImageError"
                  />
                  <div class="image-overlay">
                    <el-tag class="activity-type" :type="getActivityTypeTag(activity.type)">
                      {{ activity.type }}
                    </el-tag>
                  </div>
                </div>

                <!-- 活动内容 -->
                <div class="activity-content">
                  <div class="activity-header">
                    <h3 class="activity-title">{{ activity.title }}</h3>
                    <div class="activity-meta">
                      <span class="activity-date">
                        <el-icon><Clock /></el-icon>
                        {{ formatDate(activity.time) }}
                      </span>
                    </div>
                  </div>

                  <div class="activity-description">
                    <p>{{ activity.summary }}</p>
                  </div>

                  <!-- 活动操作 -->
                  <div class="activity-actions">
                    <el-button type="primary" text @click="handleActivityClick(activity.id)">
                      <el-icon><View /></el-icon>
                      查看详情
                    </el-button>
                  </div>
                </div>
              </el-card>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="filteredActivities.length === 0" class="empty-state">
            <el-empty description="暂无相关团建活动" :image-size="100" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search, Calendar, Clock, View
} from '@element-plus/icons-vue'

// 活动类型定义（与后端实体类对应）
interface Activity {
  id: number
  title: string
  type: string
  time: string
  summary: string
  pictureurl: string
}

// 获取路由实例
const router = useRouter()

// 响应式数据
const activeYear = ref('')
const searchKeyword = ref('')
const loading = ref(false)
const activities = ref<Activity[]>([])

// 计算属性：获取可用年份
const availableYears = computed(() => {
  const years = [...new Set(activities.value.map(activity => {
    const year = new Date(activity.time).getFullYear()
    return year
  }))]
  return years.sort((a, b) => b - a) // 按年份降序排列
})

// 方法：设置默认年份为最近一年
const setDefaultYear = () => {
  if (availableYears.value.length > 0) {
    activeYear.value = String(availableYears.value[0] ?? new Date().getFullYear())
  }
}

// 方法：获取团建活动列表
const fetchActivities = async () => {
  try {
    loading.value = true
    const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:9001'}/gallery/list`)
    const result = await response.json()

    if (result.code === 200) {
      activities.value = result.data
      // 数据加载完成后设置默认年份
      setDefaultYear()
    } else {
      ElMessage.error('获取团建活动数据失败')
    }
  } catch (error) {
    console.error('获取团建活动数据失败:', error)
    ElMessage.error('网络错误，请检查后端服务是否启动')
  } finally {
    loading.value = false
  }
}

// 计算属性：过滤活动
const filteredActivities = computed(() => {
  let filtered = activities.value.filter(activity => {
    const year = new Date(activity.time).getFullYear()
    return year.toString() === activeYear.value
  })

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(activity =>
      activity.title.toLowerCase().includes(keyword) ||
      activity.summary.toLowerCase().includes(keyword) ||
      activity.type.toLowerCase().includes(keyword)
    )
  }

  return filtered.sort((a, b) => new Date(b.time).getTime() - new Date(a.time).getTime())
})

// 方法：获取活动类型标签样式
const getActivityTypeTag = (type: string) => {
  const typeMap: { [key: string]: string } = {
    '户外活动': 'success',
    '学术活动': 'primary',
    '节日活动': 'warning',
    '体育活动': 'info',
    '庆典活动': 'danger',
    '竞赛活动': 'success',
    '实践活动': 'warning',
    '迎新活动': 'info'
  }
  return typeMap[type] || 'default'
}

// 方法：处理年份选择
const handleYearSelect = (year: string) => {
  activeYear.value = year
}

// 方法：处理搜索
const handleSearch = () => {
  // 搜索逻辑已经在计算属性中实现
}

// 方法：格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 方法：获取图片URL
const getImageUrl = (pictureurl: string) => {
  if (!pictureurl) return 'https://images.unsplash.com/photo-1497366216548-3754b308ceda?w=400'
  if (pictureurl.startsWith('http')) return pictureurl
  return `${import.meta.env.VITE_API_BASE_URL || ''}${pictureurl.startsWith('/') ? '' : '/'}${pictureurl}`
}

// 方法：处理图片加载错误
const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement
  target.src = 'https://images.unsplash.com/photo-1497366216548-3754b308ceda?w=400'
}

// 方法：处理活动点击
const handleActivityClick = (activityId: number) => {
  router.push(`/gallery-detail/${activityId}`)
}

onMounted(() => {
  fetchActivities()
})
</script>

<style scoped>
.gallery-page {
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
  margin-bottom: 20px;
}

.layout-container {
  display: flex;
  gap: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 侧边栏样式 */
.sidebar {
  width: 280px;
  flex-shrink: 0;
}

.search-section {
  margin-bottom: 20px;
}

.nav-menu {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 10px 0;
}

.nav-menu-list {
  border: none;
}

.menu-item-content {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.menu-title {
  flex: 1;
  font-weight: 500;
}

/* 主内容区域样式 */
.main-content {
  flex: 1;
  min-width: 0;
}

.section-header {
  margin-bottom: 30px;
}

.section-title {
  font-size: 1.8rem;
  color: #2c3e50;
  margin-bottom: 8px;
  font-weight: 600;
}

.section-subtitle {
  color: #7f8c8d;
  font-size: 1rem;
}

/* 加载状态样式 */
.loading-container {
  padding: 40px;
}

/* 活动网格样式 */
.activities-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.activity-card-wrapper {
  animation: fadeInUp 0.5s ease-out;
  display: flex;
}

.activity-item {
  border: none;
  border-radius: 12px;
  transition: all 0.3s ease;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
}

.activity-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* 活动图片样式 */
.activity-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.activity-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.activity-item:hover .activity-image img {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
}

.activity-type {
  font-weight: 500;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.9);
}

/* 活动内容样式 */
.activity-content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.activity-header {
  margin-bottom: 16px;
}

.activity-title {
  font-size: 1.3rem;
  color: #2c3e50;
  margin-bottom: 8px;
  font-weight: 600;
  line-height: 1.4;
  /* Fixed height for alignment */
  height: 60px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-meta {
  display: flex;
  gap: 16px;
  font-size: 0.9rem;
  color: #7f8c8d;
  /* Fixed height */
  height: 20px;
}

.activity-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.activity-description {
  margin-bottom: 20px;
  line-height: 1.6;
  color: #5a6c7d;
  flex: 1;
}

.activity-description p {
  /* Fixed height (3 lines) */
  height: 75px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0;
}

/* 活动操作按钮样式 */
.activity-actions {
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
  margin-top: auto;
  text-align: right;
}

.activity-actions .el-button {
  font-weight: 500;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .layout-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    margin-bottom: 20px;
  }

  .activities-grid {
    grid-template-columns: 1fr;
  }

  .activity-meta {
    flex-direction: column;
    gap: 8px;
  }

  .activity-actions {
    text-align: center;
  }
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
