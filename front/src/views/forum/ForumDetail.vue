<template>
  <div class="detail-page" v-loading="loading">
    <div class="detail-container" v-if="post">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/community' }">研学社区</el-breadcrumb-item>
        <el-breadcrumb-item>{{ post.categoryName || '文章详情' }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="detail-grid">
        <main class="article-main">
          <header class="article-header">
            <div class="header-flags">
              <span class="category">{{ post.categoryName || '研学分享' }}</span>
              <span v-if="post.isTop" class="flag top">置顶</span>
              <span v-if="post.isFeatured" class="flag featured">精选</span>
              <span v-if="post.visibility === 'MEMBER'" class="flag member">
                <el-icon><Lock /></el-icon>仅成员可见
              </span>
            </div>
            <h1>{{ post.title }}</h1>
            <p class="article-summary" v-if="post.summary">{{ post.summary }}</p>
            <div class="author-row">
              <el-avatar :size="44" :src="post.authorAvatar || defaultAvatar" />
              <div class="author-info">
                <strong>{{ post.authorName || '实验室成员' }}</strong>
                <span>{{ identityLabel(post.authorIdentity) }}</span>
              </div>
              <div class="publish-meta">
                <span>发布于 {{ formatDateTime(post.publishedAt || post.updatedAt) }}</span>
                <span><el-icon><View /></el-icon>{{ post.viewCount || 0 }} 阅读</span>
                <span><el-icon><Pointer /></el-icon>{{ post.likeCount || 0 }} 点赞</span>
                <span><el-icon><Star /></el-icon>{{ post.favoriteCount || 0 }} 收藏</span>
              </div>
            </div>
          </header>

          <div class="cover-wrap" v-if="post.coverUrl">
            <el-image :src="post.coverUrl" fit="cover" />
          </div>

          <article class="rich-content" v-html="post.content"></article>

          <section class="attachment-section" v-if="post.attachments?.length">
            <div class="section-title">
              <el-icon><Paperclip /></el-icon>
              文章附件
            </div>
            <div class="attachment-list">
              <div v-for="file in post.attachments" :key="file.id" class="attachment-item">
                <div class="file-icon">{{ fileExtension(file.fileName) }}</div>
                <div class="file-info">
                  <strong>{{ file.fileName }}</strong>
                  <span>{{ formatFileSize(file.fileSize) }}</span>
                </div>
                <el-button type="primary" plain @click="openFile(file.fileUrl)">
                  <el-icon><Download /></el-icon>
                  查看附件
                </el-button>
              </div>
            </div>
          </section>

          <div class="article-tags" v-if="post.tags?.length">
            <span v-for="tag in post.tags" :key="tag"># {{ tag }}</span>
          </div>

          <div class="interaction-bar">
            <button :class="{ active: post.liked }" @click="toggleLike">
              <el-icon><Pointer /></el-icon>
              {{ post.liked ? '已点赞' : '点赞' }}
              <span>{{ post.likeCount || 0 }}</span>
            </button>
            <button :class="{ active: post.favorited }" @click="toggleFavorite">
              <el-icon><Star /></el-icon>
              {{ post.favorited ? '已收藏' : '收藏' }}
              <span>{{ post.favoriteCount || 0 }}</span>
            </button>
            <button @click="copyLink">
              <el-icon><Link /></el-icon>
              复制链接
            </button>
          </div>

          <section class="related-section" v-if="post.related?.length">
            <div class="section-title">
              <el-icon><Reading /></el-icon>
              继续阅读
            </div>
            <div class="related-grid">
              <article v-for="item in post.related" :key="item.id" @click="openRelated(item.id)">
                <span>{{ item.categoryName || '研学分享' }}</span>
                <h3>{{ item.title }}</h3>
                <p>{{ item.authorName }} · {{ item.viewCount || 0 }} 阅读</p>
              </article>
            </div>
          </section>
        </main>

        <aside class="detail-aside">
          <section class="author-card">
            <el-avatar :size="68" :src="post.authorAvatar || defaultAvatar" />
            <h3>{{ post.authorName || '实验室成员' }}</h3>
            <p>{{ identityLabel(post.authorIdentity) }}</p>
            <div class="author-note">感谢分享科研道路上的真实思考与实践经验。</div>
          </section>

          <section class="principle-card">
            <div class="aside-title">社区理念</div>
            <p>保持好奇，尊重原创，让每一份经验都能被后来者找到。</p>
            <el-button type="primary" @click="router.push('/community')">返回社区</el-button>
          </section>
        </aside>
      </div>
    </div>

    <el-result
      v-else-if="!loading"
      icon="warning"
      title="暂时无法查看这篇文章"
      :sub-title="errorMessage || '文章不存在或尚未发布'"
    >
      <template #extra>
        <el-button type="primary" @click="router.push('/community')">返回研学社区</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Download, Link, Lock, Paperclip, Pointer, Reading, Star, View
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { getForumUsername } from '@/utils/forumUser'
import defaultAvatar from '@/assets/user.png'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const post = ref<any>()
const errorMessage = ref('')
const username = getForumUsername()

const fetchDetail = async () => {
  try {
    loading.value = true
    post.value = undefined
    const response = await request.get(`/forum/posts/${route.params.id}`, {
      params: { username: username || undefined }
    })
    if (response.code == 200) {
      post.value = response.data
    } else {
      errorMessage.value = response.msg || '文章不存在或暂时不可见'
    }
  } catch (error: any) {
    errorMessage.value = error.response?.data?.msg || '文章加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const requireLogin = () => {
  if (username) return true
  ElMessage.warning('请先登录后再进行该操作')
  return false
}

const toggleLike = async () => {
  if (!requireLogin() || !post.value) return
  const response = await request.post(`/forum/posts/${post.value.id}/like`, null, {
    params: { username }
  })
  if (response.code == 200) {
    post.value.liked = response.data.active
    post.value.likeCount = response.data.likeCount
    ElMessage.success(response.data.active ? '已点赞' : '已取消点赞')
  } else {
    ElMessage.error(response.msg || '操作失败')
  }
}

const toggleFavorite = async () => {
  if (!requireLogin() || !post.value) return
  const response = await request.post(`/forum/posts/${post.value.id}/favorite`, null, {
    params: { username }
  })
  if (response.code == 200) {
    post.value.favorited = response.data.active
    post.value.favoriteCount = response.data.favoriteCount
    ElMessage.success(response.data.active ? '已收藏' : '已取消收藏')
  } else {
    ElMessage.error(response.msg || '操作失败')
  }
}

const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(window.location.href)
    ElMessage.success('文章链接已复制')
  } catch {
    ElMessage.warning('请复制浏览器地址栏中的链接')
  }
}

const openRelated = (id: number) => {
  router.push(`/community/post/${id}`)
}

const openFile = (url: string) => {
  window.open(url, '_blank')
}

const formatDateTime = (value?: string) => {
  if (!value) return '刚刚'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatFileSize = (bytes?: number) => {
  const value = Number(bytes || 0)
  if (value < 1024) return `${value} B`
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`
  return `${(value / 1024 / 1024).toFixed(1)} MB`
}

const fileExtension = (name?: string) => {
  const extension = name?.split('.').pop()?.toUpperCase()
  return extension && extension.length <= 5 ? extension : 'FILE'
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

watch(() => route.params.id, fetchDetail)
onMounted(fetchDetail)
</script>

<style scoped>
.detail-page {
  min-height: 720px;
  background: #f4f7fb;
  padding: 28px 20px 60px;
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
}

.breadcrumb {
  margin: 0 0 20px 4px;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 20px;
  align-items: start;
}

.article-main,
.author-card,
.principle-card {
  border: 1px solid #e7ecf4;
  background: white;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(32, 58, 105, .05);
}

.article-main {
  padding: 38px 46px 34px;
}

.header-flags {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category,
.flag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  border-radius: 4px;
  padding: 4px 9px;
  font-size: 12px;
}

.category {
  color: #315c9a;
  background: #edf4ff;
}

.flag.top {
  color: #ae3d2d;
  background: #fff0ec;
}

.flag.featured {
  color: #976500;
  background: #fff6d6;
}

.flag.member {
  color: #6e5598;
  background: #f3efff;
}

.article-header h1 {
  margin: 18px 0 12px;
  color: #172b4d;
  font-size: 34px;
  line-height: 1.35;
}

.article-summary {
  margin: 0 0 22px;
  padding-left: 14px;
  color: #69778d;
  font-size: 15px;
  line-height: 1.8;
  border-left: 3px solid #89a9da;
}

.author-row {
  display: flex;
  align-items: center;
  padding: 16px 0 22px;
  border-bottom: 1px solid #edf0f5;
}

.author-info {
  margin-left: 10px;
}

.author-info strong,
.author-info span {
  display: block;
}

.author-info strong {
  color: #31415e;
}

.author-info span {
  margin-top: 3px;
  color: #95a0b1;
  font-size: 12px;
}

.publish-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-left: auto;
  color: #929cac;
  font-size: 12px;
}

.publish-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.cover-wrap {
  height: 360px;
  overflow: hidden;
  margin-top: 28px;
  border-radius: 10px;
}

.cover-wrap :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.rich-content {
  min-height: 260px;
  padding: 32px 0;
  color: #34445e;
  font-size: 16px;
  line-height: 1.9;
}

.rich-content :deep(h1),
.rich-content :deep(h2),
.rich-content :deep(h3) {
  margin: 1.4em 0 .7em;
  color: #1d3155;
}

.rich-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 7px;
}

.rich-content :deep(pre) {
  overflow-x: auto;
  border-radius: 7px;
  padding: 16px;
  color: #e8edf6;
  background: #1e293b;
}

.rich-content :deep(blockquote) {
  margin: 20px 0;
  padding: 12px 18px;
  color: #5e6d83;
  border-left: 4px solid #8ba8d3;
  background: #f6f8fc;
}

.attachment-section,
.related-section {
  margin-top: 10px;
  padding-top: 24px;
  border-top: 1px solid #edf0f5;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  color: #24477f;
  font-size: 18px;
  font-weight: 600;
}

.attachment-list {
  display: grid;
  gap: 10px;
}

.attachment-item {
  display: flex;
  align-items: center;
  padding: 13px 15px;
  border: 1px solid #e7edf6;
  border-radius: 8px;
  background: #f9fbfe;
}

.file-icon {
  display: flex;
  width: 44px;
  height: 44px;
  align-items: center;
  justify-content: center;
  margin-right: 11px;
  color: white;
  border-radius: 7px;
  font-size: 11px;
  font-weight: 700;
  background: #315fa4;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-info strong,
.file-info span {
  display: block;
}

.file-info strong {
  overflow: hidden;
  color: #3b4c68;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-info span {
  margin-top: 3px;
  color: #97a2b3;
  font-size: 12px;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 9px;
  margin-top: 28px;
}

.article-tags span {
  border-radius: 16px;
  padding: 6px 11px;
  color: #496a9b;
  font-size: 12px;
  background: #edf4ff;
}

.interaction-bar {
  display: flex;
  justify-content: center;
  gap: 14px;
  margin: 32px 0 10px;
}

.interaction-bar button {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 112px;
  justify-content: center;
  border: 1px solid #dbe4f1;
  border-radius: 22px;
  padding: 9px 16px;
  color: #56657b;
  background: white;
  cursor: pointer;
}

.interaction-bar button:hover,
.interaction-bar button.active {
  color: #1f56a2;
  border-color: #86a8d9;
  background: #f0f6ff;
}

.interaction-bar button span {
  color: #9ba5b5;
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.related-grid article {
  padding: 15px;
  border: 1px solid #e8edf5;
  border-radius: 8px;
  background: #fafcff;
  cursor: pointer;
}

.related-grid article:hover {
  border-color: #a9bfdf;
}

.related-grid span,
.related-grid p {
  color: #8c98aa;
  font-size: 12px;
}

.related-grid h3 {
  margin: 7px 0;
  color: #354761;
  font-size: 15px;
  line-height: 1.5;
}

.related-grid p {
  margin: 0;
}

.detail-aside {
  position: sticky;
  top: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.author-card,
.principle-card {
  padding: 24px 20px;
  text-align: center;
}

.author-card h3 {
  margin: 12px 0 4px;
  color: #2e405e;
}

.author-card > p {
  margin: 0;
  color: #8491a4;
  font-size: 13px;
}

.author-note {
  margin-top: 17px;
  padding-top: 14px;
  color: #78869a;
  font-size: 12px;
  line-height: 1.7;
  border-top: 1px solid #edf0f5;
}

.aside-title {
  color: #1e3a8a;
  font-size: 17px;
  font-weight: 600;
}

.principle-card p {
  color: #758297;
  font-size: 13px;
  line-height: 1.8;
}

@media (max-width: 900px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .detail-aside {
    position: static;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 650px) {
  .detail-page {
    padding: 15px 10px 40px;
  }

  .article-main {
    padding: 25px 18px;
  }

  .article-header h1 {
    font-size: 27px;
  }

  .publish-meta {
    display: none;
  }

  .related-grid,
  .detail-aside {
    grid-template-columns: 1fr;
  }
}
</style>
