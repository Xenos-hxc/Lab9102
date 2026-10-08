<template>
  <div class="news-page">
    <div class="layout-container">
      <aside class="sidebar">
        <div class="search-section">
          <el-input v-model="searchKeyword" placeholder="搜索新闻标题或内容..." clearable>
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="nav-menu">
          <el-menu :default-active="activeYear" class="nav-menu-list" @select="handleYearSelect">
            <el-menu-item index="all"><div class="menu-item-content"><el-icon><List /></el-icon><span class="menu-title">全部新闻</span></div></el-menu-item>
            <el-menu-item v-for="year in availableYears" :key="year" :index="String(year)">
              <div class="menu-item-content"><el-icon><Calendar /></el-icon><span class="menu-title">{{ year }}年</span></div>
            </el-menu-item>
          </el-menu>
        </div>
      </aside>

      <main ref="allAnchor" class="main-content">
        <div v-if="loading" class="loading-container"><el-skeleton :rows="10" animated /></div>
        <div v-else class="news-stream">
          <section v-for="year in availableYears" :key="year" :ref="element => setYearSection(element, year)" class="year-section" :data-year="year">
            <header class="year-heading">
              <h2>{{ year }} 年</h2>
              <span>{{ newsByYear(year).length }} 条</span>
            </header>
            <div v-if="newsByYear(year).length" class="news-grid">
              <article v-for="news in newsByYear(year)" :key="news.id" class="news-card">
                <el-card shadow="hover" class="news-item">
                  <div class="news-content">
                    <div v-if="news.pictureurl" class="news-image"><img :src="getImageUrl(news.pictureurl)" :alt="news.title" @error="handleImageError" /></div>
                    <div class="news-header">
                      <div class="news-meta"><el-tag class="news-category" :type="getCategoryTag(news.type)">{{ news.type }}</el-tag><span class="news-date"><el-icon><Clock /></el-icon>{{ formatDate(news.time) }}</span></div>
                      <h3 class="news-title">{{ news.title }}</h3>
                    </div>
                    <div class="news-body"><p class="news-summary">{{ news.summary }}</p></div>
                    <div class="news-actions"><el-button type="primary" text @click="viewNewsDetail(news)"><el-icon><View /></el-icon>查看详情</el-button></div>
                  </div>
                </el-card>
              </article>
            </div>
            <el-empty v-else :description="`${year} 年暂无相关新闻`" :image-size="90" class="year-empty" />
          </section>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Calendar, Clock, List, View } from '@element-plus/icons-vue'
import { getApiBaseUrl } from '@/utils/apiConfig'

interface News { id: number; title: string; type: string; time: string; summary: string; pictureurl: string }
const router = useRouter()
const activeYear = ref('all')
const searchKeyword = ref('')
const loading = ref(false)
const newsList = ref<News[]>([])
const allAnchor = ref<HTMLElement | null>(null)
const yearSections = new Map<number, HTMLElement>()
let scrollFrame = 0

const availableYears = computed(() => {
  const years = [...new Set(newsList.value.map(item => new Date(item.time).getFullYear()).filter(Boolean))]
  const top = Math.max(new Date().getFullYear(), ...years)
  const bottom = Math.min(top, ...(years.length ? years : [top]))
  return Array.from({ length: top - bottom + 1 }, (_, index) => top - index)
})
const searchedNews = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  return newsList.value.filter(item => !keyword || [item.title, item.summary, item.type].some(value => value?.toLowerCase().includes(keyword))).slice().sort((a, b) => new Date(b.time).getTime() - new Date(a.time).getTime())
})
const newsByYear = (year: number) => searchedNews.value.filter(item => new Date(item.time).getFullYear() === year)
const setYearSection = (element: unknown, year: number) => {
  if (element instanceof HTMLElement) yearSections.set(year, element)
  else yearSections.delete(year)
}
const updateActiveYear = () => {
  scrollFrame = 0
  if (allAnchor.value && allAnchor.value.getBoundingClientRect().top > 90) { activeYear.value = 'all'; return }
  const visibleYear = availableYears.value.find(year => (yearSections.get(year)?.getBoundingClientRect().bottom || 0) > 150)
  if (visibleYear) activeYear.value = String(visibleYear)
}
const handleScroll = () => { if (!scrollFrame) scrollFrame = window.requestAnimationFrame(updateActiveYear) }
const handleYearSelect = async (year: string) => {
  activeYear.value = year
  await nextTick()
  if (year === 'all') document.querySelector('.news-page')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  else yearSections.get(Number(year))?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}
const getCategoryTag = (type: string) => ({
  '科研成果': 'success', '学术活动': 'primary', '项目动态': 'warning', '荣誉奖项': 'danger', '合作交流': 'info',
  '实验室活动': 'success', '团队建设': 'success', '学术交流': 'primary', '文体活动': 'warning', '外出考察': 'info',
  '技术突破': 'warning', '实验室动态': 'info'
}[type] || 'default')
const formatDate = (value: string) => new Date(value).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
const getImageUrl = (value: string) => {
  if (!value) return ''
  if (/^https?:\/\//i.test(value)) return value
  return `${getApiBaseUrl()}${value.startsWith('/') ? '' : '/'}${value}`
}
const handleImageError = (event: Event) => { (event.target as HTMLImageElement).style.display = 'none' }
const viewNewsDetail = (news: News) => router.push(`/news-detail/${news.id}`)
const fetchNewsList = async () => {
  loading.value = true
  try {
    const response = await fetch(`${getApiBaseUrl()}/news/list`)
    const result = await response.json()
    if (result.code === 200) { newsList.value = result.data || []; await nextTick(); updateActiveYear() }
    else ElMessage.error('获取新闻数据失败')
  } catch (error) {
    console.error('获取新闻数据失败:', error)
    ElMessage.error('网络错误，请检查后端服务是否启动')
  } finally { loading.value = false }
}
onMounted(() => { window.addEventListener('scroll', handleScroll, { passive: true }); fetchNewsList() })
onUnmounted(() => { window.removeEventListener('scroll', handleScroll); if (scrollFrame) window.cancelAnimationFrame(scrollFrame) })
</script>

<style scoped>
.news-page { min-height: 100vh; padding: 24px 0 60px; background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%); }
.layout-container { display: grid; grid-template-columns: 240px minmax(0, 1fr); gap: 26px; width: min(94%, 1600px); margin: 0 auto; }
.sidebar { position: sticky; top: 18px; align-self: start; min-width: 0; }
.search-section { margin-bottom: 16px; }
.nav-menu { overflow: hidden; padding: 8px 0; background: #fff; border-radius: 14px; box-shadow: 0 8px 28px rgba(30, 58, 138, .09); }
.nav-menu-list { max-height: calc(100vh - 110px); overflow-y: auto; border: none; }
.nav-menu-list :deep(.el-menu-item) { height: 48px; margin: 3px 8px; border-radius: 9px; }
.menu-item-content { display: flex; align-items: center; gap: 12px; width: 100%; }
.menu-title { flex: 1; font-weight: 500; }
.main-content { min-width: 0; }
.loading-container { padding: 40px; }
.news-stream { display: flex; flex-direction: column; gap: 34px; }
.year-section { scroll-margin-top: 20px; }
.year-heading { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 18px; padding: 17px 20px; color: #1c3154; border: 1px solid #dfe8f5; border-left: 5px solid #2b63b8; border-radius: 12px; background: rgba(255, 255, 255, .92); box-shadow: 0 6px 18px rgba(44, 62, 80, .06); }
.year-heading h2 { margin: 0; font-size: 25px; }
.year-heading > span { color: #73839a; }
.news-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 20px; }
.news-card { min-width: 0; animation: fadeInUp .45s ease-out; }
.news-item { height: 100%; overflow: hidden; border: none; border-radius: 14px; transition: .28s ease; }
.news-item:hover { transform: translateY(-4px); box-shadow: 0 12px 30px rgba(27, 55, 99, .14); }
.news-content { display: flex; flex-direction: column; height: 100%; padding: 18px; }
.news-image { width: 100%; height: 180px; margin-bottom: 16px; overflow: hidden; border-radius: 9px; background: #edf2f7; }
.news-image img { width: 100%; height: 100%; object-fit: cover; transition: transform .35s ease; }
.news-item:hover .news-image img { transform: scale(1.04); }
.news-header { margin-bottom: 14px; }
.news-meta { display: flex; align-items: center; gap: 11px; margin-bottom: 11px; }
.news-date { display: flex; align-items: center; gap: 4px; color: #7f8c8d; font-size: .9rem; }
.news-title { display: -webkit-box; overflow: hidden; margin: 0; color: #263b5e; font-size: 1.16rem; font-weight: 600; line-height: 1.45; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.news-body { flex: 1; margin-bottom: 14px; }
.news-summary { display: -webkit-box; overflow: hidden; margin: 0; color: #5f7189; line-height: 1.65; -webkit-line-clamp: 3; -webkit-box-orient: vertical; }
.news-actions { padding-top: 12px; border-top: 1px solid #edf1f5; }
.year-empty { padding: 28px 0; }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(18px); } to { opacity: 1; transform: translateY(0); } }
@media (max-width: 1180px) { .news-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 760px) {
  .layout-container { grid-template-columns: 1fr; width: min(94%, 680px); }
  .sidebar { position: static; }
  .nav-menu-list { display: flex; overflow-x: auto; }
  .nav-menu-list :deep(.el-menu-item) { flex: 0 0 auto; }
  .news-grid { grid-template-columns: 1fr; }
}
</style>
