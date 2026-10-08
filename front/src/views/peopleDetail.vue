<template>
  <div class="researcher-detail-page">
    <!-- 返回按钮 -->
    <div class="back-section">
      <el-button type="primary" text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 详情内容 -->
    <div v-else-if="researcher" class="detail-content">
      <el-card class="researcher-card" shadow="never">
        <!-- 头部信息 -->
        <div class="header-section">
          <div class="photo-section">
            <div class="photo-container">
              <img
                v-if="researcher.pictureurl"
                :src="getImageUrl(researcher.pictureurl)"
                :alt="researcher.name"
                class="researcher-photo"
                @error="handleImageError"
              />
              <div v-else class="photo-placeholder">
                <el-icon><User /></el-icon>
                <span>暂无照片</span>
              </div>
            </div>
          </div>

          <div class="basic-info">
            <h1 class="researcher-name">{{ researcher.name }}</h1>
            <div class="identity-badge">{{ getIdentityLabel(researcher.identity) }}</div>
<!--            <p class="introduction">{{ researcher.introduction }}</p>-->

            <div class="contact-info">
              <div class="contact-item">
                <el-icon><Message /></el-icon>
                <span class="email">{{ researcher.email }}</span>
              </div>
              <div class="contact-item">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatEntryTime(researcher.entrytime) }}</span>
              </div>
              <a v-if="externalProfileUrl" class="contact-item profile-link" :href="externalProfileUrl" target="_blank" rel="noopener noreferrer">
                <el-icon><Link /></el-icon>
                <span>{{ researcher.profileLabel || '个人主页' }}</span>
              </a>
            </div>
          </div>
        </div>

        <!-- 研究方向 -->
        <div class="research-section">
          <h2 class="section-title">
            <el-icon><Compass /></el-icon>
            研究方向
          </h2>
          <div class="research-areas">
            <el-tag
              v-for="direction in researchDirections"
              :key="direction"
              type="primary"
              class="research-tag"
            >
              {{ direction }}
            </el-tag>
            <div v-if="researchDirections.length == 0" class="no-research">
              暂无研究方向信息
            </div>
          </div>
        </div>

        <!-- 详细信息 -->
        <div class="detail-section">
          <h2 class="section-title">
            <el-icon><InfoFilled /></el-icon>
            详细信息
          </h2>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="姓名">{{ researcher.name }}</el-descriptions-item>
            <el-descriptions-item label="身份">{{ getIdentityLabel(researcher.identity) }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ researcher.email || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="加入时间">{{ formatEntryTime(researcher.entrytime) }}</el-descriptions-item>
            <el-descriptions-item label="毕业去向" v-if="researcher.identity === 'graduate'">{{ researcher.workplace || '暂无' }}</el-descriptions-item>
<!--            <el-descriptions-item label="研究方向数量" :span="2">{{ researchDirections.length }}</el-descriptions-item>-->
            <el-descriptions-item label="个人简介" :span="2">{{ researcher.introduction || '暂无简介' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 操作按钮 -->
        <div class="action-section">
          <el-button type="primary" @click="sendEmail(researcher)">
            <el-icon><Message /></el-icon>
            联系研究人员
          </el-button>
          <el-button @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回列表
          </el-button>
        </div>

        <div v-if="mentors.length || students.length" class="relationship-section">
          <div v-if="mentors.length" class="relationship-block">
            <h2 class="section-title"><el-icon><UserFilled /></el-icon>导师</h2>
            <div class="relationship-grid">
              <button v-for="member in mentors" :key="member.id" class="relationship-card" @click="viewMember(member.id)">
                <el-avatar :size="48" :src="getImageUrl(member.pictureurl)">{{ member.name?.charAt(0) }}</el-avatar>
                <div><strong>{{ member.name }}</strong><span>{{ getIdentityLabel(member.identity) }}</span></div>
              </button>
            </div>
          </div>
          <div v-if="students.length" class="relationship-block">
            <h2 class="section-title"><el-icon><UserFilled /></el-icon>科研人员（学生）</h2>
            <div class="relationship-grid">
              <button v-for="member in students" :key="member.id" class="relationship-card" @click="viewMember(member.id)">
                <el-avatar :size="48" :src="getImageUrl(member.pictureurl)">{{ member.name?.charAt(0) }}</el-avatar>
                <div><strong>{{ member.name }}</strong><span>{{ getIdentityLabel(member.identity) }}</span></div>
              </button>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-state">
      <el-result icon="error" title="加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchResearcherDetail">重试</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft, Message, Calendar, Compass,
  User, InfoFilled, Link, UserFilled
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Request from "@/utils/request.ts"
import { getApiBaseUrl } from '@/utils/apiConfig'

// 路由相关
const route = useRoute()
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
  profileUrl?: string
  profileLabel?: string
}

// 响应式数据
const loading = ref(true)
const error = ref('')
const researcher = ref<Member | null>(null)
const researchDirections = ref<string[]>([])
const mentors = ref<Member[]>([])
const students = ref<Member[]>([])
const externalProfileUrl = computed(() => {
  const value = researcher.value?.profileUrl?.trim()
  if (!value) return ''
  if (/^https?:\/\//i.test(value)) return value
  return `https://${value.replace(/^\/\//, '')}`
})

// 成员分类映射
const identityMap = {
  'director': '负责人',
  'mentor': '导师',
  'phd': '博士研究生',
  'master': '硕士研究生',
  'graduate': '毕业生'
}

// 方法
const getIdentityLabel = (identity: string) => {
  return identityMap[identity as keyof typeof identityMap] || identity
}

const formatEntryTime = (entrytime: string) => {
  if (!entrytime) return '未知时间'
  return entrytime
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

const goBack = () => {
  router.push('/people')
}

const viewMember = async (id: number) => {
  await router.push(`/researcher/${id}`)
  window.scrollTo({ top: 0, behavior: 'smooth' })
  await fetchResearcherDetail()
}

const sendEmail = (member: Member) => {
  if (member.email) {
    window.location.href = `mailto:${member.email}`
  } else {
    ElMessage.warning('该研究人员暂无邮箱信息')
  }
}

// API调用方法 - 使用新的详情接口
const fetchResearcherDetail = async () => {
  try {
    loading.value = true
    error.value = ''

    const researcherId = route.params.id
    if (!researcherId) {
      throw new Error('研究人员ID不存在')
    }

    // 使用新的详情接口获取完整信息
    const result = await Request(`/member/detail/${researcherId}`)
    if (result.code == 200) {
      // 如果后端返回的是MemberDetailDTO格式
      if (result.data.member) {
        researcher.value = result.data.member
        researchDirections.value = result.data.researchDirections || []
        mentors.value = result.data.mentors || []
        students.value = result.data.students || []
        console.log(result.data)
      } else {
        // 如果后端返回的是Member对象
        researcher.value = result.data
        // 单独获取研究方向信息
        await fetchResearchDirections(researcherId as string)
      }
    } else {
      throw new Error(result.message || '获取研究人员信息失败')
    }
  } catch (err: any) {
    error.value = err.message || '网络错误，请检查后端服务是否启动'
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

// 获取研究方向信息
const fetchResearchDirections = async (memberId: string) => {
  try {
    // 获取成员研究方向关联
    const relationResult = await Request(`/memberdirection/member/${memberId}`)
    if (relationResult.code == 200 && relationResult.data.length > 0) {
      const directionIds = relationResult.data.map((rel: any) => rel.directionId)

      // 获取研究方向详情
      const directionsPromises = directionIds.map((id: number) =>
        Request(`/direction/${id}`)
      )

      const directionsResults = await Promise.all(directionsPromises)
      researchDirections.value = directionsResults
        .filter(result => result.code == 200)
        .map(result => result.data ? result.data.name : '')
        .filter(name => name)
    }
  } catch (error) {
    console.error('获取研究方向信息失败:', error)
  }
}

onMounted(() => {
  fetchResearcherDetail()
})
</script>

<style scoped>
.researcher-detail-page {
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  min-height: 100vh;
}

.back-section {
  margin-bottom: 20px;
}

.loading-state {
  padding: 40px 20px;
}

.researcher-card {
  border-radius: 12px;
  border: none;
  max-width: 1000px;
  margin: 0 auto;
}

/* 头部信息样式 */
.header-section {
  display: flex;
  gap: 30px;
  padding: 30px;
  border-bottom: 1px solid #eee;
}

.photo-section {
  flex-shrink: 0;
}

.photo-container {
  width: 200px;
  height: 250px;
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
}

.researcher-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: white;
  font-size: 1.1rem;
}

.photo-placeholder .el-icon {
  font-size: 4rem;
  margin-bottom: 16px;
  opacity: 0.7;
}

.basic-info {
  flex: 1;
}

.researcher-name {
  font-size: 2.5rem;
  color: #2c3e50;
  margin-bottom: 16px;
  font-weight: 600;
}

.identity-badge {
  display: inline-block;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 1rem;
  font-weight: 500;
  margin-bottom: 20px;
}

.introduction {
  font-size: 1.2rem;
  color: #5a6c7d;
  line-height: 1.6;
  margin-bottom: 25px;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.1rem;
  color: #7f8c8d;
}

.contact-item .el-icon {
  color: #3498db;
}

/* 研究方向样式 */
.research-section {
  padding: 30px;
  border-bottom: 1px solid #eee;
}

.profile-link {
  color: #2563eb;
  text-decoration: none;
}

.relationship-section {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 22px;
  padding: 28px;
  border-top: 1px solid #edf1f7;
}

.relationship-block {
  min-width: 0;
}

.relationship-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.relationship-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e5ecf5;
  border-radius: 12px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: .2s ease;
}

.relationship-card:hover {
  transform: translateY(-2px);
  border-color: #9fc2f7;
  box-shadow: 0 8px 20px rgba(30, 58, 138, .09);
}

.relationship-card strong,
.relationship-card span {
  display: block;
}

.relationship-card span {
  color: #8492a6;
  font-size: 12px;
  margin-top: 3px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.5rem;
  color: #2c3e50;
  margin-bottom: 20px;
  font-weight: 600;
}

.section-title .el-icon {
  color: #667eea;
}

.research-areas {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.research-tag {
  font-size: 1rem;
  padding: 8px 16px;
}

.no-research {
  color: #7f8c8d;
  font-style: italic;
}

/* 详细信息样式 */
.detail-section {
  padding: 30px;
  border-bottom: 1px solid #eee;
}

/* 操作按钮样式 */
.action-section {
  padding: 30px;
  text-align: center;
}

.action-section .el-button {
  margin: 0 10px;
  padding: 12px 24px;
  font-size: 1rem;
}

/* 错误状态样式 */
.error-state {
  padding: 60px 20px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .relationship-section,
  .relationship-grid {
    grid-template-columns: 1fr;
  }
  .researcher-detail-page {
    padding: 10px;
  }

  .header-section {
    flex-direction: column;
    text-align: center;
    gap: 20px;
    padding: 20px;
  }

  .photo-container {
    width: 150px;
    height: 180px;
    margin: 0 auto;
  }

  .researcher-name {
    font-size: 2rem;
  }

  .research-section,
  .detail-section,
  .action-section {
    padding: 20px;
  }

  .action-section .el-button {
    width: 100%;
    margin: 5px 0;
  }
}

/* 动画效果 */
.researcher-card {
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
