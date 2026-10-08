<template>
  <div class="forum-admin">
    <header class="page-header">
      <div>
        <p>KNOWLEDGE COMMUNITY</p>
        <h1>研学社区管理</h1>
        <span>审核实验室成员分享，维护知识分类与内容秩序。</span>
      </div>
      <el-button type="primary" :loading="loading" @click="refreshAll">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </header>

    <section class="stat-grid">
      <div v-for="item in statCards" :key="item.label" class="stat-card">
        <div class="stat-icon" :class="item.className">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div>
          <strong>{{ item.value }}</strong>
          <span>{{ item.label }}</span>
        </div>
      </div>
    </section>

    <section class="management-card">
      <el-tabs v-model="activePanel" @tab-change="handlePanelChange">
        <el-tab-pane name="posts">
          <template #label>
            <span class="tab-label">
              <el-icon><Document /></el-icon>
              文章管理
              <em v-if="Number(stats.pending)">{{ stats.pending }}</em>
            </span>
          </template>

          <div class="filter-bar">
            <el-input
              v-model="filters.keyword"
              clearable
              placeholder="搜索标题、作者或标签"
              @keyup.enter="searchPosts"
              @clear="searchPosts"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-select v-model="filters.status" clearable placeholder="全部状态" @change="searchPosts">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-select v-model="filters.categoryId" clearable placeholder="全部分类" @change="searchPosts">
              <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <el-button type="primary" @click="searchPosts">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="resetFilters">重置</el-button>
          </div>

          <el-table v-loading="loading" :data="posts" stripe>
            <el-table-column label="文章" min-width="310">
              <template #default="{ row }">
                <div class="article-cell">
                  <div class="article-title">
                    <el-tag v-if="row.isTop" size="small" type="danger" effect="light">置顶</el-tag>
                    <el-tag v-if="row.isFeatured" size="small" type="warning" effect="light">精选</el-tag>
                    <strong @click="previewPost(row)">{{ row.title }}</strong>
                  </div>
                  <p>{{ row.summary || '暂无摘要' }}</p>
                  <div class="article-tags">
                    <span>{{ row.categoryName || '未分类' }}</span>
                    <span v-for="tag in (row.tags || []).slice(0, 3)" :key="tag">#{{ tag }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="作者" width="145">
              <template #default="{ row }">
                <div class="author-cell">
                  <el-avatar :size="34" :src="row.authorAvatar || defaultAvatar" />
                  <div>
                    <strong>{{ row.authorName || '实验室成员' }}</strong>
                    <span>{{ identityLabel(row.authorIdentity) }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="95" align="center">
              <template #default="{ row }">
                <el-tag :type="statusMeta(row.status).type" effect="light">
                  {{ statusMeta(row.status).label }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="数据" width="115" align="center">
              <template #default="{ row }">
                <div class="data-cell">
                  <span><el-icon><View /></el-icon>{{ row.viewCount || 0 }}</span>
                  <span><el-icon><Star /></el-icon>{{ row.favoriteCount || 0 }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="更新时间" width="145">
              <template #default="{ row }">
                <span class="time-text">{{ formatDate(row.updatedAt) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="245" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="previewPost(row)">预览</el-button>
                <template v-if="row.status === 'PENDING'">
                  <el-button link type="success" @click="reviewPost(row, 'APPROVE')">通过</el-button>
                  <el-button link type="danger" @click="openReject(row)">驳回</el-button>
                </template>
                <template v-if="row.status === 'PUBLISHED'">
                  <el-dropdown trigger="click" @command="handleFlagCommand(row, $event)">
                    <el-button link type="primary">推荐设置<el-icon><ArrowDown /></el-icon></el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="TOP">{{ row.isTop ? '取消置顶' : '设为置顶' }}</el-dropdown-item>
                        <el-dropdown-item command="FEATURED">{{ row.isFeatured ? '取消精选' : '设为精选' }}</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                  <el-button link type="warning" @click="openOffline(row)">下架</el-button>
                </template>
                <el-button
                  v-if="['REJECTED', 'OFFLINE'].includes(row.status)"
                  link
                  type="success"
                  @click="reviewPost(row, 'PUBLISH')"
                >
                  发布
                </el-button>
                <el-button link type="danger" @click="removePost(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="postTotal > filters.pageSize" class="pagination">
            <el-pagination
              v-model:current-page="filters.pageNum"
              v-model:page-size="filters.pageSize"
              :total="postTotal"
              :page-sizes="[10, 20, 30]"
              layout="total, sizes, prev, pager, next"
              background
              @current-change="fetchPosts"
              @size-change="searchPosts"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane name="categories">
          <template #label>
            <span class="tab-label"><el-icon><FolderOpened /></el-icon>分类管理</span>
          </template>
          <div class="sub-toolbar">
            <div>
              <h3>文章分类</h3>
              <p>用于组织社区中的知识主题，已有文章的分类不能删除。</p>
            </div>
            <el-button type="primary" @click="openCategory()">
              <el-icon><Plus /></el-icon>
              新建分类
            </el-button>
          </div>
          <el-table :data="categories" stripe>
            <el-table-column prop="name" label="分类名称" min-width="150" />
            <el-table-column prop="description" label="分类说明" min-width="280">
              <template #default="{ row }">{{ row.description || '暂无说明' }}</template>
            </el-table-column>
            <el-table-column prop="postCount" label="已发布文章" width="120" align="center" />
            <el-table-column prop="sortOrder" label="排序" width="90" align="center" />
            <el-table-column label="操作" width="150" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click="openCategory(row)">编辑</el-button>
                <el-button link type="danger" @click="removeCategory(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane name="tags">
          <template #label>
            <span class="tab-label"><el-icon><PriceTag /></el-icon>标签管理</span>
          </template>
          <div class="sub-toolbar">
            <div>
              <h3>社区标签</h3>
              <p>标签由成员发表文章时创建；停用后不会在文章与标签建议中展示。</p>
            </div>
          </div>
          <div v-loading="tagLoading" class="tag-cloud">
            <div v-for="tag in tags" :key="tag.id" class="tag-item" :class="{ disabled: tag.status === 0 }">
              <span># {{ tag.name }}</span>
              <el-switch
                :model-value="tag.status === 1"
                inline-prompt
                active-text="启"
                inactive-text="停"
                @change="toggleTag(tag)"
              />
            </div>
            <el-empty v-if="!tagLoading && tags.length === 0" description="暂时还没有文章标签" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-drawer v-model="previewVisible" title="文章预览" size="64%">
      <div v-loading="previewLoading" class="preview-panel">
        <template v-if="previewData.id">
          <div class="preview-labels">
            <el-tag :type="statusMeta(previewData.status).type">{{ statusMeta(previewData.status).label }}</el-tag>
            <el-tag effect="plain">{{ previewData.categoryName || '研学分享' }}</el-tag>
            <el-tag v-if="previewData.visibility === 'MEMBER'" type="info" effect="plain">仅成员可见</el-tag>
          </div>
          <h1>{{ previewData.title }}</h1>
          <div class="preview-meta">
            {{ previewData.authorName }} · {{ formatDate(previewData.updatedAt) }} · {{ previewData.viewCount || 0 }} 次阅读
          </div>
          <p v-if="previewData.summary" class="preview-summary">{{ previewData.summary }}</p>
          <el-image v-if="previewData.coverUrl" class="preview-cover" :src="previewData.coverUrl" fit="cover" />
          <div class="rich-content" v-html="previewData.content"></div>
          <div v-if="previewData.attachments?.length" class="attachment-list">
            <h4>文章附件</h4>
            <a
              v-for="file in previewData.attachments"
              :key="file.id"
              :href="file.fileUrl"
              target="_blank"
              rel="noopener"
            >
              <el-icon><Paperclip /></el-icon>
              {{ file.fileName }}
            </a>
          </div>
        </template>
      </div>
    </el-drawer>

    <el-dialog v-model="reasonDialog.visible" :title="reasonDialog.action === 'REJECT' ? '驳回文章' : '下架文章'" width="480px">
      <el-form label-position="top">
        <el-form-item :label="reasonDialog.action === 'REJECT' ? '审核意见（必填）' : '下架说明（选填）'">
          <el-input
            v-model="reasonDialog.reason"
            type="textarea"
            :rows="4"
            maxlength="300"
            show-word-limit
            :placeholder="reasonDialog.action === 'REJECT' ? '请说明需要修改的内容，帮助作者完善文章' : '可填写下架原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reasonDialog.visible = false">取消</el-button>
        <el-button
          :type="reasonDialog.action === 'REJECT' ? 'danger' : 'warning'"
          :loading="reasonDialog.loading"
          @click="confirmReasonAction"
        >
          确认{{ reasonDialog.action === 'REJECT' ? '驳回' : '下架' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="categoryDialog.visible" :title="categoryDialog.form.id ? '编辑分类' : '新建分类'" width="500px">
      <el-form label-position="top">
        <el-form-item label="分类名称" required>
          <el-input v-model="categoryDialog.form.name" maxlength="50" show-word-limit placeholder="例如：科研方法" />
        </el-form-item>
        <el-form-item label="分类说明">
          <el-input
            v-model="categoryDialog.form.description"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="简要说明该分类适合分享的内容"
          />
        </el-form-item>
        <el-form-item label="展示排序">
          <el-input-number v-model="categoryDialog.form.sortOrder" :min="0" :max="999" />
          <span class="form-tip">数字越小越靠前</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="categoryDialog.loading" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, markRaw, onMounted, reactive, ref } from 'vue'
import {
  ArrowDown, Clock, Collection, Document, FolderOpened, Paperclip,
  Plus, PriceTag, Refresh, Search, Star, TrendCharts, View
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import defaultAvatar from '@/assets/user.png'

const activePanel = ref('posts')
const loading = ref(false)
const tagLoading = ref(false)
const previewVisible = ref(false)
const previewLoading = ref(false)
const posts = ref<any[]>([])
const postTotal = ref(0)
const categories = ref<any[]>([])
const tags = ref<any[]>([])
const previewData = ref<any>({})
const stats = reactive({
  total: 0,
  pending: 0,
  published: 0,
  draft: 0,
  offline: 0,
  rejected: 0,
  views: 0,
  favorites: 0
})
const filters = reactive({
  keyword: '',
  categoryId: undefined as number | undefined,
  status: '',
  pageNum: 1,
  pageSize: 10
})
const reasonDialog = reactive({
  visible: false,
  loading: false,
  action: 'REJECT',
  reason: '',
  post: null as any
})
const categoryDialog = reactive({
  visible: false,
  loading: false,
  form: {
    id: undefined as number | undefined,
    name: '',
    description: '',
    sortOrder: 0,
    status: 1
  }
})

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '审核中', value: 'PENDING' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已驳回', value: 'REJECTED' },
  { label: '已下架', value: 'OFFLINE' }
]

const statCards = computed(() => [
  { label: '全部文章', value: stats.total, icon: markRaw(Document), className: 'blue' },
  { label: '待审核', value: stats.pending, icon: markRaw(Clock), className: 'gold' },
  { label: '已发布', value: stats.published, icon: markRaw(Collection), className: 'green' },
  { label: '累计阅读', value: stats.views, icon: markRaw(TrendCharts), className: 'purple' }
])

const statusMeta = (status: string) => {
  const map: Record<string, { label: string; type: '' | 'success' | 'warning' | 'info' | 'danger' }> = {
    DRAFT: { label: '草稿', type: 'info' },
    PENDING: { label: '待审核', type: 'warning' },
    PUBLISHED: { label: '已发布', type: 'success' },
    REJECTED: { label: '已驳回', type: 'danger' },
    OFFLINE: { label: '已下架', type: 'info' }
  }
  return map[status] || { label: status || '未知', type: '' }
}

const fetchStats = async () => {
  const response = await request.get('/forum/admin/stats')
  if (response.code == 200) Object.assign(stats, response.data || {})
}

const fetchCategories = async () => {
  const response = await request.get('/forum/categories')
  if (response.code == 200) categories.value = response.data || []
}

const fetchTags = async () => {
  try {
    tagLoading.value = true
    const response = await request.get('/forum/admin/tags')
    if (response.code == 200) {
      tags.value = response.data || []
    } else {
      ElMessage.error(response.msg || '获取标签失败')
    }
  } finally {
    tagLoading.value = false
  }
}

const fetchPosts = async () => {
  try {
    loading.value = true
    const response = await request.get('/forum/admin/posts', {
      params: {
        keyword: filters.keyword.trim() || undefined,
        categoryId: filters.categoryId,
        status: filters.status || undefined,
        pageNum: filters.pageNum,
        pageSize: filters.pageSize
      }
    })
    if (response.code == 200) {
      posts.value = response.data?.list || []
      postTotal.value = Number(response.data?.total || 0)
    } else {
      ElMessage.error(response.msg || '获取文章失败')
    }
  } catch (error) {
    console.error('获取管理文章失败', error)
    ElMessage.error('文章列表加载失败')
  } finally {
    loading.value = false
  }
}

const refreshAll = async () => {
  try {
    await Promise.all([fetchStats(), fetchCategories(), fetchPosts()])
    if (activePanel.value === 'tags') await fetchTags()
    ElMessage.success('数据已刷新')
  } catch (error) {
    console.error('刷新论坛管理数据失败', error)
    ElMessage.error('部分数据刷新失败')
  }
}

const searchPosts = () => {
  filters.pageNum = 1
  fetchPosts()
}

const resetFilters = () => {
  filters.keyword = ''
  filters.categoryId = undefined
  filters.status = ''
  searchPosts()
}

const previewPost = async (post: any) => {
  previewVisible.value = true
  previewLoading.value = true
  previewData.value = {}
  try {
    const response = await request.get(`/forum/posts/${post.id}`, { params: { admin: true } })
    if (response.code == 200) previewData.value = response.data || {}
    else ElMessage.error(response.msg || '文章预览失败')
  } catch (error) {
    console.error('文章预览失败', error)
    ElMessage.error('文章预览失败')
  } finally {
    previewLoading.value = false
  }
}

const reviewPost = async (post: any, action: string, reason?: string) => {
  if (action === 'APPROVE' || action === 'PUBLISH') {
    try {
      await ElMessageBox.confirm(
        action === 'APPROVE' ? `确认审核通过《${post.title}》吗？` : `确认重新发布《${post.title}》吗？`,
        action === 'APPROVE' ? '审核通过' : '重新发布',
        { type: 'info', confirmButtonText: '确认', cancelButtonText: '取消' }
      )
    } catch {
      return false
    }
  }
  const response = await request.put(`/forum/admin/posts/${post.id}/review`, { action, reason })
  if (response.code == 200) {
    ElMessage.success(action === 'REJECT' ? '文章已驳回' : action === 'OFFLINE' ? '文章已下架' : '文章已发布')
    await Promise.all([fetchPosts(), fetchStats(), fetchCategories()])
    return true
  }
  ElMessage.error(response.msg || '操作失败')
  return false
}

const openReject = (post: any) => {
  Object.assign(reasonDialog, { visible: true, loading: false, action: 'REJECT', reason: '', post })
}

const openOffline = (post: any) => {
  Object.assign(reasonDialog, { visible: true, loading: false, action: 'OFFLINE', reason: '', post })
}

const confirmReasonAction = async () => {
  if (reasonDialog.action === 'REJECT' && !reasonDialog.reason.trim()) {
    ElMessage.warning('请填写审核意见')
    return
  }
  try {
    reasonDialog.loading = true
    const success = await reviewPost(reasonDialog.post, reasonDialog.action, reasonDialog.reason.trim())
    if (success) reasonDialog.visible = false
  } catch (error) {
    console.error('审核操作失败', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    reasonDialog.loading = false
  }
}

const handleFlag = async (post: any, command: string) => {
  const current = command === 'TOP' ? Boolean(post.isTop) : Boolean(post.isFeatured)
  try {
    const response = await request.put(`/forum/admin/posts/${post.id}/flag`, {
      type: command,
      value: !current
    })
    if (response.code == 200) {
      ElMessage.success(command === 'TOP'
        ? (current ? '已取消置顶' : '已设为置顶')
        : (current ? '已取消精选' : '已设为精选'))
      await fetchPosts()
    } else {
      ElMessage.error(response.msg || '设置失败')
    }
  } catch (error) {
    console.error('推荐设置失败', error)
    ElMessage.error('设置失败，请稍后重试')
  }
}

const handleFlagCommand = (post: any, command: unknown) => handleFlag(post, String(command))

const removePost = async (post: any) => {
  try {
    await ElMessageBox.confirm(`确认删除《${post.title}》吗？删除后不可恢复。`, '删除文章', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    })
    const response = await request.delete(`/forum/admin/posts/${post.id}`)
    if (response.code == 200) {
      ElMessage.success('文章已删除')
      await Promise.all([fetchPosts(), fetchStats(), fetchCategories()])
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

const openCategory = (category?: any) => {
  Object.assign(categoryDialog.form, {
    id: category?.id,
    name: category?.name || '',
    description: category?.description || '',
    sortOrder: Number(category?.sortOrder || 0),
    status: category?.status ?? 1
  })
  categoryDialog.visible = true
}

const saveCategory = async () => {
  if (!categoryDialog.form.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  try {
    categoryDialog.loading = true
    const response = await request.post('/forum/admin/categories', {
      ...categoryDialog.form,
      name: categoryDialog.form.name.trim(),
      description: categoryDialog.form.description.trim()
    })
    if (response.code == 200) {
      ElMessage.success(categoryDialog.form.id ? '分类已更新' : '分类已创建')
      categoryDialog.visible = false
      await fetchCategories()
    } else {
      ElMessage.error(response.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存分类失败', error)
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    categoryDialog.loading = false
  }
}

const removeCategory = async (category: any) => {
  try {
    await ElMessageBox.confirm(`确认删除分类“${category.name}”吗？`, '删除分类', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    })
    const response = await request.delete(`/forum/admin/categories/${category.id}`)
    if (response.code == 200) {
      ElMessage.success('分类已删除')
      await fetchCategories()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('删除分类失败', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

const toggleTag = async (tag: any) => {
  try {
    const response = await request.put(`/forum/admin/tags/${tag.id}/disable`)
    if (response.code == 200) {
      tag.status = response.data.status
      ElMessage.success(tag.status === 1 ? '标签已启用' : '标签已停用')
    } else {
      ElMessage.error(response.msg || '设置失败')
    }
  } catch (error) {
    console.error('设置标签状态失败', error)
    ElMessage.error('设置失败，请稍后重试')
  }
}

const handlePanelChange = (name: string | number) => {
  if (name === 'tags') fetchTags()
}

const formatDate = (value?: string) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

const identityLabel = (identity?: string) => {
  const map: Record<string, string> = {
    teacher: '教师', doctor: '博士研究生', master: '硕士研究生', graduate: '毕业成员'
  }
  return map[identity || ''] || identity || '实验室成员'
}

onMounted(async () => {
  try {
    await Promise.all([fetchStats(), fetchCategories(), fetchPosts()])
  } catch (error) {
    console.error('初始化论坛管理页失败', error)
    ElMessage.error('论坛管理数据加载失败')
  }
})
</script>

<style scoped>
.forum-admin {
  color: #283952;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 20px;
  padding: 25px 28px;
  overflow: hidden;
  border-radius: 13px;
  color: white;
  background:
    radial-gradient(circle at 85% -50%, rgba(255, 214, 102, .3), transparent 42%),
    linear-gradient(120deg, #173466, #2a59a5);
  box-shadow: 0 8px 20px rgba(31, 72, 137, .16);
}

.page-header p {
  margin: 0 0 5px;
  color: #bdd2f3;
  font-size: 10px;
  letter-spacing: 2px;
}

.page-header h1 {
  margin: 0 0 5px;
  font-size: 25px;
}

.page-header span {
  color: #dce8fa;
  font-size: 13px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border: 1px solid #e6ebf3;
  border-radius: 10px;
  background: white;
  box-shadow: 0 4px 15px rgba(40, 62, 99, .05);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  font-size: 21px;
}

.stat-icon.blue { color: #2c63b4; background: #eaf2ff; }
.stat-icon.gold { color: #b47a18; background: #fff4d8; }
.stat-icon.green { color: #25845d; background: #e6f8ef; }
.stat-icon.purple { color: #7157a8; background: #f0ebff; }

.stat-card strong,
.stat-card span {
  display: block;
}

.stat-card strong {
  color: #1f365c;
  font-size: 24px;
}

.stat-card span {
  margin-top: 2px;
  color: #8d99aa;
  font-size: 12px;
}

.management-card {
  min-height: 520px;
  padding: 4px 22px 25px;
  border: 1px solid #e6ebf3;
  border-radius: 11px;
  background: white;
  box-shadow: 0 5px 18px rgba(40, 62, 99, .05);
}

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.tab-label em {
  min-width: 18px;
  padding: 0 5px;
  border-radius: 9px;
  color: white;
  background: #e85c4a;
  font-size: 10px;
  font-style: normal;
  text-align: center;
}

.filter-bar {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 140px 150px auto auto;
  gap: 10px;
  margin: 8px 0 18px;
  padding: 15px;
  border-radius: 8px;
  background: #f6f8fc;
}

.article-cell {
  padding: 5px 0;
}

.article-title {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.article-title strong {
  overflow: hidden;
  color: #284164;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

.article-title strong:hover {
  color: #2c66ba;
}

.article-cell p {
  overflow: hidden;
  margin: 6px 0;
  color: #8490a1;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-tags {
  display: flex;
  gap: 9px;
  color: #7084a2;
  font-size: 11px;
}

.author-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-cell strong,
.author-cell span {
  display: block;
}

.author-cell strong {
  color: #374a68;
  font-size: 12px;
}

.author-cell span {
  margin-top: 2px;
  color: #98a2b1;
  font-size: 10px;
}

.data-cell {
  display: flex;
  justify-content: center;
  gap: 9px;
  color: #8995a6;
  font-size: 11px;
}

.data-cell span {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.time-text {
  color: #7f8b9d;
  font-size: 12px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  padding-top: 22px;
}

.sub-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin: 5px 0 18px;
  padding: 15px 18px;
  border-radius: 8px;
  background: #f6f8fc;
}

.sub-toolbar h3 {
  margin: 0;
  color: #2b4367;
  font-size: 16px;
}

.sub-toolbar p {
  margin: 5px 0 0;
  color: #8d98a8;
  font-size: 12px;
}

.tag-cloud {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr));
  gap: 12px;
  min-height: 200px;
}

.tag-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  min-height: 42px;
  padding: 10px 13px;
  border: 1px solid #dfe7f2;
  border-radius: 8px;
  color: #41638f;
  background: #f7faff;
}

.tag-item.disabled {
  color: #a2a9b3;
  background: #f5f5f5;
}

.preview-panel {
  max-width: 820px;
  min-height: 300px;
  margin: 0 auto;
  color: #34445d;
}

.preview-labels {
  display: flex;
  gap: 8px;
}

.preview-panel h1 {
  margin: 18px 0 10px;
  color: #1f3558;
  font-size: 29px;
  line-height: 1.4;
}

.preview-meta {
  padding-bottom: 16px;
  color: #8f99a8;
  font-size: 12px;
  border-bottom: 1px solid #edf0f4;
}

.preview-summary {
  margin: 18px 0;
  padding: 13px 16px;
  border-left: 3px solid #4e76b5;
  color: #62728a;
  background: #f5f8fc;
  line-height: 1.7;
}

.preview-cover {
  width: 100%;
  max-height: 360px;
  margin-bottom: 18px;
  border-radius: 9px;
}

.rich-content {
  overflow-wrap: anywhere;
  color: #35445b;
  font-size: 15px;
  line-height: 1.85;
}

.rich-content :deep(img) {
  max-width: 100%;
}

.attachment-list {
  margin-top: 26px;
  padding: 15px;
  border-radius: 8px;
  background: #f6f8fc;
}

.attachment-list h4 {
  margin: 0 0 10px;
}

.attachment-list a {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 7px 0;
  color: #3164aa;
  text-decoration: none;
}

.form-tip {
  margin-left: 10px;
  color: #98a2b1;
  font-size: 12px;
}

@media (max-width: 1100px) {
  .filter-bar {
    grid-template-columns: 1fr 140px 150px;
  }

  .tag-cloud {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 760px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .page-header,
  .sub-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .filter-bar {
    grid-template-columns: 1fr;
  }

  .tag-cloud {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
