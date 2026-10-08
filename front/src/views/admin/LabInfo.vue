<template>
  <div class="lab-info-page">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h1 class="page-title">实验室信息管理</h1>
      <div class="action-buttons">
        <el-button type="primary" @click="handleEdit" :icon="Edit">
          {{ isEditing ? '保存信息' : '编辑信息' }}
        </el-button>
        <el-button v-if="isEditing" @click="handleCancel" :icon="Close">
          取消编辑
        </el-button>
        <el-button @click="handleRefresh" :icon="Refresh">
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 主要内容 -->
    <div v-else class="content-container">
      <!-- 基本信息卡片 -->
      <el-card class="info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><OfficeBuilding /></el-icon>
            <span>基本信息</span>
          </div>
        </template>

        <el-form :model="labinfo" label-width="120px" :disabled="!isEditing">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="实验室名称">
                <el-input v-model="labinfo.labName" placeholder="请输入实验室名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="所属大学">
                <el-input v-model="labinfo.university" placeholder="请输入所属大学" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="所属学院">
                <el-input v-model="labinfo.college" placeholder="请输入所属学院" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话">
                <el-input v-model="labinfo.phone" placeholder="请输入联系电话" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="实验室简介">
            <el-input
              v-model="labinfo.introduction"
              type="textarea"
              :rows="4"
              placeholder="请输入实验室简介"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 地址信息卡片 -->
      <el-card class="info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><Location /></el-icon>
            <span>地址信息</span>
          </div>
        </template>

        <el-form :model="labinfo" label-width="120px" :disabled="!isEditing">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="省份">
                <el-input v-model="labinfo.addressProvince" placeholder="请输入省份" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="城市">
                <el-input v-model="labinfo.addressCity" placeholder="请输入城市" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="区县">
                <el-input v-model="labinfo.addressDistrict" placeholder="请输入区县" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="街道地址">
                <el-input v-model="labinfo.addressStreet" placeholder="请输入街道地址" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="详细地址">
                <el-input v-model="labinfo.addressDetail" placeholder="请输入详细地址" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="地图链接">
            <el-input v-model="labinfo.mapUrl" placeholder="请输入地图链接" />
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 联系人员信息卡片 -->
      <el-card class="info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><User /></el-icon>
            <span>联系人员</span>
          </div>
        </template>

        <el-form :model="labinfo" label-width="120px" :disabled="!isEditing">
          <div v-for="n in 5" :key="n" class="contact-person">
            <el-divider v-if="n > 1">联系人 {{ n }}</el-divider>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="`联系人${n}姓名`">
                  <el-input
                    v-model="(labinfo as any)[`contactPerson${n}Name`]"
                    :placeholder="`请输入联系人${n}姓名`"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="`联系人${n}邮箱`">
                  <el-input
                    v-model="(labinfo as any)[`contactPerson${n}Email`]"
                    :placeholder="`请输入联系人${n}邮箱`"
                    type="email"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-form>
      </el-card>

      <!-- 工作时间信息卡片 -->
      <el-card class="info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><Clock /></el-icon>
            <span>工作时间</span>
          </div>
        </template>

        <el-form :model="labinfo" label-width="120px" :disabled="!isEditing">
          <el-form-item label="工作日时间">
            <el-input v-model="labinfo.workdayHours" placeholder="例如：9:00-18:00" />
          </el-form-item>
          <el-form-item label="周末时间">
            <el-input v-model="labinfo.weekendHours" placeholder="例如：10:00-16:00" />
          </el-form-item>
          <el-form-item label="节假日说明">
            <el-input
              v-model="labinfo.holidayNote"
              type="textarea"
              :rows="3"
              placeholder="请输入节假日安排说明"
            />
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="确认编辑"
      width="400px"
      :before-close="handleDialogClose"
    >
      <span>确定要保存对实验室信息的修改吗？</span>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmEdit" :loading="saving">
          确定保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Edit,
  Close,
  Refresh,
  OfficeBuilding,
  Location,
  User,
  Clock
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 实验室信息数据接口
interface Labinfo {
  id?: number
  labName: string
  university: string
  college: string
  phone: string
  addressProvince: string
  addressCity: string
  addressDistrict: string
  addressStreet: string
  addressDetail: string
  contactPerson1Name: string
  contactPerson1Email: string
  contactPerson2Name: string
  contactPerson2Email: string
  contactPerson3Name: string
  contactPerson3Email: string
  contactPerson4Name: string
  contactPerson4Email: string
  contactPerson5Name: string
  contactPerson5Email: string
  workdayHours: string
  weekendHours: string
  holidayNote: string
  mapUrl: string
  introduction: string
  createdAt?: string
  updatedAt?: string
}

// 响应式数据
const labinfo = ref<Labinfo>({
  labName: '',
  university: '',
  college: '',
  phone: '',
  addressProvince: '',
  addressCity: '',
  addressDistrict: '',
  addressStreet: '',
  addressDetail: '',
  contactPerson1Name: '',
  contactPerson1Email: '',
  contactPerson2Name: '',
  contactPerson2Email: '',
  contactPerson3Name: '',
  contactPerson3Email: '',
  contactPerson4Name: '',
  contactPerson4Email: '',
  contactPerson5Name: '',
  contactPerson5Email: '',
  workdayHours: '',
  weekendHours: '',
  holidayNote: '',
  mapUrl: '',
  introduction: ''
})

const loading = ref(false)
const isEditing = ref(false)
const editDialogVisible = ref(false)
const saving = ref(false)
const originalLabinfo = ref<Labinfo>({ ...labinfo.value })

// 获取实验室信息
const fetchLabinfo = async () => {
  try {
    loading.value = true
    const response = await request.get('/labinfo/detail')

    if (response.code === 200) {
      const data = response.data
      if (data) {
        // 更新数据，确保所有字段都有值
        Object.keys(labinfo.value).forEach(key => {
          if (data[key] !== undefined && data[key] !== null) {
            ;(labinfo.value as any)[key] = data[key]
          }
        })
        originalLabinfo.value = { ...labinfo.value }
      } else {
        ElMessage.warning('实验室信息不存在，请先创建信息')
      }
    } else {
      ElMessage.error('获取实验室信息失败: ' + response.message)
    }
  } catch (error) {
    console.error('获取实验室信息出错:', error)
    ElMessage.error('获取实验室信息出错')
  } finally {
    loading.value = false
  }
}

// 编辑信息
const handleEdit = () => {
  if (isEditing.value) {
    // 检查是否有修改
    const hasChanges = JSON.stringify(labinfo.value) !== JSON.stringify(originalLabinfo.value)
    if (hasChanges) {
      editDialogVisible.value = true
    } else {
      isEditing.value = false
      ElMessage.info('没有检测到修改')
    }
  } else {
    isEditing.value = true
  }
}

// 取消编辑
const handleCancel = () => {
  ElMessageBox.confirm('确定要取消编辑吗？所有未保存的修改将会丢失。', '确认取消', {
    confirmButtonText: '确定',
    cancelButtonText: '继续编辑',
    type: 'warning'
  }).then(() => {
    // 恢复原始数据
    labinfo.value = { ...originalLabinfo.value }
    isEditing.value = false
    ElMessage.info('已取消编辑')
  }).catch(() => {
    // 用户点击了取消按钮，继续编辑
  })
}

// 确认编辑
const confirmEdit = async () => {
  try {
    saving.value = true
    const response = await request.post('/labinfo/update', labinfo.value)

    if (response.code === 200) {
      ElMessage.success('实验室信息更新成功')
      originalLabinfo.value = { ...labinfo.value }
      isEditing.value = false
      editDialogVisible.value = false
    } else {
      ElMessage.error('更新失败: ' + response.message)
    }
  } catch (error) {
    console.error('更新实验室信息出错:', error)
    ElMessage.error('更新实验室信息出错')
  } finally {
    saving.value = false
  }
}

// 刷新数据
const handleRefresh = () => {
  fetchLabinfo()
  ElMessage.success('数据已刷新')
}

// 对话框关闭处理
const handleDialogClose = (done: () => void) => {
  ElMessageBox.confirm('确定要关闭对话框吗？未保存的修改将会丢失。', '确认关闭', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    done()
  }).catch(() => {
    // 取消关闭
  })
}

// 组件挂载时获取数据
onMounted(() => {
  fetchLabinfo()
})
</script>

<style scoped>
.lab-info-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-title {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.content-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card {
  border-radius: 8px;
  border: none;
}

.info-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px 8px 0 0;
  padding: 16px 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.loading-container {
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.contact-person {
  margin-bottom: 10px;
}

.contact-person:last-child {
  margin-bottom: 0;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input) .el-input__inner,
:deep(.el-textarea) .el-textarea__inner {
  border-radius: 6px;
}

:deep(.el-divider) {
  margin: 20px 0;
}

:deep(.el-divider__text) {
  background-color: #f5f7fa;
  color: #606266;
  font-size: 14px;
}
</style>
