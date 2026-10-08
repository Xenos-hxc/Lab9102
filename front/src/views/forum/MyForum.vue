<template>
  <div class="my-forum-page">
    <section class="page-hero">
      <div>
        <button class="back-link" @click="router.push('/community')">
          <el-icon><ArrowLeft /></el-icon>
          返回研学社区
        </button>
        <p>PERSONAL KNOWLEDGE DESK</p>
        <h1>我的研学空间</h1>
        <span>管理你的草稿、审核进度、已发布文章与收藏内容。</span>
      </div>
      <el-button type="warning" size="large" @click="router.push('/community/editor')">
        <el-icon><EditPen /></el-icon>
        写一篇新文章
      </el-button>
    </section>

    <main class="page-container">
      <section class="summary-row">
        <div v-for="item in summaryItems" :key="item.label" class="summary-card">
          <div class="summary-icon" :class="item.className">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div>
            <strong>{{ item.value }}</strong>
            <span>{{ item.label }}</span>
          </div>
        </div>
      </section>

      <section class="content-card">
        <div class="content-header">
          <div>
            <h2>内容管理</h2>
            <p>稿件经过管理员审核后会出现在研学社区。</p>
          </div>
          <el-input
            v-model="keyword"
            class="local-search"
            clearable
            placeholder="在当前列表中搜索"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>

        <div class="status-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.value"
            :class="{ active: activeTab === tab.value }"
            @click="changeTab(tab.value)"
          >
            {{ tab.label }}
            <span v-if="tab.value !== 'FAVORITES'">{{ statusCount(tab.value) }}</span>
          </button>
        </div>

        <div v-loading="loading" class="post-list">
          <article v-for="post in filteredPosts" :key="post.id" class="post-card">
            <div v-if="post.coverUrl" class="post-cover">
              <el-image :src="post.coverUrl" fit="cover" />
            </div>
            <div v-else class="post-cover cover-placeholder">
              {{ (post.categoryName || '研')[0] }}
            </div>

            <div class="post-main">
              <div class="post-labels">
                <el-tag :type="statusMeta(post.status).type" effect="light">
                  {{ statusMeta(post.status).label }}
                </el-tag>
                <span class="category">{{ post.categoryName || '研学分享' }}</span>
                <span v-if="post.visibility === 'MEMBER'" class="visibility">
                  <el-icon><Lock /></el-icon>仅成员
                </span>
              </div>
              <h3 @click="openPost(post)">{{ post.title }}</h3>
              <p>{{ post.summary || '暂未填写文章摘要。' }}</p>
              <div v-if="post.status === 'REJECTED' && post.rejectReason" class="reject-reason">
                <el-icon><Warning /></el-icon>
                <span><strong>审核意见：</strong>{{ post.rejectReason }}</span>
              </div>
              <div class="post-bottom">
                <div class="meta">
                  <span><el-icon><Clock /></el-icon>{{ formatDate(post.updatedAt) }}</span>
                  <span v-if="post.status === 'PUBLISHED'"><el-icon><View /></el-icon>{{ post.viewCount || 0 }}</span>
                  <span v-if="post.status === 'PUBLISHED'"><el-icon><Star /></el-icon>{{ post.favoriteCount || 0 }}</span>
                </div>
                <div class="actions">
                  <el-button
                    v-if="activeTab === 'FAVORITES'"
                    link
                    type="primary"
                    @click="router.push(`/community/post/${post.id}`)"
                  >
                    阅读文章
                  </el-button>
                  <template v-else>
                    <el-button
                      v-if="post.status !== 'PENDING'"
                      link
                      type="primary"
                      @click="router.push(`/community/editor/${post.id}`)"
                    >
                      编辑
                    </el-button>
                    <el-button
                      v-if="post.status === 'PUBLISHED'"
                      link
                      type="primary"
                      @click="router.push(`/community/post/${post.id}`)"
                    >
                      查看
                    </el-button>
                    <el-button
                      v-if="['DRAFT', 'REJECTED', 'OFFLINE'].includes(post.status)"
                      link
                      type="success"
                      :loading="submittingId === post.id"
                      @click="submitPost(post)"
                    >
                      提交审核
                    </el-button>
                    <el-button link type="danger" @click="removePost(post)">删除</el-button>
                  </template>
                </div>
              </div>
            </div>
          </article>

          <el-empty
            v-if="!loading && filteredPosts.length === 0"
            :description="activeTab === 'FAVORITES' ? '还没有收藏文章' : '当前状态下没有文章'"
            :image-size="130"
          >
            <el-button v-if="activeTab !== 'FAVORITES'" type="primary" @click="router.push('/community/editor')">
              开始写作
            </el-button>
            <el-button v-else type="primary" @click="router.push('/community')">去社区看看</el-button>
          </el-empty>
        </div>

        <div v-if="activeTab !== 'FAVORITES' && total > pageSize" class="pagination">
          <el-pagination
            v-model:current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            background
            @current-change="fetchPosts"
          />
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, markRaw, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowLeft, Clock, Collection, Document, EditPen, Finished,
  Lock, Search, Star, Timer, View, Warning
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { getForumUsername } from '@/utils/forumUser'

const router = useRouter()
const username = getForumUsername()
const loading = ref(false)
const submittingId = ref<number>()
const activeTab = ref('')
const keyword = ref('')
const posts = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 8
const counts = reactive<Record<string, number>>({
  ALL: 0,
  DRAFT: 0,
  PENDING: 0,
  PUBLISHED: 0,
  REJECTED: 0,
  OFFLINE: 0
})

const tabs = [
  { label: '全部文章', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '审核中', value: 'PENDING' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '需修改', value: 'REJECTED' },
  { label: '已下架', value: 'OFFLINE' },
  { label: '我的收藏', value: 'FAVORITES' }
]

const summaryItems = computed(() => [
  { label: '我的文章', value: counts.ALL, icon: markRaw(Document), className: 'blue' },
  { label: '审核中', value: counts.PENDING, icon: markRaw(Timer), className: 'gold' },
  { label: '已发布', value: counts.PUBLISHED, icon: markRaw(Finished), className: 'green' },
  { label: '草稿', value: counts.DRAFT, icon: markRaw(Collection), className: 'purple' }
])

const filteredPosts = computed(() => {
  const value = keyword.value.trim().toLowerCase()
  if (!value) return posts.value
  return posts.value.filter(post =>
    [post.title, post.summary, post.categoryName, ...(post.tags || [])]
      .some(item => String(item || '').toLowerCase().includes(value))
  )
})

const statusCount = (status: string) => status ? counts[status] || 0 : counts.ALL

const statusMeta = (status: string) => {
  const map: Record<string, { label: string; type: '' | 'success' | 'warning' | 'info' | 'danger' }> = {
    DRAFT: { label: '草稿', type: 'info' },
    PENDING: { label: '审核中', type: 'warning' },
    PUBLISHED: { label: '已发布', type: 'success' },
    REJECTED: { label: '需修改', type: 'danger' },
    OFFLINE: { label: '已下架', type: 'info' }
  }
  return map[status] || { label: status || '未知', type: '' }
}

const fetchCounts = async () => {
  const statuses = ['', 'DRAFT', 'PENDING', 'PUBLISHED', 'REJECTED', 'OFFLINE']
  const responses = await Promise.all(statuses.map(status =>
    request.get('/forum/my', {
      params: { username, status: status || undefined, pageNum: 1, pageSize: 1 }
    })
  ))
  responses.forEach((response: any, index) => {
    if (response.code == 200) {
      counts[statuses[index] || 'ALL'] = Number(response.data?.total || 0)
    }
  })
}

const fetchPosts = async () => {
  try {
    loading.value = true
    if (activeTab.value === 'FAVORITES') {
      const response = await request.get('/forum/favorites', { params: { username } })
      if (response.code == 200) {
        posts.value = response.data || []
        total.value = posts.value.length
      } else {
        ElMessage.error(response.msg || '获取收藏文章失败')
      }
      return
    }
    const response = await request.get('/forum/my', {
      params: {
        username,
        status: activeTab.value || undefined,
        pageNum: pageNum.value,
        pageSize
      }
    })
    if (response.code == 200) {
      posts.value = response.data?.list || []
      total.value = Number(response.data?.total || 0)
    } else {
      ElMessage.error(response.msg || '获取文章失败')
    }
  } catch (error) {
    console.error('获取我的文章失败', error)
    ElMessage.error('内容加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const changeTab = (value: string) => {
  activeTab.value = value
  pageNum.value = 1
  keyword.value = ''
  fetchPosts()
}

const openPost = (post: any) => {
  if (post.status === 'PUBLISHED' || activeTab.value === 'FAVORITES') {
    router.push(`/community/post/${post.id}`)
  } else {
    router.push(`/community/editor/${post.id}`)
  }
}

const submitPost = async (post: any) => {
  try {
    await ElMessageBox.confirm(`确认将《${post.title}》提交管理员审核吗？`, '提交审核', {
      type: 'info',
      confirmButtonText: '确认提交',
      cancelButtonText: '再检查一下'
    })
    submittingId.value = post.id
    const response = await request.post(`/forum/posts/${post.id}/submit`, null, {
      params: { username }
    })
    if (response.code == 200) {
      ElMessage.success('已提交审核')
      await Promise.all([fetchPosts(), fetchCounts()])
    } else {
      ElMessage.error(response.msg || '提交失败')
    }
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('提交文章失败', error)
      ElMessage.error('提交失败，请稍后重试')
    }
  } finally {
    submittingId.value = undefined
  }
}

const removePost = async (post: any) => {
  try {
    await ElMessageBox.confirm(`删除后将无法恢复，确认删除《${post.title}》吗？`, '删除文章', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    })
    const response = await request.delete(`/forum/posts/${post.id}`, { params: { username } })
    if (response.code == 200) {
      ElMessage.success('文章已删除')
      await Promise.all([fetchPosts(), fetchCounts()])
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('删除文章失败', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

const formatDate = (value?: string) => {
  if (!value) return '刚刚更新'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
  })
}

onMounted(async () => {
  if (!username) {
    ElMessage.warning('请先登录后查看个人研学空间')
    router.replace('/community')
    return
  }
  await Promise.all([fetchCounts(), fetchPosts()])
})
</script>

<style scoped>
.my-forum-page {
  min-height: 100vh;
  color: #263753;
  background: #f4f7fb;
}

.page-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 30px;
  padding: 42px max(24px, calc((100vw - 1200px) / 2));
  color: white;
  background:
    radial-gradient(circle at 78% -30%, rgba(255, 214, 102, .28), transparent 35%),
    linear-gradient(120deg, #102a5e, #244c96);
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 0;
  border: none;
  color: #dce8ff;
  background: transparent;
  cursor: pointer;
}

.page-hero p {
  margin: 22px 0 7px;
  color: #bcd1f4;
  font-size: 11px;
  letter-spacing: 2px;
}

.page-hero h1 {
  margin: 0 0 8px;
  font-size: 34px;
  letter-spacing: 3px;
}

.page-hero span {
  color: #dce8ff;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 26px 20px 60px;
}

.summary-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 18px;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  border: 1px solid #e7edf5;
  border-radius: 12px;
  background: white;
  box-shadow: 0 5px 18px rgba(31, 59, 105, .05);
}

.summary-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  border-radius: 12px;
  font-size: 22px;
}

.summary-icon.blue { color: #2c5fac; background: #eaf2ff; }
.summary-icon.gold { color: #bc801c; background: #fff5dc; }
.summary-icon.green { color: #27825e; background: #e7f8f1; }
.summary-icon.purple { color: #7358ac; background: #f1ecff; }

.summary-card strong,
.summary-card span {
  display: block;
}

.summary-card strong {
  color: #1b3155;
  font-size: 25px;
}

.summary-card span {
  margin-top: 2px;
  color: #8b98aa;
  font-size: 13px;
}

.content-card {
  overflow: hidden;
  min-height: 520px;
  border: 1px solid #e7edf5;
  border-radius: 13px;
  background: white;
  box-shadow: 0 6px 20px rgba(31, 59, 105, .05);
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  padding: 22px 26px 18px;
}

.content-header h2 {
  margin: 0;
  color: #20375c;
  font-size: 20px;
}

.content-header p {
  margin: 5px 0 0;
  color: #939fb0;
  font-size: 13px;
}

.local-search {
  max-width: 260px;
}

.status-tabs {
  display: flex;
  gap: 5px;
  overflow-x: auto;
  padding: 0 26px;
  border-bottom: 1px solid #e9edf3;
}

.status-tabs button {
  flex: 0 0 auto;
  padding: 12px 13px;
  border: none;
  border-bottom: 2px solid transparent;
  color: #6c798d;
  background: transparent;
  cursor: pointer;
}

.status-tabs button.active {
  color: #1f57a4;
  border-bottom-color: #1f57a4;
  font-weight: 600;
}

.status-tabs span {
  display: inline-block;
  min-width: 18px;
  margin-left: 4px;
  padding: 1px 5px;
  border-radius: 10px;
  color: #8c98a8;
  background: #edf1f6;
  font-size: 11px;
}

.status-tabs button.active span {
  color: #23579f;
  background: #e6f0ff;
}

.post-list {
  min-height: 340px;
  padding: 0 26px;
}

.post-card {
  display: flex;
  gap: 20px;
  padding: 24px 0;
  border-bottom: 1px solid #edf0f4;
}

.post-cover {
  flex: 0 0 150px;
  width: 150px;
  height: 104px;
  overflow: hidden;
  border-radius: 9px;
}

.post-cover :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #3564ab, #8aa9d5);
}

.post-main {
  min-width: 0;
  flex: 1;
}

.post-labels {
  display: flex;
  align-items: center;
  gap: 9px;
}

.category,
.visibility {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  color: #6e809c;
  font-size: 12px;
}

.post-main h3 {
  overflow: hidden;
  margin: 8px 0 6px;
  color: #253a5c;
  font-size: 18px;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

.post-main h3:hover {
  color: #245cad;
}

.post-main > p {
  display: -webkit-box;
  overflow: hidden;
  margin: 0;
  color: #718096;
  font-size: 13px;
  line-height: 1.7;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
}

.reject-reason {
  display: flex;
  align-items: flex-start;
  gap: 7px;
  margin-top: 9px;
  padding: 7px 10px;
  border-radius: 5px;
  color: #ae4938;
  background: #fff1ef;
  font-size: 12px;
}

.post-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-top: 13px;
}

.meta {
  display: flex;
  gap: 15px;
  color: #99a3b2;
  font-size: 12px;
}

.meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.actions {
  flex: 0 0 auto;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 25px;
}

@media (max-width: 800px) {
  .page-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .summary-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .content-header,
  .post-bottom {
    align-items: flex-start;
    flex-direction: column;
  }

  .local-search {
    width: 100%;
    max-width: none;
  }
}

@media (max-width: 560px) {
  .summary-row {
    grid-template-columns: 1fr;
  }

  .post-cover {
    display: none;
  }

  .post-list,
  .content-header,
  .status-tabs {
    padding-left: 16px;
    padding-right: 16px;
  }
}
</style>
