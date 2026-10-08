<template>
  <div class="lectures-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h2 class="page-title">报告讲座管理</h2>
            <span class="page-subtitle">管理学术报告和讲座信息</span>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="handleAdd" :icon="Plus">
              添加讲座
            </el-button>
            <el-button @click="refreshData" :icon="Refresh">
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
      <div class="filter-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索讲座标题、主讲人、主持人..."
              clearable
              @input="handleSearch"
              @clear="handleSearchClear"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD HH:mm:ss"
              @change="handleDateFilter"
              style="width: 100%"
            />
          </el-col>
          <el-col :span="4">
            <el-button @click="resetFilters" :icon="Refresh">
              重置筛选
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 讲座列表 -->
      <div class="table-section">
        <el-table
          :data="pagedLectureList"
          v-loading="loading"
          stripe
          border
          style="width: 100%"
          :default-sort="{ prop: 'time', order: 'descending' }"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="title" label="讲座标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="speaker" label="主讲人" width="120" align="center" />
          <el-table-column prop="speakerfrom" label="主讲人单位" min-width="150" show-overflow-tooltip />
          <el-table-column prop="time" label="讲座时间" width="180" align="center" sortable>
            <template #default="{ row }">
              {{ formatDateTime(row.time) }}
            </template>
          </el-table-column>
          <el-table-column prop="address" label="讲座地点" width="120" align="center" />
          <el-table-column prop="host" label="主持人" width="100" align="center" />
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                :icon="View"
                @click="handleView(row)"
                link
              >
                查看
              </el-button>
              <el-button
                type="warning"
                size="small"
                :icon="Edit"
                @click="handleEdit(row)"
                link
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                :icon="Delete"
                @click="handleDelete(row.id)"
                link
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-section">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 添加/编辑讲座对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        label-position="right"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="讲座标题" prop="title">
              <el-input v-model="formData.title" placeholder="请输入讲座标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主讲人" prop="speaker">
              <el-input v-model="formData.speaker" placeholder="请输入主讲人姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="主讲人单位" prop="speakerfrom">
              <el-input v-model="formData.speakerfrom" placeholder="请输入主讲人单位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="讲座时间" prop="time">
              <el-date-picker
                v-model="formData.time"
                type="datetime"
                placeholder="选择讲座时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="讲座地点" prop="address">
              <el-input v-model="formData.address" placeholder="请输入讲座地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主持人" prop="host">
              <el-input v-model="formData.host" placeholder="请输入主持人姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="讲座简介" prop="lectureintroduction">
          <el-input
            v-model="formData.lectureintroduction"
            type="textarea"
            :autosize="{ minRows: 4, maxRows: 12 }"
            placeholder="请输入讲座简介"
          />
        </el-form-item>

        <el-form-item label="主讲人简介" prop="speakerintroduction">
          <el-input
            v-model="formData.speakerintroduction"
            type="textarea"
            :autosize="{ minRows: 4, maxRows: 12 }"
            placeholder="请输入主讲人简介"
          />
        </el-form-item>

        <el-form-item label="讲座图片" prop="pictureurl">
          <div class="upload-section">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="handleUpload"
              accept="image/*"
            >
              <img v-if="formData.pictureurl" :src="formData.pictureurl" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              <div v-if="uploading" class="uploading-overlay">
                <el-icon class="loading-icon"><Loading /></el-icon>
                <span>上传中...</span>
              </div>
            </el-upload>
            <div class="upload-tips">
              <p>支持 JPG、PNG、GIF 格式，大小不超过 2MB</p>
              <div v-if="formData.pictureurl" class="image-actions">
                <el-button type="text" @click="clearImage">清除图片</el-button>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ dialogType == 'add' ? '添加' : '更新' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看讲座详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="讲座详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="讲座标题">{{ currentLecture?.title }}</el-descriptions-item>
        <el-descriptions-item label="主讲人">{{ currentLecture?.speaker }}</el-descriptions-item>
        <el-descriptions-item label="主讲人单位">{{ currentLecture?.speakerfrom }}</el-descriptions-item>
        <el-descriptions-item label="讲座时间">{{ formatDateTime(currentLecture?.time || '') }}</el-descriptions-item>
        <el-descriptions-item label="讲座地点">{{ currentLecture?.address }}</el-descriptions-item>
        <el-descriptions-item label="主持人">{{ currentLecture?.host }}</el-descriptions-item>
        <el-descriptions-item label="讲座简介" :span="2">
          {{ currentLecture?.lectureintroduction || '暂无简介' }}
        </el-descriptions-item>
        <el-descriptions-item label="主讲人简介" :span="2">
          {{ currentLecture?.speakerintroduction || '暂无简介' }}
        </el-descriptions-item>
        <el-descriptions-item label="讲座图片" :span="2">
          <img
            v-if="currentLecture?.pictureurl"
            :src="currentLecture.pictureurl"
            class="lecture-image"
            style="max-width: 100%; max-height: 200px;"
          />
          <span v-else>暂无图片</span>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Refresh,
  Search,
  Edit,
  View,
  Delete,
  Loading
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import type { UploadRequestOptions } from 'element-plus'

// 讲座数据类型定义
interface Lecture {
  id?: number
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
const loading = ref(false)
const searchKeyword = ref('')
const dateRange = ref<string[]>([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const formRef = ref<FormInstance>()

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 表单数据
const formData = reactive<Lecture>({
  title: '',
  speaker: '',
  speakerfrom: '',
  time: '',
  address: '',
  host: '',
  lectureintroduction: '',
  speakerintroduction: '',
  pictureurl: ''
})

// 当前操作的讲座
const currentLecture = ref<Lecture | null>(null)
const dialogType = ref<'add' | 'edit'>('add')

// 讲座列表
const lectureList = ref<Lecture[]>([])

// 计算属性
const dialogTitle = computed(() => {
  return dialogType.value == 'add' ? '添加讲座' : '编辑讲座'
})
const pagedLectureList = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  return lectureList.value.slice(start, start + pagination.pageSize)
})

// 表单验证规则
const formRules: FormRules = {
  title: [
    { required: true, message: '请输入讲座标题', trigger: 'blur' },
    { min: 1, max: 200, message: '标题长度在 1 到 200 个字符', trigger: 'blur' }
  ],
  speaker: [
    { required: true, message: '请输入主讲人姓名', trigger: 'blur' }
  ],
  speakerfrom: [
    { required: true, message: '请输入主讲人单位', trigger: 'blur' }
  ],
  time: [
    { required: true, message: '请选择讲座时间', trigger: 'change' }
  ],
  address: [
    { required: true, message: '请输入讲座地点', trigger: 'blur' }
  ]
}

// 生命周期
onMounted(() => {
  fetchLectures()
})

// 方法：获取讲座列表
const fetchLectures = async () => {
  loading.value = true
  try {
    const response = await request.get('/lecture/list')

    if (response.code == 200) {
      lectureList.value = response.data || []
      pagination.total = lectureList.value.length
    } else {
      ElMessage.error('获取讲座列表失败: ' + response.message)
    }
  } catch (error) {
    console.error('获取讲座列表失败:', error)
    ElMessage.error('获取讲座列表失败')
  } finally {
    loading.value = false
  }
}

// 方法：搜索讲座
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    fetchLectures()
    return
  }

  try {
    const response = await request.get('/lecture/search', {
      params: { keyword: searchKeyword.value.trim() }
    })

    if (response.code == 200) {
      lectureList.value = response.data || []
      pagination.total = lectureList.value.length
      pagination.currentPage = 1
    } else {
      ElMessage.error('搜索失败: ' + response.message)
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败')
  }
}

// 方法：按时间筛选
const handleDateFilter = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    fetchLectures()
    return
  }

  try {
    const [startTime, endTime] = dateRange.value
    const response = await request.get('/lecture/filter/time', {
      params: { startTime, endTime }
    })

    if (response.code == 200) {
      lectureList.value = response.data || []
      pagination.total = lectureList.value.length
      pagination.currentPage = 1
    } else {
      ElMessage.error('筛选失败: ' + response.message)
    }
  } catch (error) {
    console.error('筛选失败:', error)
    ElMessage.error('筛选失败')
  }
}

// 方法：添加讲座
const handleAdd = () => {
  dialogType.value = 'add'
  Object.assign(formData, {
    title: '',
    speaker: '',
    speakerfrom: '',
    time: '',
    address: '',
    host: '',
    lectureintroduction: '',
    speakerintroduction: '',
    pictureurl: ''
  })
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 方法：编辑讲座
const handleEdit = (lecture: Lecture) => {
  dialogType.value = 'edit'
  Object.assign(formData, lecture)
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 方法：查看讲座详情
const handleView = (lecture: Lecture) => {
  currentLecture.value = lecture
  viewDialogVisible.value = true
}

// 方法：删除讲座
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个讲座吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await request.delete(`/lecture/delete/${id}`)

    if (response.code == 200) {
      ElMessage.success('删除讲座成功')
      fetchLectures()
    } else {
      ElMessage.error('删除讲座失败: ' + response.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除讲座失败:', error)
      ElMessage.error('删除讲座失败')
    }
  }
}

// 方法：提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    let response
    if (dialogType.value == 'add') {
      response = await request.post('/lecture/add', formData)
    } else {
      console.log(formData)
      response = await request.put('/lecture/update', formData)
    }

    if (response.code == 200) {
      ElMessage.success(dialogType.value == 'add' ? '添加讲座成功' : '更新讲座成功')
      dialogVisible.value = false
      fetchLectures()
    } else {
      ElMessage.error((dialogType.value == 'add' ? '添加' : '更新') + '讲座失败: ' + response.message)
    }
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

// 方法：关闭对话框
const handleDialogClose = () => {
  dialogVisible.value = false
  formRef.value?.clearValidate()
}

// 方法：刷新数据
const refreshData = () => {
  searchKeyword.value = ''
  dateRange.value = []
  pagination.currentPage = 1
  fetchLectures()
  ElMessage.success('数据已刷新')
}

// 方法：重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  dateRange.value = []
  pagination.currentPage = 1
  fetchLectures()
  ElMessage.success('筛选条件已重置')
}

// 方法：清空搜索
const handleSearchClear = () => {
  fetchLectures()
}

// 方法：分页大小改变
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
}

// 方法：当前页改变
const handleCurrentChange = (page: number) => {
  pagination.currentPage = page
}

// 方法：格式化日期时间
const formatDateTime = (dateTimeString: string) => {
  if (!dateTimeString) return ''
  const date = new Date(dateTimeString)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 图片上传相关方法
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleUpload = async (options: UploadRequestOptions) => {
  const formData1 = new FormData()
  formData1.append('file', options.file)

  uploading.value = true

  try {
    const response = await request.post('/upload/file', formData1, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (response.code == 200) {
      ElMessage.success('图片上传成功')
      console.log('图片上传成功:', response.data)
      formData.pictureurl = response.data
      console.log(formData)
    } else {
      ElMessage.error('图片上传失败: ' + response.message)
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败')
  } finally {
    uploading.value = false
  }
}

const clearImage = () => {
  formData.pictureurl = ''
}
</script>

<style scoped>
.lectures-page {
  padding: 20px;
}

.page-card {
  min-height: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 {
  margin: 0;
  color: #303133;
}

.page-subtitle {
  color: #909399;
  font-size: 14px;
}

.filter-section {
  margin-bottom: 20px;
}

.table-section {
  margin-top: 20px;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 上传组件样式 */
.upload-section {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 148px;
  height: 148px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.uploading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #409eff;
}

.loading-icon {
  font-size: 24px;
  margin-bottom: 8px;
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.upload-tips {
  flex: 1;
}

.upload-tips p {
  margin: 0 0 8px 0;
  color: #909399;
  font-size: 12px;
}

.image-actions {
  margin-top: 8px;
}

.lecture-image {
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>
