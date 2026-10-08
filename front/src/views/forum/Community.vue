<template>
  <div class="community-page">
    <section class="community-hero">
      <div class="hero-decoration hero-decoration-one"></div>
      <div class="hero-decoration hero-decoration-two"></div>
      <div class="hero-inner">
        <div class="hero-copy">
          <div class="hero-eyebrow">
            <el-icon><Reading /></el-icon>
            ISAC KNOWLEDGE COMMUNITY
          </div>
          <h1>研学社区</h1>
          <p>记录思考，沉淀方法，共享每一次科研与学习中的真实收获。</p>
          <div class="hero-actions">
            <el-button type="warning" size="large" @click="startWriting">
              <el-icon><EditPen /></el-icon>
              发表研学心得
            </el-button>
            <el-button v-if="username" class="outline-button" size="large" @click="router.push('/community/mine')">
              <el-icon><Notebook /></el-icon>
              我的文章
            </el-button>
          </div>
        </div>
        <div class="hero-stats">
          <div class="hero-stat">
            <strong>{{ total }}</strong>
            <span>篇知识分享</span>
          </div>
          <div class="hero-stat">
            <strong>{{ overview.contributors.length }}</strong>
            <span>位分享成员</span>
          </div>
          <div class="hero-stat">
            <strong>{{ overview.categories.length }}</strong>
            <span>个研学方向</span>
          </div>
        </div>
      </div>
    </section>

    <div class="community-container">
      <section class="search-panel">
        <div class="search-box">
          <el-input
            v-model="keywordInput"
            size="large"
            clearable
            placeholder="搜索文章标题、作者或标签"
            @keyup.enter="applySearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" size="large" @click="applySearch">搜索</el-button>
        </div>
        <div class="category-row">
          <button
            class="category-chip"
            :class="{ active: selectedCategory === undefined }"
            @click="selectCategory(undefined)"
          >
            全部分享
          </button>
          <button
            v-for="category in overview.categories"
            :key="category.id"
            class="category-chip"
            :class="{ active: selectedCategory === category.id }"
            @click="selectCategory(category.id)"
          >
            {{ category.name }}
            <span>{{ category.postCount || 0 }}</span>
          </button>
        </div>
      </section>

      <div class="content-grid">
        <main class="article-column">
          <div class="list-toolbar">
            <div>
              <h2>{{ activeCategoryName }}</h2>
              <span>共 {{ total }} 篇内容</span>
            </div>
            <el-radio-group v-model="sort" size="small" @change="refreshFromFirstPage">
              <el-radio-button value="latest">最新</el-radio-button>
              <el-radio-button value="popular">热门</el-radio-button>
              <el-radio-button value="favorite">收藏最多</el-radio-button>
              <el-radio-button value="featured">精选</el-radio-button>
            </el-radio-group>
          </div>

          <div v-loading="loading" class="article-list">
            <article
              v-for="post in posts"
              :key="post.id"
              class="article-card"
              @click="openPost(post.id)"
            >
              <div class="article-content">
                <div class="article-badges">
                  <span v-if="post.isTop" class="badge top">置顶</span>
                  <span v-if="post.isFeatured" class="badge featured">精选</span>
                  <span class="category-label">{{ post.categoryName || '研学分享' }}</span>
                  <span v-if="post.visibility === 'MEMBER'" class="member-only">
                    <el-icon><Lock /></el-icon>
                    仅成员
                  </span>
                </div>
                <h3>{{ post.title }}</h3>
                <p class="summary">{{ post.summary || '作者暂未填写摘要，点击查看完整内容。' }}</p>
                <div class="tag-list" v-if="post.tags?.length">
                  <span v-for="tag in post.tags.slice(0, 4)" :key="tag"># {{ tag }}</span>
                </div>
                <div class="article-footer">
                  <div class="author">
                    <el-avatar :size="30" :src="post.authorAvatar || defaultAvatar" />
                    <strong>{{ post.authorName || '实验室成员' }}</strong>
                    <span>{{ identityLabel(post.authorIdentity) }}</span>
                  </div>
                  <div class="article-meta">
                    <span>{{ formatDate(post.publishedAt || post.updatedAt) }}</span>
                    <span><el-icon><View /></el-icon>{{ post.viewCount || 0 }}</span>
                    <span><el-icon><Star /></el-icon>{{ post.favoriteCount || 0 }}</span>
                    <span><el-icon><Pointer /></el-icon>{{ post.likeCount || 0 }}</span>
                  </div>
                </div>
              </div>
              <div v-if="post.coverUrl" class="article-cover">
                <el-image :src="post.coverUrl" fit="cover" lazy>
                  <template #error>
                    <div class="cover-fallback">{{ (post.categoryName || '研')[0] }}</div>
                  </template>
                </el-image>
              </div>
              <div v-else class="article-cover cover-fallback">
                {{ (post.categoryName || '研')[0] }}
              </div>
            </article>

            <el-empty
              v-if="!loading && posts.length === 0"
              description="这个分类还没有文章，欢迎成为第一位分享者"
              :image-size="130"
            >
              <el-button type="primary" @click="startWriting">发表文章</el-button>
            </el-empty>
          </div>

          <div class="pagination-wrap" v-if="total > pageSize">
            <el-pagination
              v-model:current-page="pageNum"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              background
              @current-change="fetchPosts"
            />
          </div>
        </main>

        <aside class="side-column">
          <section class="side-card about-card">
            <div class="side-title">
              <el-icon><Collection /></el-icon>
              <h3>关于研学社区</h3>
            </div>
            <p>面向实验室成员的知识沉淀空间，分享文献阅读、实验复盘、科研方法和工具实践。</p>
            <div class="community-rules">
              <span><el-icon><CircleCheck /></el-icon>真实记录</span>
              <span><el-icon><CircleCheck /></el-icon>尊重原创</span>
              <span><el-icon><CircleCheck /></el-icon>审核发布</span>
            </div>
          </section>

          <section class="side-card" v-if="overview.featured.length">
            <div class="side-title">
              <el-icon><Medal /></el-icon>
              <h3>精选阅读</h3>
            </div>
            <div
              v-for="(post, index) in overview.featured"
              :key="post.id"
              class="featured-item"
              @click="openPost(post.id)"
            >
              <span>{{ String(index + 1).padStart(2, '0') }}</span>
              <div>
                <strong>{{ post.title }}</strong>
                <small>{{ post.authorName }} · {{ post.viewCount || 0 }} 阅读</small>
              </div>
            </div>
          </section>

          <section class="side-card" v-if="overview.contributors.length">
            <div class="side-title">
              <el-icon><UserFilled /></el-icon>
              <h3>分享成员</h3>
            </div>
            <div class="contributor-grid">
              <div v-for="person in overview.contributors" :key="person.id" class="contributor">
                <el-avatar :size="42" :src="person.avatar || defaultAvatar" />
                <strong>{{ person.name }}</strong>
                <span>{{ person.postCount }} 篇</span>
              </div>
            </div>
          </section>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  CircleCheck, Collection, EditPen, Lock, Medal, Notebook,
  Pointer, Reading, Search, Star, UserFilled, View
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { getForumUsername } from '@/utils/forumUser'
import defaultAvatar from '@/assets/user.png'

const router = useRouter()
const username = ref(getForumUsername())
const loading = ref(false)
const keywordInput = ref('')
const keyword = ref('')
const selectedCategory = ref<number | undefined>()
const sort = ref('latest')
const posts = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 8
const overview = reactive({
  categories: [] as any[],
  featured: [] as any[],
  contributors: [] as any[]
})

const activeCategoryName = computed(() => {
  if (!selectedCategory.value) return keyword.value ? `“${keyword.value}”的搜索结果` : '全部研学分享'
  return overview.categories.find(item => item.id === selectedCategory.value)?.name || '分类文章'
})

const fetchOverview = async () => {
  try {
    const response = await request.get('/forum/overview', {
      params: { username: username.value || undefined }
    })
    if (response.code == 200) {
      Object.assign(overview, response.data || {})
    }
  } catch (error) {
    console.error('获取社区概览失败', error)
  }
}

const fetchPosts = async () => {
  try {
    loading.value = true
    const response = await request.get('/forum/posts', {
      params: {
        keyword: keyword.value || undefined,
        categoryId: selectedCategory.value,
        sort: sort.value,
        pageNum: pageNum.value,
        pageSize,
        username: username.value || undefined
      }
    })
    if (response.code == 200) {
      posts.value = response.data?.list || []
      total.value = Number(response.data?.total || 0)
    } else {
      ElMessage.error(response.msg || '获取社区文章失败')
    }
  } catch (error) {
    console.error('获取社区文章失败', error)
    ElMessage.error('社区内容加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const applySearch = () => {
  keyword.value = keywordInput.value.trim()
  refreshFromFirstPage()
}

const selectCategory = (id?: number) => {
  selectedCategory.value = id
  refreshFromFirstPage()
}

const refreshFromFirstPage = () => {
  pageNum.value = 1
  fetchPosts()
}

const startWriting = () => {
  if (!username.value) {
    ElMessage.warning('请先通过页面右上角登录，再发表研学心得')
    return
  }
  router.push('/community/editor')
}

const openPost = (id: number) => {
  router.push(`/community/post/${id}`)
}

const formatDate = (value?: string) => {
  if (!value) return '刚刚'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

const identityLabel = (identity?: string) => {
  const map: Record<string, string> = {
    teacher: '教师',
    doctor: '博士研究生',
    master: '硕士研究生',
    graduate: '毕业成员'
  }
  return map[identity || ''] || identity || '实验室成员'
}

onMounted(async () => {
  await Promise.all([fetchOverview(), fetchPosts()])
})
</script>

<style scoped>
.community-page {
  min-height: 100vh;
  background: #f4f7fb;
  color: #22304a;
}

.community-hero {
  position: relative;
  overflow: hidden;
  background: linear-gradient(120deg, #102a5e 0%, #1e3a8a 55%, #315eb1 100%);
  color: white;
  padding: 52px 24px;
}

.hero-decoration {
  position: absolute;
  border: 1px solid rgba(255, 255, 255, .14);
  border-radius: 50%;
}

.hero-decoration-one {
  width: 320px;
  height: 320px;
  right: 8%;
  top: -180px;
}

.hero-decoration-two {
  width: 190px;
  height: 190px;
  right: 24%;
  bottom: -130px;
}

.hero-inner {
  position: relative;
  z-index: 1;
  max-width: 1240px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 60px;
}

.hero-eyebrow {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  letter-spacing: 2px;
  color: #d7e5ff;
}

.hero-copy h1 {
  margin: 10px 0 8px;
  font-size: 42px;
  letter-spacing: 4px;
}

.hero-copy p {
  margin: 0;
  max-width: 620px;
  font-size: 17px;
  color: #dce8ff;
}

.hero-actions {
  margin-top: 26px;
  display: flex;
  gap: 12px;
}

.outline-button {
  color: white;
  border-color: rgba(255, 255, 255, .5);
  background: rgba(255, 255, 255, .08);
}

.hero-stats {
  min-width: 330px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  padding: 23px 18px;
  border: 1px solid rgba(255, 255, 255, .2);
  border-radius: 18px;
  background: rgba(255, 255, 255, .09);
  backdrop-filter: blur(10px);
}

.hero-stat {
  text-align: center;
  border-right: 1px solid rgba(255, 255, 255, .18);
}

.hero-stat:last-child {
  border-right: none;
}

.hero-stat strong,
.hero-stat span {
  display: block;
}

.hero-stat strong {
  font-size: 27px;
  color: #ffd666;
}

.hero-stat span {
  margin-top: 3px;
  font-size: 12px;
  color: #dce8ff;
}

.community-container {
  max-width: 1240px;
  margin: 0 auto;
  padding: 28px 20px 60px;
}

.search-panel,
.article-column,
.side-card {
  border: 1px solid #e7ecf4;
  background: white;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(32, 58, 105, .05);
}

.search-panel {
  padding: 22px 24px 18px;
  margin-bottom: 20px;
}

.search-box {
  max-width: 720px;
  display: flex;
  gap: 10px;
}

.category-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.category-chip {
  border: 1px solid #dce5f2;
  border-radius: 20px;
  padding: 7px 14px;
  color: #53627a;
  background: #f8faff;
  cursor: pointer;
  transition: .2s ease;
}

.category-chip span {
  margin-left: 4px;
  color: #9aa8bd;
}

.category-chip:hover,
.category-chip.active {
  color: white;
  border-color: #1e3a8a;
  background: #1e3a8a;
}

.category-chip.active span {
  color: #dbe7ff;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 310px;
  gap: 20px;
  align-items: start;
}

.article-column {
  padding: 22px 24px;
  min-height: 500px;
}

.list-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 18px;
  border-bottom: 1px solid #edf0f5;
}

.list-toolbar h2 {
  display: inline;
  margin: 0 10px 0 0;
  color: #1c2f53;
  font-size: 21px;
}

.list-toolbar span {
  color: #98a2b3;
  font-size: 13px;
}

.article-list {
  min-height: 300px;
}

.article-card {
  display: flex;
  gap: 22px;
  padding: 24px 4px;
  border-bottom: 1px solid #edf0f5;
  cursor: pointer;
  transition: transform .2s ease;
}

.article-card:hover {
  transform: translateY(-2px);
}

.article-card:hover h3 {
  color: #2457a7;
}

.article-content {
  min-width: 0;
  flex: 1;
}

.article-badges {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 7px;
  min-height: 24px;
}

.badge,
.category-label,
.member-only {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  border-radius: 4px;
  padding: 3px 8px;
  font-size: 12px;
}

.badge.top {
  color: #a83b27;
  background: #fff0ec;
}

.badge.featured {
  color: #946200;
  background: #fff7d9;
}

.category-label {
  color: #315c9a;
  background: #edf4ff;
}

.member-only {
  color: #725b9e;
  background: #f3efff;
}

.article-card h3 {
  margin: 10px 0 8px;
  overflow: hidden;
  color: #1e2d49;
  font-size: 19px;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color .2s ease;
}

.summary {
  display: -webkit-box;
  overflow: hidden;
  margin: 0;
  color: #68758a;
  line-height: 1.7;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.tag-list {
  display: flex;
  gap: 9px;
  margin-top: 10px;
  color: #7690b6;
  font-size: 12px;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.author,
.article-meta {
  display: flex;
  align-items: center;
}

.author {
  gap: 7px;
}

.author strong {
  font-size: 13px;
  color: #394963;
}

.author span,
.article-meta {
  color: #98a2b3;
  font-size: 12px;
}

.article-meta {
  gap: 15px;
}

.article-meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.article-cover {
  flex: 0 0 180px;
  width: 180px;
  height: 116px;
  overflow: hidden;
  align-self: center;
  border-radius: 9px;
}

.article-cover :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.cover-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: white;
  font-size: 38px;
  font-weight: 700;
  background: linear-gradient(135deg, #2d5ca5, #7b9bd1);
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 28px 0 6px;
}

.side-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.side-card {
  padding: 20px;
}

.side-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 13px;
  color: #1e3a8a;
  border-bottom: 1px solid #edf0f5;
}

.side-title h3 {
  margin: 0;
  color: #253858;
  font-size: 17px;
}

.about-card p {
  margin: 15px 0;
  color: #68758a;
  font-size: 13px;
  line-height: 1.8;
}

.community-rules {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 5px;
}

.community-rules span {
  display: flex;
  align-items: center;
  gap: 3px;
  color: #58729a;
  font-size: 11px;
}

.featured-item {
  display: flex;
  gap: 10px;
  padding: 13px 0;
  border-bottom: 1px dashed #edf0f5;
  cursor: pointer;
}

.featured-item:last-child {
  border-bottom: none;
}

.featured-item > span {
  color: #c7d1df;
  font-size: 18px;
  font-weight: 700;
}

.featured-item:nth-child(2) > span,
.featured-item:nth-child(3) > span,
.featured-item:nth-child(4) > span {
  color: #e9a83a;
}

.featured-item div {
  min-width: 0;
}

.featured-item strong,
.featured-item small {
  display: block;
}

.featured-item strong {
  display: -webkit-box;
  overflow: hidden;
  color: #34445f;
  font-size: 13px;
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.featured-item small {
  margin-top: 5px;
  color: #9ba6b7;
}

.contributor-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px 8px;
  padding-top: 18px;
}

.contributor {
  min-width: 0;
  text-align: center;
}

.contributor :deep(.el-avatar) {
  display: block;
  margin: 0 auto 6px;
}

.contributor strong,
.contributor span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.contributor strong {
  color: #46556d;
  font-size: 12px;
}

.contributor span {
  margin-top: 2px;
  color: #9aa5b5;
  font-size: 10px;
}

@media (max-width: 980px) {
  .hero-inner {
    display: block;
  }

  .hero-stats {
    margin-top: 28px;
    max-width: 420px;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }

  .side-column {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 680px) {
  .community-hero {
    padding: 35px 18px;
  }

  .hero-copy h1 {
    font-size: 32px;
  }

  .hero-stats {
    min-width: 0;
  }

  .search-box,
  .list-toolbar,
  .article-footer {
    align-items: stretch;
    flex-direction: column;
  }

  .list-toolbar {
    gap: 14px;
  }

  .article-cover {
    display: none;
  }

  .article-meta {
    margin-top: 12px;
  }

  .side-column {
    display: flex;
  }
}
</style>
