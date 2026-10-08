<template>
  <div class="research-directions-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h2 class="page-title">
              <el-icon><Compass /></el-icon>
              研究方向管理
            </h2>
            <p class="page-subtitle">管理实验室的研究方向分类和详细信息</p>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="handleAdd" :icon="Plus">
              添加研究方向
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
      <div class="search-section">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索研究方向名称"
              clearable
              :prefix-icon="Search"
              @input="handleSearch"
            />
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="levelFilter"
              placeholder="筛选层级"
              clearable
              @change="handleFilter"
            >
              <el-option label="父级研究方向" value="1" />
              <el-option label="子级研究方向" value="2" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-button type="info" :icon="Refresh" @click="refreshData">
              刷新
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 数据表格 -->
      <div class="table-section">
        <el-table
          :data="filteredDirections"
          v-loading="loading"
          stripe
          style="width: 100%"
          :default-sort="{ prop: 'id', order: 'ascending' }"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />

          <el-table-column label="研究方向图片" width="100" align="center">
            <template #default="{ row }">
              <div class="direction-image">
                <el-image
                  v-if="row.pictureurl"
                  :src="getImageUrl(row.pictureurl)"
                  :preview-src-list="[getImageUrl(row.pictureurl)]"
                  fit="cover"
                  class="direction-img"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div v-else class="no-image">
                  <el-icon><Picture /></el-icon>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="name" label="研究方向名称" min-width="150" sortable>
            <template #default="{ row }">
              <div class="direction-name">
                <span class="name-text">{{ row.name }}</span>
                <el-tag
                  v-if="row.level == 1"
                  type="primary"
                  size="small"
                  effect="light"
                >
                  父级
                </el-tag>
                <el-tag
                  v-else
                  type="success"
                  size="small"
                  effect="light"
                >
                  子级
                </el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="introduction" label="研究方向简介" min-width="200" show-overflow-tooltip />

          <el-table-column prop="level" label="层级" width="100" align="center" sortable>
            <template #default="{ row }">
              <span :class="['level-badge', row.level == 1 ? 'level-primary' : 'level-success']">
                {{ row.level == 1 ? '父级' : '子级' }}
              </span>
            </template>
          </el-table-column>

          <el-table-column prop="parentid" label="父级ID" width="100" align="center">
            <template #default="{ row }">
              <span v-if="row.level == 2">{{ row.parentid }}</span>
              <span v-else class="no-parent">-</span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="200" fixed="right" align="center">
            <template #default="{ row }">
              <el-button
                type="primary"
                link
                size="small"
                :icon="Edit"
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                link
                size="small"
                :icon="Delete"
                @click="handleDelete(row.id)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-section">
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

    <!-- 添加/编辑研究方向对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="directionFormRef"
        :model="directionForm"
        :rules="directionRules"
        label-width="100px"
      >
        <el-form-item label="研究方向名称" prop="name">
          <el-input
            v-model="directionForm.name"
            placeholder="请输入研究方向名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="研究方向层级" prop="level">
          <el-radio-group v-model="directionForm.level">
            <el-radio :label="1">父级研究方向</el-radio>
            <el-radio :label="2">子级研究方向</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="directionForm.level == 2" label="父级研究方向" prop="parentid">
          <el-select
            v-model="directionForm.parentid"
            placeholder="请选择父级研究方向"
            style="width: 100%"
          >
            <el-option
              v-for="parent in parentDirections"
              :key="parent.id"
              :label="parent.name"
              :value="parent.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="研究方向图片" prop="pictureurl">
          <el-upload
            class="avatar-uploader"
            action="/api/upload/avatar"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <el-image
              v-if="directionForm.pictureurl"
              :src="getImageUrl(directionForm.pictureurl)"
              class="avatar"
              fit="cover"
            />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议上传 300×200 像素的图片</div>
        </el-form-item>

        <el-form-item label="研究方向简介" prop="introduction">
          <el-input
            v-model="directionForm.introduction"
            type="textarea"
            :rows="4"
            placeholder="请输入研究方向简介"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            {{ isEdit ? '更新' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Search,
  Plus,
  Edit,
  Delete,
  Refresh,
  Compass,
  Picture
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 研究方向接口定义
interface Direction {
  id?: number
  name: string
  pictureurl: string
  introduction: string
  level: number
  parentid?: number
}

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const searchKeyword = ref('')
const levelFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 研究方向数据
const directions = ref<Direction[]>([])
const parentDirections = ref<Direction[]>([])

// 表单引用和表单数据
const directionFormRef = ref<FormInstance>()
const directionForm = reactive<Direction>({
  name: '',
  pictureurl: '',
  introduction: '',
  level: 1,
  parentid: undefined
})

// 表单验证规则
const directionRules: FormRules = {
  name: [
    { required: true, message: '请输入研究方向名称', trigger: 'blur' },
    { min: 2, max: 50, message: '研究方向名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  level: [
    { required: true, message: '请选择研究方向层级', trigger: 'change' }
  ],
  parentid: [
    { required: true, message: '请选择父级研究方向', trigger: 'change' }
  ],
  introduction: [
    { required: true, message: '请输入研究方向简介', trigger: 'blur' },
    { min: 10, max: 500, message: '研究方向简介长度在 10 到 500 个字符', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑研究方向' : '添加研究方向')
const filteredDirections = computed(() => {
  let filtered = directions.value

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(direction =>
      direction.name.toLowerCase().includes(keyword) ||
      direction.introduction.toLowerCase().includes(keyword)
    )
  }

  // 层级筛选
  if (levelFilter.value) {
    filtered = filtered.filter(direction => direction.level.toString() == levelFilter.value)
  }

  // 分页
  total.value = filtered.length
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filtered.slice(start, end)
})

// 方法
const getImageUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `${import.meta.env.VITE_API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`
}

// 获取研究方向数据
const fetchDirections = async () => {
  try {
    loading.value = true
    const response = await request.get('/direction/list')
    if (response.code == 200) {
      directions.value = response.data || []
    } else {
      ElMessage.error('获取研究方向数据失败')
    }
  } catch (error) {
    console.error('获取研究方向数据失败:', error)
    ElMessage.error('网络错误，请检查后端服务')
  } finally {
    loading.value = false
  }
}

// 获取父级研究方向
const fetchParentDirections = async () => {
  try {
    const response = await request.get('/direction/parent')
    if (response.code == 200) {
      parentDirections.value = response.data || []
    }
  } catch (error) {
    console.error('获取父级研究方向失败:', error)
  }
}

// 添加研究方向
const handleAdd = () => {
  fetchDirections()
  fetchParentDirections()
  isEdit.value = false
  Object.assign(directionForm, {
    name: '',
    pictureurl: '',
    introduction: '',
    level: 1,
    parentid: undefined
  })
  dialogVisible.value = true
}

// 编辑研究方向
const handleEdit = (direction: Direction) => {
  fetchDirections()
  fetchParentDirections()
  isEdit.value = true
  Object.assign(directionForm, { ...direction })
  dialogVisible.value = true
}

// 删除研究方向
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个研究方向吗？此操作不可恢复。',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    const response = await request.delete(`/direction/${id}`)
    if (response.code == 200) {
      ElMessage.success('删除成功')
      fetchDirections()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    console.error('删除研究方向失败:', error)
  }
}

// 提交表单
const submitForm = async () => {
  if (!directionFormRef.value) return

  try {
    await directionFormRef.value.validate()
    submitting.value = true

    const apiUrl = isEdit.value ? `/direction/save` : `/direction/save`
    const response = await request.post(apiUrl, directionForm)

    if (response.code == 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      fetchDirections()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('表单提交失败:', error)
  } finally {
    submitting.value = false
  }
}

// 头像上传处理
const handleAvatarSuccess = (response: any) => {
  if (response.code == 200) {
    directionForm.pictureurl = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

const beforeAvatarUpload = (file: File) => {
  const isJPGOrPNG = file.type == 'image/jpeg' || file.type == 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 100

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

// 搜索和筛选
const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

const refreshData = () => {
  searchKeyword.value = ''
  levelFilter.value = ''
  currentPage.value = 1
  fetchDirections()
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
}

// 生命周期
onMounted(() => {
  fetchDirections()
  fetchParentDirections()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
}

.table-section {
  margin-top: 20px;
}

.direction-image {
  display: flex;
  justify-content: center;
  align-items: center;
}

.direction-img {
  width: 60px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
}

.image-error, .no-image {
  width: 60px;
  height: 40px;
  background: #f3f4f6;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}

.direction-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-text {
  font-weight: 500;
}

.level-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.level-primary {
  background: #e0f2fe;
  color: #0369a1;
}

.level-success {
  background: #dcfce7;
  color: #166534;
}

.no-parent {
  color: #9ca3af;
  font-style: italic;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
  }

  :deep(.el-upload:hover) {
    border-color: #409eff;
  }
}

.avatar {
  width: 178px;
  height: 120px;
  display: block;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 120px;
  text-align: center;
  line-height: 120px;
}

.upload-tip {
  font-size: 12px;
  color: #6b7280;
  margin-top: 8px;
}
</style>
