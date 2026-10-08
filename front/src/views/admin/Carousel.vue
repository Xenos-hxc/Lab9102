<template>
  <div class="carousel-admin">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>走马灯管理</h2>
      <p>管理首页轮播图展示内容</p>
    </div>

    <!-- 操作工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd" :icon="Plus">
        添加走马灯
      </el-button>
      <el-button @click="refreshData" :icon="Refresh">
        刷新
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-card class="data-card">
      <template #header>
        <div class="card-header">
          <span>走马灯列表</span>
          <el-tag type="info">共 {{ carouselList.length }} 项</el-tag>
        </div>
      </template>

      <el-table
        :data="carouselList"
        v-loading="loading"
        style="width: 100%"
        :border="true"
        stripe
      >
        <el-table-column type="index" label="序号" width="60" align="center" />

        <el-table-column label="预览" width="120" align="center">
          <template #default="scope">
            <div class="image-preview">
              <el-image
                v-if="scope.row.pictureurl"
                :src="scope.row.pictureurl"
                :preview-src-list="[scope.row.pictureurl]"
                fit="cover"
                style="width: 80px; height: 60px; border-radius: 4px;"
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-else class="image-placeholder">
                <el-icon><Picture /></el-icon>
                <span>无图片</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="scope">
            <span class="title-text">{{ scope.row.title }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="pictureurl" label="图片路径" min-width="300" show-overflow-tooltip />

        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleEdit(scope.row)"
              :icon="Edit"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.row.id)"
              :icon="Delete"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑走马灯' : '添加走马灯'"
      width="600px"
      :before-close="handleClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        label-position="left"
      >
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="formData.title"
            placeholder="请输入走马灯标题"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="图片" prop="pictureurl">
          <div class="upload-section">
            <el-upload
              class="avatar-uploader"
              :http-request="customUpload"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
            >
              <el-image
                v-if="formData.pictureurl"
                :src="formData.pictureurl"
                class="avatar"
                fit="cover"
              >
                <template #error>
                  <div class="avatar-uploader-icon">
                    <el-icon><Plus /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-else class="avatar-uploader-icon">
                <el-icon><Plus /></el-icon>
              </div>
            </el-upload>
            <div class="upload-tips">
              <p>建议尺寸：1200×400像素</p>
              <p>支持格式：JPG、PNG、GIF</p>
              <p>文件大小：不超过2MB</p>
            </div>
          </div>
        </el-form-item>

        <el-form-item v-if="formData.pictureurl" label="图片预览">
          <div class="preview-container">
            <el-image
              :src="formData.pictureurl"
              :preview-src-list="[formData.pictureurl]"
              style="width: 100%; max-height: 200px;"
              fit="contain"
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Refresh, Edit, Delete, Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 走马灯数据接口
interface CarouselItem {
  id?: number
  title: string
  pictureurl: string
}

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const carouselList = ref<CarouselItem[]>([])

// 表单数据
const formData = reactive<CarouselItem>({
  title: '',
  pictureurl: ''
})

// 表单验证规则
const formRules: FormRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 1, max: 50, message: '标题长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  pictureurl: [
    { required: true, message: '请上传图片', trigger: 'change' }
  ]
}

// 自定义上传方法
const customUpload = async (options: any) => {
  const { file, onSuccess, onError } = options

  try {
    const formData = new FormData()
    formData.append('file', file)

    const response = await request.post('/upload/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (response.code == 200) {
      onSuccess(response)
    } else {
      onError(new Error('上传失败: ' + response.msg))
    }
  } catch (error) {
    console.error('文件上传出错:', error)
    onError(new Error('上传失败，请检查网络连接'))
  }
}

// 上传配置
const uploadUrl = '/api/upload/avatar'
const uploadHeaders = {
  'Content-Type': 'multipart/form-data'
}

// 方法定义
const fetchCarouselList = async () => {
  try {
    loading.value = true
    const response = await request.get('/carousel/list')
    if (response.code == 200) {
      carouselList.value = response.data || []
    } else {
      ElMessage.error('获取走马灯列表失败')
    }
  } catch (error) {
    console.error('获取走马灯列表出错:', error)
    ElMessage.error('获取数据失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    title: '',
    pictureurl: ''
  })
  dialogVisible.value = true
}

const handleEdit = (item: CarouselItem) => {
  isEdit.value = true
  Object.assign(formData, { ...item })
  dialogVisible.value = true
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个走马灯吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await request.delete(`/carousel/${id}`)
    if (response.code == 200) {
      ElMessage.success('删除成功')
      fetchCarouselList()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    // 用户取消删除
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    if (isEdit.value) {
      // 更新走马灯
      const response = await request.put('/carousel/update', formData)
      if (response.code == 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        fetchCarouselList()
      } else {
        ElMessage.error('更新失败')
      }
    } else {
      // 添加走马灯
      const response = await request.post('/carousel/add', formData)
      if (response.code == 200) {
        ElMessage.success('添加成功')
        dialogVisible.value = false
        fetchCarouselList()
      } else {
        ElMessage.error('添加失败')
      }
    }
  } catch (error) {
    console.error('提交表单出错:', error)
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  dialogVisible.value = false
  formRef.value?.clearValidate()
}

const handleUploadSuccess = (response: any) => {
  if (response.code == 200) {
    formData.pictureurl = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

const beforeUpload = (file: File) => {
  const isJPG = file.type == 'image/jpeg' || file.type == 'image/png' || file.type == 'image/gif'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('图片只能是 JPG/PNG/GIF 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const refreshData = () => {
  fetchCarouselList()
}

// 生命周期
onMounted(() => {
  fetchCarouselList()
})
</script>

<style scoped>
.carousel-admin {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  color: #303133;
  margin-bottom: 8px;
}

.page-header p {
  color: #909399;
  font-size: 14px;
}

.toolbar {
  margin-bottom: 20px;
}

.data-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-preview {
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 80px;
  height: 60px;
  background: #f5f7fa;
  color: #909399;
  border-radius: 4px;
}

.image-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 80px;
  height: 60px;
  background: #f5f7fa;
  color: #909399;
  border-radius: 4px;
  font-size: 12px;
}

.title-text {
  font-weight: 500;
  color: #303133;
}

.upload-section {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
  width: 148px;
  height: 148px;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 148px;
  height: 148px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar {
  width: 148px;
  height: 148px;
  display: block;
}

.upload-tips {
  flex: 1;
}

.upload-tips p {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #909399;
}

.preview-container {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  background: #fafafa;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
