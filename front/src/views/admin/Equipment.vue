<template>
  <div class="equipment-admin-page">
    <div class="page-header">
      <div><h1>团队设备管理</h1><p>维护设备分类、图片、名称、引入时间与简介</p></div>
      <div class="header-actions">
        <el-button :icon="FolderOpened" @click="openCategoryManager">分类管理</el-button>
        <el-button :icon="Refresh" @click="loadData">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openAdd">添加设备</el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-input v-model="keyword" clearable :prefix-icon="Search" placeholder="搜索设备名称或简介" @input="resetPage" />
      <el-select v-model="filterCategory" clearable placeholder="全部分类" @change="resetPage">
        <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
        <el-option v-if="items.some(item => !item.categoryId)" label="未分类" value="unclassified" />
      </el-select>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="pagedEquipment" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="65" align="center" />
        <el-table-column label="设备图片" width="130" align="center">
          <template #default="{ row }">
            <el-image v-if="row.pictureUrl" :src="fileUrl(row.pictureUrl)" :preview-src-list="[fileUrl(row.pictureUrl)]" fit="cover" class="table-image" preview-teleported />
            <div v-else class="image-empty"><el-icon><Cpu /></el-icon></div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="设备名称" min-width="210" />
        <el-table-column prop="categoryName" label="设备分类" width="130" align="center">
          <template #default="{ row }"><el-tag effect="plain">{{ row.categoryName || '未分类' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="purchaseDate" label="引入时间" width="130" sortable />
        <el-table-column prop="introduction" label="设备简介" min-width="280" show-overflow-tooltip />
        <el-table-column label="操作" width="170" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="removeEquipment(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[8, 12, 20, 50]" :total="filteredEquipment.length" layout="total, sizes, prev, pager, next, jumper" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑团队设备' : '添加团队设备'" width="680px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="form.name" maxlength="150" show-word-limit placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择设备分类" style="width: 100%">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="引入时间" prop="purchaseDate">
          <el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" placeholder="选择设备引入日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="设备图片" prop="pictureUrl">
          <el-upload action="#" :show-file-list="false" :http-request="uploadImage" :before-upload="beforeUpload">
            <div class="upload-box">
              <img v-if="form.pictureUrl" :src="fileUrl(form.pictureUrl)" alt="设备图片" />
              <div v-else><el-icon><Plus /></el-icon><span>上传设备图片</span></div>
            </div>
          </el-upload>
          <div class="upload-tip">支持 JPG、PNG、WEBP，建议使用 16:10 横图，最大 5MB</div>
        </el-form-item>
        <el-form-item label="设备简介" prop="introduction">
          <el-input v-model="form.introduction" type="textarea" :rows="6" maxlength="2000" show-word-limit placeholder="说明设备配置、用途和适用的科研任务" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEquipment">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="categoryDialogVisible" title="设备分类管理" width="760px" destroy-on-close>
      <div class="category-editor">
        <el-input v-model="categoryForm.name" maxlength="50" placeholder="分类名称，如：服务器" />
        <el-input v-model="categoryForm.description" maxlength="255" placeholder="分类说明（选填）" />
        <el-input-number v-model="categoryForm.sortOrder" :min="0" :max="9999" controls-position="right" />
        <el-button type="primary" :loading="categorySaving" @click="saveCategory">{{ categoryForm.id ? '更新' : '添加' }}</el-button>
        <el-button v-if="categoryForm.id" @click="resetCategoryForm">取消编辑</el-button>
      </div>
      <el-table :data="categories" stripe class="category-table">
        <el-table-column prop="name" label="分类名称" width="140" />
        <el-table-column prop="description" label="分类说明" min-width="240" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="90" align="center" />
        <el-table-column label="设备数" width="90" align="center">
          <template #default="{ row }">{{ categoryEquipmentCount(row.id) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="editCategory(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="removeCategory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!categories.length" description="暂无分类，请先添加设备分类" :image-size="80" />
      <template #footer><el-button @click="categoryDialogVisible = false">完成</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, onMounted } from 'vue'
import { Cpu, Delete, Edit, FolderOpened, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadRequestOptions } from 'element-plus'
import request from '@/utils/request'
import { getApiBaseUrl } from '@/utils/apiConfig'

interface EquipmentCategory { id?: number; name: string; description: string; sortOrder: number }
interface Equipment {
  id?: number
  name: string
  categoryId?: number
  categoryName?: string
  purchaseDate: string
  introduction: string
  pictureUrl: string
}

const items = ref<Equipment[]>([])
const categories = ref<EquipmentCategory[]>([])
const loading = ref(false)
const saving = ref(false)
const categorySaving = ref(false)
const dialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const keyword = ref('')
const filterCategory = ref<number | 'unclassified' | ''>('')
const currentPage = ref(1)
const pageSize = ref(8)
const formRef = ref<FormInstance>()
const form = reactive<Equipment>({ name: '', categoryId: undefined, purchaseDate: '', introduction: '', pictureUrl: '' })
const categoryForm = reactive<EquipmentCategory>({ name: '', description: '', sortOrder: 0 })
const formRules: FormRules = {
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择设备分类', trigger: 'change' }],
  purchaseDate: [{ required: true, message: '请选择引入时间', trigger: 'change' }],
  pictureUrl: [{ required: true, message: '请上传设备图片', trigger: 'change' }],
  introduction: [{ required: true, message: '请输入设备简介', trigger: 'blur' }]
}

const filteredEquipment = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return items.value.filter(item => {
    const categoryMatches = !filterCategory.value
      || (filterCategory.value === 'unclassified' ? !item.categoryId : item.categoryId === filterCategory.value)
    return categoryMatches && (!text || item.name.toLowerCase().includes(text) || item.introduction?.toLowerCase().includes(text))
  })
})
const pagedEquipment = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredEquipment.value.slice(start, start + pageSize.value)
})
const resetPage = () => { currentPage.value = 1 }
const categoryEquipmentCount = (categoryId?: number) => items.value.filter(item => item.categoryId === categoryId).length

const fileUrl = (value: string) => {
  if (!value) return ''
  if (/^https?:\/\//i.test(value)) return value
  return `${getApiBaseUrl()}${value.startsWith('/') ? '' : '/'}${value}`
}

const loadData = async () => {
  loading.value = true
  try {
    const [equipmentResponse, categoryResponse] = await Promise.all([
      request.get('/equipment/list'),
      request.get('/equipment/categories')
    ])
    if (equipmentResponse.code == 200) items.value = equipmentResponse.data || []
    else ElMessage.error(equipmentResponse.data || '设备列表获取失败')
    if (categoryResponse.code == 200) categories.value = categoryResponse.data || []
    else ElMessage.error(categoryResponse.data || '设备分类获取失败')
  } finally { loading.value = false }
}

const openAdd = () => {
  if (!categories.value.length) {
    ElMessage.warning('请先添加至少一个设备分类')
    openCategoryManager()
    return
  }
  Object.assign(form, { id: undefined, name: '', categoryId: undefined, categoryName: undefined, purchaseDate: '', introduction: '', pictureUrl: '' })
  dialogVisible.value = true
}
const openEdit = (item: Equipment) => { Object.assign(form, item); dialogVisible.value = true }

const beforeUpload = (file: File) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('请选择图片文件'); return false }
  if (file.size > 5 * 1024 * 1024) { ElMessage.error('图片不能超过 5MB'); return false }
  return true
}
const uploadImage = async (options: UploadRequestOptions) => {
  const data = new FormData()
  data.append('file', options.file)
  try {
    const response = await request.post('/upload/file', data, { headers: { 'Content-Type': 'multipart/form-data' } })
    if (response.code == 200) {
      form.pictureUrl = response.data
      ElMessage.success('设备图片上传成功')
      options.onSuccess(response)
    } else throw new Error(response.data || '上传失败')
  } catch (error) {
    options.onError(error as any)
    ElMessage.error('设备图片上传失败')
  }
}

const saveEquipment = async () => {
  if (!formRef.value || !(await formRef.value.validate())) return
  saving.value = true
  try {
    const response = await request.post('/equipment/save', form)
    if (response.code == 200) {
      ElMessage.success(form.id ? '设备已更新' : '设备已添加')
      dialogVisible.value = false
      await loadData()
    } else ElMessage.error(response.data || response.msg || '保存失败')
  } finally { saving.value = false }
}
const removeEquipment = async (item: Equipment) => {
  try {
    await ElMessageBox.confirm(`确定删除设备“${item.name}”吗？`, '删除确认', { type: 'warning' })
    const response = await request.delete(`/equipment/${item.id}`)
    if (response.code == 200) { ElMessage.success('设备已删除'); await loadData() }
    else ElMessage.error(response.data || response.msg || '删除失败')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error('删除失败')
  }
}

const resetCategoryForm = () => Object.assign(categoryForm, { id: undefined, name: '', description: '', sortOrder: 0 })
const openCategoryManager = () => { resetCategoryForm(); categoryDialogVisible.value = true }
const editCategory = (category: EquipmentCategory) => Object.assign(categoryForm, category)
const saveCategory = async () => {
  if (!categoryForm.name.trim()) { ElMessage.warning('请输入分类名称'); return }
  categorySaving.value = true
  try {
    const response = await request.post('/equipment/category/save', categoryForm)
    if (response.code == 200) {
      ElMessage.success(categoryForm.id ? '分类已更新' : '分类已添加')
      resetCategoryForm()
      await loadData()
    } else ElMessage.error(response.data || response.msg || '分类保存失败')
  } finally { categorySaving.value = false }
}
const removeCategory = async (category: EquipmentCategory) => {
  try {
    await ElMessageBox.confirm(`确定删除分类“${category.name}”吗？`, '删除确认', { type: 'warning' })
    const response = await request.delete(`/equipment/category/${category.id}`)
    if (response.code == 200) {
      ElMessage.success('分类已删除')
      if (filterCategory.value === category.id) filterCategory.value = ''
      await loadData()
    } else ElMessage.error(response.data || response.msg || '分类删除失败')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error('分类删除失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.equipment-admin-page { padding: 6px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 18px; }
.page-header h1 { margin: 0; color: #1f2937; font-size: 26px; }
.page-header p { margin: 7px 0 0; color: #718096; }
.header-actions { display: flex; flex-wrap: wrap; gap: 10px; }
.filter-card, .table-card { border: 0; margin-bottom: 18px; }
.filter-card :deep(.el-card__body) { display: flex; gap: 12px; }
.filter-card .el-input { width: min(420px, 55%); }
.filter-card .el-select { width: 180px; }
.table-image { width: 96px; height: 62px; border-radius: 8px; }
.image-empty { width: 96px; height: 62px; display: grid; place-items: center; margin: auto; border-radius: 8px; background: #edf2f8; color: #8aa0ba; font-size: 28px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 20px; }
.upload-box { width: 320px; height: 190px; overflow: hidden; display: grid; place-items: center; border: 1px dashed #b9c7da; border-radius: 12px; background: #f7f9fc; color: #6c7f99; }
.upload-box img { width: 100%; height: 100%; object-fit: cover; }
.upload-box > div { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.upload-box .el-icon { font-size: 34px; }
.upload-tip { align-self: flex-end; margin-left: 12px; color: #94a3b8; font-size: 12px; }
.category-editor { display: grid; grid-template-columns: 150px minmax(190px, 1fr) 110px auto auto; gap: 10px; align-items: center; margin-bottom: 18px; padding: 16px; border-radius: 12px; background: #f5f8fd; }
.category-table { border-radius: 10px; overflow: hidden; }
@media (max-width: 900px) { .category-editor { grid-template-columns: 1fr 1fr; } }
@media (max-width: 760px) {
  .page-header, .filter-card :deep(.el-card__body) { flex-direction: column; gap: 12px; }
  .filter-card .el-input, .filter-card .el-select { width: 100%; }
  .category-editor { grid-template-columns: 1fr; }
}
</style>
