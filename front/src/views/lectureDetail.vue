<template>
  <div class="lecture-detail-page">
    <main class="detail-shell">
      <el-button class="back-button" :icon="ArrowLeft" @click="router.push('/teaching')">返回讲座列表</el-button>

      <el-skeleton v-if="loading" :rows="10" animated class="detail-card" />
      <el-empty v-else-if="!lecture" description="讲座信息不存在或已被删除" :image-size="130">
        <el-button type="primary" @click="router.push('/teaching')">返回列表</el-button>
      </el-empty>

      <article v-else class="detail-card">
        <section class="lecture-hero">
          <div class="speaker-photo">
            <img :src="fileUrl(lecture.pictureurl) || '/images/default-speaker.jpg'" :alt="lecture.speaker" @error="handleImageError" />
          </div>
          <div class="hero-content">
            <span class="eyebrow">ACADEMIC LECTURE</span>
            <h1>{{ lecture.title }}</h1>
            <div class="speaker-line">
              <strong>{{ lecture.speaker }}</strong>
              <span>{{ lecture.speakerfrom || '单位信息暂未填写' }}</span>
            </div>
          </div>
        </section>

        <section class="meta-grid">
          <div class="meta-item">
            <el-icon><Calendar /></el-icon>
            <div><span>讲座时间</span><strong>{{ formatDateTime(lecture.time) }}</strong></div>
          </div>
          <div class="meta-item">
            <el-icon><Location /></el-icon>
            <div><span>讲座地点</span><strong>{{ lecture.address || '待定' }}</strong></div>
          </div>
          <div class="meta-item">
            <el-icon><User /></el-icon>
            <div><span>主持人</span><strong>{{ lecture.host || '待定' }}</strong></div>
          </div>
        </section>

        <section class="full-introduction">
          <div class="section-heading"><span>01</span><h2>讲座简介</h2></div>
          <p>{{ lecture.lectureintroduction || '暂无讲座简介' }}</p>
        </section>

        <section class="full-introduction speaker-section">
          <div class="section-heading"><span>02</span><h2>主讲人简介</h2></div>
          <p>{{ lecture.speakerintroduction || '暂无主讲人简介' }}</p>
        </section>
      </article>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Calendar, Location, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { getApiBaseUrl } from '@/utils/apiConfig'

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

const route = useRoute()
const router = useRouter()
const lecture = ref<Lecture | null>(null)
const loading = ref(true)

const fileUrl = (value: string) => {
  if (!value) return ''
  if (/^https?:\/\//i.test(value)) return value
  return `${getApiBaseUrl()}${value.startsWith('/') ? '' : '/'}${value}`
}

const formatDateTime = (value: string) => {
  if (!value) return '时间待定'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}年${month}月${day}日 ${hours}:${minutes}`
}

const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement
  if (!target.src.endsWith('/images/default-speaker.jpg')) target.src = '/images/default-speaker.jpg'
}

const loadLecture = async () => {
  loading.value = true
  try {
    const response = await request.get(`/lecture/detail/${route.params.id}`)
    if (response.code == 200) lecture.value = response.data
    else lecture.value = null
  } catch (error) {
    lecture.value = null
    ElMessage.error('讲座详情加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(loadLecture)
</script>

<style scoped>
.lecture-detail-page { min-height: 75vh; padding: 34px 0 70px; background: linear-gradient(180deg, #f4f8ff 0%, #fff 54%); }
.detail-shell { width: min(90%, 1180px); margin: 0 auto; }
.back-button { margin-bottom: 20px; color: #315d9f; border-color: #cbdaf0; background: rgba(255, 255, 255, .85); }
.detail-card, .detail-shell > :deep(.el-skeleton) { overflow: hidden; border: 1px solid #e2eaf5; border-radius: 22px; background: #fff; box-shadow: 0 20px 52px rgba(31, 70, 126, .1); }
.detail-shell > :deep(.el-skeleton) { padding: 36px; }
.lecture-hero { display: grid; grid-template-columns: 220px minmax(0, 1fr); gap: 34px; align-items: center; padding: 38px 42px; color: #fff; background: linear-gradient(130deg, #112f6b 0%, #235db0 58%, #3e81d8 100%); }
.speaker-photo { width: 220px; height: 220px; overflow: hidden; border: 5px solid rgba(255, 255, 255, .2); border-radius: 18px; background: rgba(255, 255, 255, .12); box-shadow: 0 16px 34px rgba(5, 22, 52, .25); }
.speaker-photo img { width: 100%; height: 100%; object-fit: cover; }
.eyebrow { display: inline-block; margin-bottom: 15px; color: #c9dcff; font-size: 12px; font-weight: 700; letter-spacing: 2.5px; }
.hero-content h1 { margin: 0; max-width: 780px; font-size: clamp(27px, 3vw, 42px); line-height: 1.35; letter-spacing: .5px; }
.speaker-line { display: flex; flex-wrap: wrap; align-items: center; gap: 12px 20px; margin-top: 24px; }
.speaker-line strong { font-size: 20px; }
.speaker-line span { padding-left: 20px; border-left: 1px solid rgba(255, 255, 255, .4); color: #dfebff; }
.meta-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 1px; background: #e9eff8; border-bottom: 1px solid #e3eaf4; }
.meta-item { display: flex; align-items: center; gap: 14px; min-width: 0; padding: 24px 28px; background: #f9fbff; }
.meta-item > .el-icon { flex: 0 0 auto; width: 42px; height: 42px; color: #2563af; border-radius: 12px; background: #e7f0ff; font-size: 20px; }
.meta-item div { min-width: 0; }
.meta-item span, .meta-item strong { display: block; }
.meta-item span { margin-bottom: 5px; color: #8190a5; font-size: 12px; }
.meta-item strong { overflow-wrap: anywhere; color: #253a59; font-size: 15px; line-height: 1.5; }
.full-introduction { padding: 42px 48px 10px; }
.speaker-section { padding-bottom: 48px; }
.section-heading { display: flex; align-items: center; gap: 14px; margin-bottom: 20px; }
.section-heading span { display: grid; place-items: center; width: 38px; height: 38px; color: #fff; border-radius: 11px; background: linear-gradient(135deg, #1f56a6, #4a8ce0); font-size: 13px; font-weight: 700; }
.section-heading h2 { margin: 0; color: #20395f; font-size: 24px; }
.full-introduction p { margin: 0; color: #52657e; font-size: 16px; line-height: 2; text-align: justify; white-space: pre-wrap; overflow-wrap: anywhere; }

@media (max-width: 760px) {
  .detail-shell { width: min(94%, 680px); }
  .lecture-hero { grid-template-columns: 1fr; gap: 24px; padding: 28px 24px; text-align: center; }
  .speaker-photo { width: 150px; height: 150px; margin: 0 auto; }
  .speaker-line { justify-content: center; }
  .meta-grid { grid-template-columns: 1fr; }
  .full-introduction { padding: 32px 24px 4px; }
  .speaker-section { padding-bottom: 36px; }
}

@media (max-width: 480px) {
  .speaker-line { flex-direction: column; }
  .speaker-line span { padding-left: 0; border-left: 0; }
  .meta-item { padding: 20px; }
}
</style>
