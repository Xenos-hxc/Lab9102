<template>
  <div class="resolution-page">
    <div class="page-header">
      <div>
        <h1>分辨率设置</h1>
        <p>当屏幕分辨率与启用的规则完全匹配时，系统自动按指定系数缩放；未匹配时保持 100%。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增规则</el-button>
    </div>

    <el-row :gutter="20" class="summary-row">
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card shadow="never" class="summary-card current-screen">
          <div class="summary-icon"><el-icon><Monitor /></el-icon></div>
          <div>
            <span>当前屏幕分辨率</span>
            <strong>{{ screenInfo.width }} × {{ screenInfo.height }}</strong>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-icon green"><el-icon><Aim /></el-icon></div>
          <div>
            <span>当前匹配规则</span>
            <strong>{{ currentRule ? `${Number(currentRule.scale).toFixed(2)} 倍` : '系统默认 1.00 倍' }}</strong>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-icon orange"><el-icon><SetUp /></el-icon></div>
          <div>
            <span>已启用规则</span>
            <strong>{{ enabledCount }} / {{ rules.length }}</strong>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="rule-card">
      <template #header>
        <div class="card-header">
          <span>缩放规则</span>
          <el-button :icon="Refresh" @click="loadRules">刷新</el-button>
        </div>
      </template>
      <el-table :data="rules" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column label="分辨率" min-width="180">
          <template #default="{ row }">
            <span class="resolution-value">{{ row.width }} × {{ row.height }}</span>
            <el-tag v-if="isCurrentResolution(row)" size="small" type="success">当前屏幕</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="缩放系数" width="150" align="center">
          <template #default="{ row }">
            <el-tag :type="scaleTagType(row.scale)" effect="light">{{ Number(row.scale).toFixed(2) }} 倍</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="220">
          <template #default="{ row }">{{ row.description || '自定义分辨率' }}</template>
        </el-table-column>
        <el-table-column label="启用" width="100" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="saveToggle(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="removeRule(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分辨率规则' : '新增分辨率规则'" width="520px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="宽度" prop="width">
              <el-input-number v-model="form.width" :min="320" :max="10000" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="高度" prop="height">
              <el-input-number v-model="form.height" :min="240" :max="10000" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="缩放系数" prop="scale">
          <el-input-number v-model="form.scale" :min="0.5" :max="2" :step="0.05" :precision="2" controls-position="right" />
          <span class="scale-help">小于 1 缩小，大于 1 放大</span>
        </el-form-item>
        <el-form-item label="规则说明">
          <el-input v-model="form.description" maxlength="100" show-word-limit placeholder="例如：会议室 2K 显示器" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveRule">保存并应用</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Aim, Delete, Edit, Monitor, Plus, Refresh, SetUp } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import request from '@/utils/request'
import { refreshDisplayScale } from '@/utils/displayScale'

interface ResolutionRule {
  id?: number
  width: number
  height: number
  scale: number
  description: string
  enabled: boolean
}

const screenInfo = reactive({ width: window.screen.width, height: window.screen.height })
const rules = ref<ResolutionRule[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<ResolutionRule>({ width: 1920, height: 1080, scale: 1, description: '', enabled: true })
const formRules: FormRules = {
  width: [{ required: true, message: '请输入屏幕宽度', trigger: 'change' }],
  height: [{ required: true, message: '请输入屏幕高度', trigger: 'change' }],
  scale: [{ required: true, message: '请输入缩放系数', trigger: 'change' }]
}

const isCurrentResolution = (rule: ResolutionRule) => (
  (rule.width === screenInfo.width && rule.height === screenInfo.height)
  || (rule.width === screenInfo.height && rule.height === screenInfo.width)
)
const currentRule = computed(() => rules.value.find(rule => rule.enabled && isCurrentResolution(rule)))
const enabledCount = computed(() => rules.value.filter(rule => rule.enabled).length)
const scaleTagType = (scale: number) => Number(scale) < 1 ? 'warning' : Number(scale) > 1 ? 'success' : 'info'

const loadRules = async () => {
  loading.value = true
  try {
    const response = await request.get('/resolution-setting/list')
    if (response.code == 200) rules.value = response.data || []
    else ElMessage.error(response.data || '获取分辨率规则失败')
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  Object.assign(form, { id: undefined, width: screenInfo.width, height: screenInfo.height, scale: 1, description: '', enabled: true })
  dialogVisible.value = true
}
const openEdit = (row: ResolutionRule) => {
  Object.assign(form, row, { scale: Number(row.scale) })
  dialogVisible.value = true
}

const saveRule = async () => {
  if (!formRef.value || !(await formRef.value.validate())) return
  saving.value = true
  try {
    const response = await request.post('/resolution-setting/save', form)
    if (response.code == 200) {
      ElMessage.success('规则已保存并应用')
      dialogVisible.value = false
      await loadRules()
      await refreshDisplayScale()
    } else ElMessage.error(response.data || '保存失败')
  } finally {
    saving.value = false
  }
}

const saveToggle = async (row: ResolutionRule) => {
  const response = await request.post('/resolution-setting/save', row)
  if (response.code != 200) {
    row.enabled = !row.enabled
    ElMessage.error(response.data || '状态更新失败')
  }
  await refreshDisplayScale()
}

const removeRule = async (row: ResolutionRule) => {
  try {
    await ElMessageBox.confirm(`确定删除 ${row.width} × ${row.height} 的缩放规则吗？`, '删除确认', { type: 'warning' })
    const response = await request.delete(`/resolution-setting/${row.id}`)
    if (response.code == 200) {
      ElMessage.success('规则已删除')
      await loadRules()
      await refreshDisplayScale()
    } else ElMessage.error(response.data || '删除失败')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error('删除失败')
  }
}

onMounted(loadRules)
</script>

<style scoped>
.resolution-page { padding: 6px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.page-header h1 { margin: 0; color: #1f2937; font-size: 26px; }
.page-header p { margin: 8px 0 0; color: #64748b; }
.summary-row { margin-bottom: 20px; }
.summary-card { margin-bottom: 12px; border: 0; }
.summary-card :deep(.el-card__body) { display: flex; align-items: center; gap: 16px; }
.summary-icon { width: 50px; height: 50px; display: grid; place-items: center; border-radius: 14px; background: #e8f1ff; color: #2563eb; font-size: 25px; }
.summary-icon.green { background: #e8f8ef; color: #16a34a; }
.summary-icon.orange { background: #fff3e6; color: #ea7c16; }
.summary-card span { display: block; color: #64748b; font-size: 13px; }
.summary-card strong { display: block; color: #172033; font-size: 20px; margin-top: 4px; }
.rule-card { border: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: 600; }
.resolution-value { font-weight: 700; color: #24324a; margin-right: 10px; }
.scale-help { color: #94a3b8; font-size: 12px; margin-left: 12px; }
@media (max-width: 720px) {
  .page-header { gap: 16px; }
  .page-header p { display: none; }
  .resolution-page { padding: 0; }
}
</style>
