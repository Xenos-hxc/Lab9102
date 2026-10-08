<template>
  <div class="news-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>新闻记录管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加新闻
          </el-button>
        </div>
      </template>

      <!-- 搜索筛选区域 -->
      <div class="filter-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索新闻标题或内容"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="filterYear"
              placeholder="选择年份"
              clearable
              @change="handleFilter"
            >
              <el-option
                v-for="year in yearOptions"
                :key="year"
                :label="year + '年'"
                :value="year"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select
              v-model="filterType"
              placeholder="选择类型"
              clearable
              @change="handleFilter"
            >
              <el-option
                v-for="type in typeOptions"
                :key="type"
                :label="type"
                :value="type"
              />
            </el-select>
          </el-col>
          <el-col :span="10" class="filter-actions">
            <el-button @click="resetFilters">重置筛选</el-button>
            <el-button type="primary" @click="refreshList">刷新列表</el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 新闻列表 -->
      <div class="news-list">
        <el-table
          :data="pagedNewsList"
          v-loading="loading"
          empty-text="暂无新闻记录"
          style="width: 100%"
        >
          <el-table-column label="新闻封面" width="120">
            <template #default="{ row }">
              <div class="cover-image">
                <el-image
                  v-if="row.pictureurl"
                  :src="getImageUrl(row.pictureurl)"
                  :preview-src-list="[getImageUrl(row.pictureurl)]"
                  fit="cover"
                  style="width: 80px; height: 60px; border-radius: 4px;"
                />
                <div v-else class="no-image">暂无图片</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="新闻标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="type" label="新闻类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getTypeTagType(row.type)">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="time" label="发布时间" width="120">
            <template #default="{ row }">
              {{ formatDate(row.time) }}
            </template>
          </el-table-column>
          <el-table-column prop="summary" label="新闻摘要" min-width="200" show-overflow-tooltip />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="handleView(row)">查看</el-button>
              <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="80%"
      :before-close="handleClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="news-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="新闻标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入新闻标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新闻类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择新闻类型">
                <el-option
                  v-for="type in typeOptions"
                  :key="type"
                  :label="type"
                  :value="type"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="发布时间" prop="time">
              <el-date-picker
                v-model="form.time"
                type="date"
                placeholder="选择发布时间"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新闻封面" prop="pictureurl">
              <el-upload
                class="avatar-uploader"
                :action="uploadUrl"
                :show-file-list="false"
                :on-success="handleUploadSuccess"
                :before-upload="beforeUpload"
              >
                <el-image
                  v-if="form.pictureurl"
                  :src="getImageUrl(form.pictureurl)"
                  class="avatar"
                  fit="cover"
                />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="新闻摘要" prop="summary">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入新闻摘要"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="新闻内容" prop="content">
          <div class="editor-container">
            <Toolbar
              :editor="editor"
              :mode="'default'"
              style="border-bottom: 1px solid #ccc"
            />
            <Editor
              v-model="form.content"
              :default-config="editorConfig"
              :mode="'default'"
              style="height: 400px; overflow-y: hidden;"
              @onCreated="handleEditorCreated"
              @onDestroyed="handleEditorDestroyed"
              @onChange="handleEditorChange"
            />
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '更新' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="新闻详情"
      width="70%"
    >
      <div v-if="currentNews" class="news-detail">
        <div class="detail-header">
          <h2>{{ currentNews.title }}</h2>
          <div class="detail-meta">
            <el-tag :type="getTypeTagType(currentNews.type)">{{ currentNews.type }}</el-tag>
            <span class="time">{{ formatDate(currentNews.time) }}</span>
          </div>
        </div>

        <div class="detail-cover">
          <el-image
            v-if="currentNews.pictureurl"
            :src="getImageUrl(currentNews.pictureurl)"
            fit="cover"
            style="width: 100%; max-height: 300px; border-radius: 8px;"
          />
        </div>

        <div class="detail-summary">
          <h3>新闻摘要</h3>
          <p>{{ currentNews.summary }}</p>
        </div>

        <div class="detail-content">
          <h3>新闻内容</h3>
          <div class="rich-content" v-html="currentNews.content"></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import request from '@/utils/request'
import { getApiBaseUrl } from '@/utils/apiConfig'

// 类型定义 - 适配后端实体类字段
interface News {
  id?: number
  title: string
  type: string
  time: string
  summary: string
  content: string
  pictureurl: string
}

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const filterYear = ref('')
const filterType = ref('')

const newsList = ref<News[]>([])
const currentNews = ref<News | null>(null)
const formRef = ref<FormInstance>()
const editor = ref<any>(null)

// 表单数据 - 适配后端实体类字段
const form = reactive<News>({
  title: '',
  type: '',
  time: '',
  summary: '',
  content: '',
  pictureurl: ''
})

// 表单验证规则
const rules: FormRules = {
  title: [
    { required: true, message: '请输入新闻标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择新闻类型', trigger: 'change' }
  ],
  time: [
    { required: true, message: '请选择发布时间', trigger: 'change' }
  ],
  summary: [
    { required: false, message: '请输入新闻摘要', trigger: 'blur' },
    { min: 1, max: 200, message: '摘要长度在 1 到 200 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入新闻内容', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑新闻' : '添加新闻')
const uploadUrl = computed(() => `${getApiBaseUrl()}/upload/file`)
const pagedNewsList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return newsList.value.slice(start, start + pageSize.value)
})

// 选项数据
const yearOptions = computed(() => {
  const currentYear = new Date().getFullYear()
  return Array.from({ length: 10 }, (_, i) => currentYear - i)
})

const typeOptions = [
  '实验室动态', '科研成果', '学术活动', '项目动态', '荣誉奖项',
  '团队建设', '学术交流', '文体活动', '外出考察', '活动通知', '其他'
]

// 工具函数
const getImageUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `${getApiBaseUrl()}${url.startsWith('/') ? '' : '/'}${url}`
}

const formatDate = (date: string) => {
  if (!date) return ''
  return date.split('T')[0]
}

const getTypeTagType = (type: string) => {
  const typeMap: { [key: string]: string } = {
    '公司新闻': 'success',
    '行业动态': 'primary',
    '技术分享': 'warning',
    '产品更新': 'info',
    '活动通知': 'danger',
    '其他': 'info'
  }
  return typeMap[type] || 'info'
}

// 获取新闻列表
const fetchNewsList = async () => {
  loading.value = true
  try {
    let url = `/news/list`
    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    }

    if (searchKeyword.value) {
      url = `/news/search`
      params.keyword = searchKeyword.value
    } else if (filterYear.value) {
      url = `/news/list/year/${filterYear.value}`
    } else if (filterType.value) {
      url = `/news/list/type/${filterType.value}`
    }

    const response = await request.get(url, { params })

    if (response.code == 200) {
      newsList.value = response.data || []
      total.value = response.data?.length || 0
    } else {
      ElMessage.error(response.message || '获取新闻列表失败')
    }
  } catch (error) {
    console.error('获取新闻列表失败:', error)
    ElMessage.error('网络错误，请检查后端服务是否启动')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchNewsList()
}

// 处理筛选
const handleFilter = () => {
  currentPage.value = 1
  fetchNewsList()
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  filterYear.value = ''
  filterType.value = ''
  currentPage.value = 1
  fetchNewsList()
}

// 刷新列表
const refreshList = () => {
  fetchNewsList()
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchNewsList()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchNewsList()
}

// 添加新闻
const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    title: '',
    type: '',
    time: '',
    summary: '',
    content: '',
    pictureurl: ''
  })
  dialogVisible.value = true
  nextTick(() => {
    if (formRef.value) {
      formRef.value.clearValidate()
    }
    if (editor.value) {
      editor.value.clear()
    }
  })
}

// 编辑新闻
const handleEdit = (row: News) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
  nextTick(() => {
    if (formRef.value) {
      formRef.value.clearValidate()
    }
    if (editor.value) {
      editor.value.setHtml(row.content)
    }
  })
}

// 查看新闻
const handleView = (row: News) => {
  currentNews.value = row
  viewDialogVisible.value = true
}

// 删除新闻
const handleDelete = async (row: News) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除新闻"${row.title}"吗？此操作不可恢复。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await request.delete(`/news/delete/${row.id}`)

    if (response.code == 200) {
      ElMessage.success('删除成功')
      fetchNewsList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    // 用户取消删除
  }
}

// 编辑器配置
const editorConfig = {
  placeholder: '请输入新闻内容...',
  MENU_CONF: {
    uploadImage: {
      server: `${getApiBaseUrl()}/upload/file`,
      fieldName: 'file',
      maxFileSize: 2 * 1024 * 1024, // 2MB
      allowedFileTypes: ['image/jpeg', 'image/png', 'image/gif', 'image/jpg'],
      customInsert(res: any, insertFn: any) {
        console.log('上传响应:', res)
        if (res && res.code == 200) {
          const imageUrl = res.data
          if (imageUrl) {
            insertFn(imageUrl, '', imageUrl)
          } else {
            ElMessage.error('图片上传失败: 返回的URL为空')
          }
        } else {
          const errorMsg = res?.message || '未知错误'
          ElMessage.error('图片上传失败: ' + errorMsg)
        }
      },
      timeout: 30000,
      withCredentials: false,
      onError(file: File, err: any, res: any) {
        console.error('上传错误:', err)
        ElMessage.error('图片上传失败: ' + (err?.message || '网络错误'))
      },
      onProgress(progress: number) {
        console.log('上传进度:', progress)
      }
    }
  },
  readOnly: false,
  autoFocus: false,
  scroll: true
}

// 编辑器创建
const handleEditorCreated = (editorInstance: any) => {
  editor.value = editorInstance
  console.log('编辑器创建成功:', editorInstance)
}

// 编辑器销毁
const handleEditorDestroyed = () => {
  console.log('编辑器销毁')
}

// 编辑器内容变化
const handleEditorChange = (editor: any) => {
  console.log('编辑器内容变化:', editor.getHtml())
}

// 图片上传成功
const handleUploadSuccess = (response: any) => {
  if (response.code == 200) {
    form.pictureurl = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

// 上传前验证
const beforeUpload = (file: File) => {
  const isJPGOrPNG = file.type == 'image/jpeg' || file.type == 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPGOrPNG) {
    ElMessage.error('图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 对话框关闭
const handleClose = (done: () => void) => {
  ElMessageBox.confirm('确定要关闭吗？未保存的内容将会丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    done()
  }).catch(() => {
    // 取消关闭
  })
}

// 表单提交
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    submitting.value = true

    const url = isEdit.value ? '/news/update' : '/news/add'
    const method = isEdit.value ? 'put' : 'post'

    const response = await request[method](url, form)

    if (response.code == 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      fetchNewsList()
    } else {
      ElMessage.error(response.message || (isEdit.value ? '更新失败' : '添加失败'))
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('网络错误，请检查后端服务是否启动')
  } finally {
    submitting.value = false
  }
}

// 生命周期
onMounted(() => {
  fetchNewsList()
})

// 组件卸载时销毁编辑器
import { onBeforeUnmount } from 'vue'
onBeforeUnmount(() => {
  if (editor.value == null) return
  editor.value.destroy()
})
</script>

<style scoped>
.news-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.news-list {
  margin-top: 20px;
}

.cover-image {
  display: flex;
  justify-content: center;
  align-items: center;
}

.no-image {
  width: 80px;
  height: 60px;
  background: #f5f7fa;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 12px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.news-form {
  max-height: 70vh;
  overflow-y: auto;
}

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;
  }

  :deep(.el-upload:hover) {
    border-color: #409eff;
  }
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 148px;
  height: 148px;
  line-height: 148px;
  text-align: center;
}

.avatar {
  width: 148px;
  height: 148px;
  display: block;
  object-fit: cover;
}

.news-detail {
  padding: 20px;
}

.detail-header {
  text-align: center;
  margin-bottom: 20px;
}

.detail-header h2 {
  color: #303133;
  margin-bottom: 10px;
}

.detail-meta {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.detail-meta .time {
  color: #909399;
  font-size: 14px;
}

.detail-cover {
  margin-bottom: 20px;
}

.detail-summary {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
}

.detail-summary h3 {
  color: #303133;
  margin-bottom: 10px;
}

.detail-content h3 {
  color: #303133;
  margin-bottom: 15px;
}

.rich-content {
  line-height: 1.6;
  color: #606266;
}

.rich-content :deep(p) {
  margin-bottom: 1em;
}

.rich-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
}
</style>
