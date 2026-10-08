<template>
  <div class="teaching-page">
    <!-- 筛选工具栏 -->
    <div class="filter-toolbar">
      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索讲座标题或主讲人..."
          clearable
          @input="handleSearch"
          style="width: 300px;"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="date-filter-section">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleDateFilter"
          style="width: 320px;"
        />
      </div>

      <div class="filter-actions">
        <el-button type="primary" @click="resetFilters">
          <el-icon><Refresh /></el-icon>
          重置筛选
        </el-button>
      </div>
    </div>

    <!-- 讲座列表 -->
    <div class="lecture-section">
      <div class="section-header">
        <h2 class="section-title">
          讲座列表
          <span class="lecture-count">(共 {{ filteredLectures.length }} 场)</span>
        </h2>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 讲座卡片列表 -->
      <div v-else class="lecture-list">
        <div
          v-for="lecture in filteredLectures"
          :key="lecture.id"
          class="lecture-card"
        >
          <el-card shadow="hover" class="lecture-item">
            <div class="lecture-content">
              <!-- 主讲人图片和基本信息 -->
              <div class="lecture-header">
                <div class="speaker-avatar-square">
                  <img
                    :src="fileUrl(lecture.pictureurl) || '/images/default-speaker.jpg'"
                    :alt="lecture.speaker"
                    @error="handleImageError"
                  />
                </div>
                <div class="lecture-basic-info">
                  <h3 class="lecture-title">{{ lecture.title }}</h3>
                  <div class="speaker-info">
                    <span class="speaker-name">主讲人：{{ lecture.speaker }}</span>
                    <span class="speaker-title">{{ lecture.speakerfrom }}</span>
                  </div>
                </div>
              </div>

              <!-- 讲座详细信息 -->
              <div class="lecture-details">
                <div class="detail-item">
                  <el-icon><Calendar /></el-icon>
                  <span class="detail-label">讲座时间：</span>
                  <span class="detail-value">{{ formatDateTime(lecture.time) }}</span>
                </div>

                <div class="detail-item">
                  <el-icon><Location /></el-icon>
                  <span class="detail-label">讲座地点：</span>
                  <span class="detail-value">{{ lecture.address }}</span>
                </div>

                <div class="detail-item">
                  <el-icon><User /></el-icon>
                  <span class="detail-label">主持人：</span>
                  <span class="detail-value">{{ lecture.host }}</span>
                </div>
              </div>

              <!-- 讲座简介 -->
              <div class="lecture-intro">
                <h4 class="intro-title">讲座简介</h4>
                <p class="intro-content">{{ truncateText(lecture.lectureintroduction) }}</p>
              </div>

              <!-- 主讲人简介 -->
              <div class="speaker-intro" v-if="lecture.speakerintroduction">
                <h4 class="intro-title">主讲人简介</h4>
                <p class="intro-content">{{ truncateText(lecture.speakerintroduction) }}</p>
              </div>

              <div class="lecture-actions">
                <el-button type="primary" plain @click="viewLectureDetail(lecture.id)">
                  查看详情
                  <el-icon class="button-icon"><ArrowRight /></el-icon>
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && filteredLectures.length === 0" class="empty-state">
        <el-empty description="暂无相关讲座" :image-size="100" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Search, Calendar, Location, User, Refresh, ArrowRight
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { getApiBaseUrl } from '@/utils/apiConfig'

// 讲座类型定义（与后端实体类对应）
interface Lecture {
  id: number
  title: string
  speaker: string
  speakerfrom: string
  time: string
  address: string
  host: string
  lectureintroduction: string
  speakerintroduction: string
  pictureurl: string
}

// 响应式数据
const searchKeyword = ref('')
const dateRange = ref<string[]>([])
const loading = ref(true)
const lectures = ref<Lecture[]>([])
const router = useRouter()

// 计算属性：过滤讲座
const filteredLectures = computed(() => {
  let filtered = lectures.value

  // 搜索筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(lecture =>
      lecture.title.toLowerCase().includes(keyword) ||
      lecture.speaker.toLowerCase().includes(keyword) ||
      lecture.host.toLowerCase().includes(keyword) ||
      lecture.speakerfrom.toLowerCase().includes(keyword)
    )
  }

  // 日期范围筛选
  if (dateRange.value && dateRange.value.length === 2) {
    const startDate = dateRange.value[0] || ''
    const endDate = dateRange.value[1] || ''
    filtered = filtered.filter(lecture => {
      const lectureDate = lecture.time.split(' ')[0] || '' // 只取日期部分
      return lectureDate >= startDate && lectureDate <= endDate
    })
  }

  // 按时间降序排列（最新的在前面）
  return [...filtered].sort((a, b) => new Date(b.time).getTime() - new Date(a.time).getTime())
})

const truncateText = (value: string, limit = 500) => {
  const text = (value || '').trim()
  if (!text) return '暂无简介'
  const characters = Array.from(text)
  return characters.length > limit ? `${characters.slice(0, limit).join('')}…` : text
}

const fileUrl = (value: string) => {
  if (!value) return ''
  if (/^https?:\/\//i.test(value)) return value
  return `${getApiBaseUrl()}${value.startsWith('/') ? '' : '/'}${value}`
}

const viewLectureDetail = (id: number) => router.push(`/lecture/${id}`)

// 方法：格式化日期时间
const formatDateTime = (dateTimeString: string) => {
  const date = new Date(dateTimeString)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}年${month}月${day}日 ${hours}:${minutes}`
}

// 方法：处理搜索
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    searchLectures(searchKeyword.value.trim())
  } else {
    fetchLectures()
  }
}

// 方法：处理日期筛选
const handleDateFilter = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    filterLecturesByTime()
  } else {
    fetchLectures()
  }
}

// 方法：重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  dateRange.value = []
  fetchLectures()
  ElMessage.success('筛选条件已重置')
}

// 方法：处理图片加载错误
const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement
  target.src = '/images/default-speaker.jpg'
}

// API调用方法
// 获取所有讲座列表
const fetchLectures = async () => {
  loading.value = true
  try {
    const response = await request.get('/lecture/list')

    if (response.code == 200) {
      lectures.value = response.data || []
    } else {
      ElMessage.error('获取讲座列表失败: ' + response.msg)
      lectures.value = []
    }
  } catch (error) {
    console.error('获取讲座列表失败:', error)
    ElMessage.error('网络错误，获取讲座列表失败')
    lectures.value = []
  } finally {
    loading.value = false
  }
}

// 搜索讲座
const searchLectures = async (keyword: string) => {
  loading.value = true
  try {
    const response = await request.get(`/lecture/search?keyword=${encodeURIComponent(keyword)}`)

    if (response.code == 200) {
      lectures.value = response.data || []
    } else {
      ElMessage.error('搜索失败: ' + response.msg)
      lectures.value = []
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('网络错误，搜索失败')
    lectures.value = []
  } finally {
    loading.value = false
  }
}

// 按时间筛选讲座
const filterLecturesByTime = async () => {
  loading.value = true
  try {
    const [startDate, endDate] = dateRange.value
    const startTime = `${startDate} 00:00:00`
    const endTime = `${endDate} 23:59:59`

    const response = await request.get(`/lecture/filter/time?startTime=${encodeURIComponent(startTime)}&endTime=${encodeURIComponent(endTime)}`)

    if (response.code == 200) {
      lectures.value = response.data || []
    } else {
      ElMessage.error('筛选失败: ' + response.msg)
      lectures.value = []
    }
  } catch (error) {
    console.error('筛选失败:', error)
    ElMessage.error('网络错误，筛选失败')
    lectures.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchLectures()
})
</script>

<style scoped>
.teaching-page {
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
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

/* 筛选工具栏 */
.filter-toolbar {
  width: min(100%, 1200px);
  margin: 0 auto 30px;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  flex-wrap: wrap;
}

.search-section, .date-filter-section {
  flex-shrink: 0;
}

.filter-actions {
  margin-left: auto;
}

/* 讲座列表区域 */
.lecture-section {
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  margin-bottom: 30px;
  text-align: center;
}

.section-title {
  font-size: 1.8rem;
  color: #2c3e50;
  margin-bottom: 8px;
  font-weight: 600;
}

.lecture-count {
  font-size: 1rem;
  color: #7f8c8d;
  font-weight: normal;
}

/* 加载状态 */
.loading-state {
  padding: 40px 0;
}

/* 讲座列表 */
.lecture-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.lecture-card {
  animation: fadeInUp 0.5s ease-out;
}

.lecture-item {
  border: none;
  border-radius: 16px;
  transition: all 0.3s ease;
  overflow: hidden;
}

.lecture-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.lecture-content {
  padding: 24px;
}

/* 讲座头部信息 */
.lecture-header {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

/* 方形头像样式 */
.speaker-avatar-square {
  width: 100px;
  height: 100px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
  border: 3px solid #e8f4fd;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.speaker-avatar-square img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.lecture-basic-info {
  flex: 1;
}

.lecture-title {
  font-size: 1.4rem;
  color: #2c3e50;
  margin: 0 0 12px 0;
  font-weight: 600;
  line-height: 1.4;
}

.speaker-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.speaker-name {
  font-size: 1.1rem;
  color: #1890ff;
  font-weight: 500;
}

.speaker-title {
  font-size: 0.95rem;
  color: #7f8c8d;
}

/* 讲座详细信息 */
.lecture-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-item .el-icon {
  color: #1890ff;
  font-size: 1.1rem;
}

.detail-label {
  font-weight: 500;
  color: #5a6c7d;
  min-width: 70px;
}

.detail-value {
  color: #2c3e50;
  font-weight: 500;
}

/* 简介区域 */
.lecture-intro, .speaker-intro {
  margin-bottom: 20px;
}

.intro-title {
  font-size: 1.1rem;
  color: #2c3e50;
  margin: 0 0 12px 0;
  font-weight: 600;
  padding-bottom: 8px;
  border-bottom: 2px solid #1890ff;
}

.intro-content {
  font-size: 1rem;
  line-height: 1.6;
  color: #5a6c7d;
  margin: 0;
  text-align: justify;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.lecture-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
  padding-top: 18px;
  border-top: 1px solid #eef2f7;
}

.lecture-actions .el-button {
  min-width: 126px;
  border-radius: 9px;
}

.button-icon {
  margin-left: 6px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .filter-actions {
    margin-left: 0;
    text-align: center;
  }

  .lecture-header {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .lecture-details {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .detail-item {
    justify-content: flex-start;
  }

  .search-section .el-input,
  .date-filter-section .el-date-picker {
    width: 100% !important;
  }

  .speaker-avatar-square {
    width: 80px;
    height: 80px;
    margin: 0 auto;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 2rem;
  }

  .section-title {
    font-size: 1.5rem;
  }

  .lecture-content {
    padding: 16px;
  }

  .speaker-avatar-square {
    width: 70px;
    height: 70px;
  }
}
</style>
