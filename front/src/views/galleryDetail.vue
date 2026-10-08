<template>
  <div class="gallery-detail-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button
        type="primary"
        link
        @click="handleBack"
        class="back-button"
      >
        <el-icon><ArrowLeft /></el-icon>
        返回团建列表
      </el-button>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content" v-if="activity">
      <!-- 活动封面图 -->
      <div class="activity-cover">
        <el-image
          :src="getImageUrl(activity.pictureurl)"
          :alt="activity.title"
          fit="cover"
          class="cover-image"
          @error="handleImageError"
        >
          <template #error>
            <div class="image-error">
              <el-icon><Picture /></el-icon>
              <span>图片加载失败</span>
            </div>
          </template>
        </el-image>
        <div class="cover-overlay">
          <div class="cover-content">
            <h1 class="activity-title">{{ activity.title }}</h1>
            <div class="activity-meta">
              <el-tag :type="getActivityTypeTag(activity.type)" size="large">
                {{ activity.type }}
              </el-tag>
              <div class="meta-info">
                <span class="date-info">
                  <el-icon><Calendar /></el-icon>
                  {{ formatDate(activity.time) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 活动详情内容 -->
      <div class="activity-detail">
        <el-card class="content-card">
<!--          <template #header>-->
<!--            <div class="card-header">-->
<!--              <h2>活动内容</h2>-->
<!--            </div>-->
<!--          </template>-->

          <!-- 富文本内容区域 -->
          <div class="rich-content">
            <div class="content-section" v-html="activity.content"></div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 未找到活动 -->
    <div v-else class="not-found-container">
      <el-result icon="warning" title="活动未找到" sub-title="请检查活动ID是否正确">
        <template #extra>
          <el-button type="primary" @click="handleBack">返回团建列表</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Calendar,
  Picture
} from '@element-plus/icons-vue'

// 活动类型定义（与后端实体类对应）
interface Activity {
  id: number
  title: string
  type: string
  time: string
  summary: string
  content: string
  pictureurl: string
}

// 获取路由实例
const router = useRouter()
const route = useRoute()

// 响应式数据
const activity = ref<Activity | null>(null)
const loading = ref(false)

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
  if (!pictureurl) return 'https://images.unsplash.com/photo-1497366216548-3754b308ceda?w=800'
  if (pictureurl.startsWith('http')) return pictureurl
  return `${import.meta.env.VITE_API_BASE_URL || ''}${pictureurl.startsWith('/') ? '' : '/'}${pictureurl}`
}

// 方法：处理返回
const handleBack = () => {
  router.push('/gallery')
}

// 方法：处理图片加载错误
const handleImageError = () => {
  // 图片加载错误处理
}

// 方法：获取活动详情
const fetchActivityDetail = async (id: number) => {
  try {
    loading.value = true
    const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:9001'}/gallery/detail/${id}`)
    const result = await response.json()

    if (result.code === 200) {
      activity.value = result.data
    } else {
      ElMessage.error('获取活动详情失败')
      activity.value = null
    }
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('网络错误，请检查后端服务是否启动')
    activity.value = null
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  const activityId = parseInt(route.params.id as string)
  if (isNaN(activityId)) {
    ElMessage.error('活动ID格式错误')
    router.push('/gallery')
    return
  }

  fetchActivityDetail(activityId)
})
</script>

<style scoped>
.gallery-detail-page {
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.back-button {
  color: #409eff;
  font-weight: 500;
}

/* 活动封面样式 */
.activity-cover {
  position: relative;
  height: 400px;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  max-width: 1000px;
  margin-left: auto;
  margin-right: auto;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0.2) 0%,
    rgba(0, 0, 0, 0.6) 100%
  );
  display: flex;
  align-items: flex-end;
  padding: 40px;
}

.cover-content {
  color: white;
  width: 100%;
}

.activity-title {
  font-size: 2.5rem;
  font-weight: 600;
  margin-bottom: 16px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.activity-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-info {
  display: flex;
  gap: 20px;
  color: rgba(255, 255, 255, 0.9);
}

.meta-info span {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 1rem;
}

/* 活动详情样式 */
.activity-detail {
  max-width: 1000px;
  margin: 0 auto;
}

.content-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.card-header {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header h2 {
  margin: 0;
  color: #2c3e50;
  font-weight: 600;
}

/* 富文本内容样式 */
.rich-content {
  padding: 20px;
}

.content-section {
  line-height: 1.8;
  color: #5a6c7d;
  font-size: 1rem;
}

/* 富文本内容样式增强 */
.content-section :deep(h1),
.content-section :deep(h2),
.content-section :deep(h3) {
  color: #2c3e50;
  margin-top: 1.5em;
  margin-bottom: 0.5em;
}

.content-section :deep(p) {
  margin-bottom: 1em;
}

.content-section :deep(ul),
.content-section :deep(ol) {
  margin: 1em 0;
  padding-left: 2em;
}

.content-section :deep(li) {
  margin-bottom: 0.5em;
}

.content-section :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding-left: 1em;
  margin: 1em 0;
  color: #666;
  font-style: italic;
}

.content-section :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 1em 0;
}

.content-section :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1em 0;
}

.content-section :deep(th),
.content-section :deep(td) {
  border: 1px solid #ddd;
  padding: 0.5em;
  text-align: left;
}

.content-section :deep(th) {
  background-color: #f5f7fa;
  font-weight: 600;
}

/* 加载状态样式 */
.loading-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px;
}

/* 未找到活动样式 */
.not-found-container {
  max-width: 600px;
  margin: 100px auto;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .gallery-detail-page {
    padding: 10px;
  }

  .activity-cover {
    height: 300px;
    max-width: calc(100% - 20px);
    margin-left: 10px;
    margin-right: 10px;
  }

  .cover-overlay {
    padding: 20px;
  }

  .activity-title {
    font-size: 1.8rem;
  }

  .activity-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .meta-info {
    flex-direction: column;
    gap: 8px;
  }

  .content-section :deep(table) {
    font-size: 0.9em;
  }
}

/* 动画效果 */
.activity-cover,
.content-card {
  animation: fadeInUp 0.6s ease-out;
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
