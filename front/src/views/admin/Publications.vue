<template>
  <div class="publications-page">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">科研成果管理</span>
          <div class="header-actions">
            <el-radio-group v-model="activeTab" @change="handleTabChange">
              <el-radio-button label="papers">科研论文</el-radio-button>
              <el-radio-button label="projects">科研项目</el-radio-button>
            </el-radio-group>
            <el-button type="primary" @click="handleAdd" :icon="Plus">
              {{ activeTab == 'papers' ? '添加论文' : '添加项目' }}
            </el-button>
          </div>
        </div>
      </template>

      <!-- 科研论文管理 -->
      <div v-if="activeTab == 'papers'" class="tab-content">
        <!-- 搜索和筛选区域 -->
        <div class="filter-section">
          <el-row :gutter="20">
            <el-col :span="4">
              <el-input
                v-model="paperSearchParams.title"
                placeholder="搜索论文标题"
                clearable
                :prefix-icon="Search"
                @clear="handlePaperSearch"
                @keyup.enter="handlePaperSearch"
              />
            </el-col>
            <el-col :span="4">
              <el-input
                v-model="paperSearchParams.authors"
                placeholder="搜索作者"
                clearable
                :prefix-icon="User"
                @clear="handlePaperSearch"
                @keyup.enter="handlePaperSearch"
              />
            </el-col>
            <el-col :span="4">
              <el-input
                v-model="paperSearchParams.place"
                placeholder="搜索期刊/会议"
                clearable
                :prefix-icon="Location"
                @clear="handlePaperSearch"
                @keyup.enter="handlePaperSearch"
              />
            </el-col>
            <el-col :span="4">
              <el-select
                v-model="paperSearchParams.level"
                placeholder="论文级别"
                clearable
                style="width: 100%"
                @change="handlePaperSearch"
              >
                <el-option label="SCI" value="SCI" />
                <el-option label="EI" value="EI" />
                <el-option label="核心" value="核心" />
                <el-option label="普通" value="普通" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-button type="primary" @click="handlePaperSearch" :icon="Search">
                搜索
              </el-button>
              <el-button @click="handlePaperReset" :icon="Refresh">
                重置
              </el-button>
            </el-col>
          </el-row>

          <!-- 年份筛选 -->
          <div class="year-filter">
            <el-row :gutter="20" style="margin-top: 15px;">
              <el-col :span="8">
                <el-date-picker
                  v-model="paperYearRange"
                  type="yearrange"
                  range-separator="至"
                  start-placeholder="开始年份"
                  end-placeholder="结束年份"
                  value-format="YYYY"
                  @change="handlePaperYearFilter"
                />
              </el-col>
            </el-row>
          </div>
        </div>

        <!-- 论文表格 -->
        <el-table
          :data="papers"
          v-loading="paperLoading"
          style="width: 100%; margin-top: 20px;"
          :border="true"
          stripe
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="论文标题" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">
              <a href="javascript:void(0)" class="title-link" @click="handleTitleClick(row)" style="color: #409eff; text-decoration: underline;">
                {{ row.title }}
              </a>
            </template>
          </el-table-column>
          <el-table-column prop="authors" label="作者" width="105" show-overflow-tooltip />
          <el-table-column prop="time" label="发表年份" width="82" align="center">
            <template #default="{ row }">
              {{ row.time }}
            </template>
          </el-table-column>
          <el-table-column prop="place" label="期刊/会议" width="110" show-overflow-tooltip />
          <el-table-column prop="level" label="论文级别" width="88" align="center">
            <template #default="{ row }">
              <el-tag :type="getLevelTagType(row.level)" size="small">
                {{ row.level || '未设置' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="BibTeX" width="108" align="center">
            <template #default="{ row }">
              <div class="bib-table-cell">
                <el-tag :type="row.bib ? 'success' : 'info'" size="small" effect="plain">
                  {{ row.bib ? '已录入' : '未录入' }}
                </el-tag>
                <el-button
                  link
                  type="primary"
                  :icon="CopyDocument"
                  :disabled="!row.bib"
                  @click.stop="handleCopyBib(row.bib)"
                >
                  Copy
                </el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="205" align="center">
            <template #default="{ row }">
              <el-button
                link
                size="small"
                type="primary"
                :icon="Edit"
                @click="handlePaperEdit(row)"
              >
                编辑
              </el-button>
              <el-button
                link
                size="small"
                type="danger"
                :icon="Delete"
                @click="handlePaperDelete(row.id)"
              >
                删除
              </el-button>
              <el-button
                v-if="row.pdfurl"
                link
                size="small"
                type="success"
                @click="handlePreviewPaper(row)"
              >
                预览论文
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-section">
          <el-pagination
            v-model:current-page="paperPagination.currentPage"
            v-model:page-size="paperPagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="paperPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handlePaperSizeChange"
            @current-change="handlePaperCurrentChange"
          />
        </div>
      </div>

      <!-- 科研项目管理 -->
      <div v-if="activeTab == 'projects'" class="tab-content">
        <!-- 搜索和筛选区域 -->
        <div class="filter-section">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-input
                v-model="projectSearchParams.title"
                placeholder="搜索项目标题"
                clearable
                :prefix-icon="Search"
                @clear="handleProjectSearch"
                @keyup.enter="handleProjectSearch"
              />
            </el-col>
            <el-col :span="6">
              <el-input
                v-model="projectSearchParams.company"
                placeholder="搜索合作方"
                clearable
                :prefix-icon="User"
                @clear="handleProjectSearch"
                @keyup.enter="handleProjectSearch"
              />
            </el-col>
            <el-col :span="6">
              <el-select
                v-model="projectSearchParams.type"
                placeholder="项目类型"
                clearable
                style="width: 100%"
                @change="handleProjectSearch"
              >
                <el-option label="横向项目" value="横向项目" />
                <el-option label="纵向项目" value="纵向项目" />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-select
                v-model="projectSearchParams.status"
                placeholder="项目状态"
                clearable
                style="width: 100%"
                @change="handleProjectSearch"
              >
                <el-option label="进行中" value="ongoing" />
                <el-option label="已结题" value="completed" />
              </el-select>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 15px;">
            <el-col :span="24">
              <el-button type="primary" @click="handleProjectSearch" :icon="Search">
                搜索
              </el-button>
              <el-button @click="handleProjectReset" :icon="Refresh">
                重置
              </el-button>
            </el-col>
          </el-row>
        </div>

        <!-- 项目表格 -->
        <el-table
          :data="projects"
          v-loading="projectLoading"
          style="width: 100%; margin-top: 20px;"
          :border="true"
          stripe
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="title" label="项目标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="type" label="项目类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.type == '横向项目' ? 'primary' : 'success'">
                {{ row.type }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="company" label="合作方" width="150" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.company || '未指定' }}
            </template>
          </el-table-column>
          <el-table-column prop="starttime" label="开始时间" width="120" align="center">
            <template #default="{ row }">
              {{ formatDate(row.starttime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endtime" label="结束时间" width="120" align="center">
            <template #default="{ row }">
              {{ row.endtime ? formatDate(row.endtime) : '进行中' }}
            </template>
          </el-table-column>
          <el-table-column label="项目状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.endtime ? 'success' : 'warning'">
                {{ row.endtime ? '已结题' : '进行中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right" align="center">
            <template #default="{ row }">
              <el-button
                size="small"
                type="primary"
                :icon="Edit"
                @click="handleProjectEdit(row)"
              >
                编辑
              </el-button>
              <el-button
                size="small"
                type="danger"
                :icon="Delete"
                @click="handleProjectDelete(row.id)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-section">
          <el-pagination
            v-model:current-page="projectPagination.currentPage"
            v-model:page-size="projectPagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="projectPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleProjectSizeChange"
            @current-change="handleProjectCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 添加/编辑论文对话框 -->
    <el-dialog
      v-model="paperDialogVisible"
      :title="paperDialogTitle"
      width="760px"
      class="paper-dialog"
      :before-close="handlePaperClose"
    >
      <el-form
        ref="paperFormRef"
        :model="paperForm"
        :rules="paperRules"
        label-width="100px"
        style="padding: 0 20px;"
      >
        <el-form-item label="论文标题" prop="title">
          <el-input
            v-model="paperForm.title"
            placeholder="请输入论文标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="作者" prop="authors">
          <el-input
            v-model="paperForm.authors"
            placeholder="请输入作者（多个作者用逗号分隔）"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="发表年份" prop="time">
          <el-date-picker
            v-model="paperForm.time"
            type="year"
            placeholder="选择发表年份"
            value-format="YYYY"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="期刊/会议" prop="place">
          <el-input
            v-model="paperForm.place"
            placeholder="请输入发表的期刊或会议名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="论文级别" prop="level">
          <el-input
            v-model="paperForm.level"
            placeholder="多个级别请用英文逗号进行分隔"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="论文链接" prop="url">
          <el-input
            v-model="paperForm.url"
            placeholder="请输入论文在线链接"
            maxlength="500"
          />
        </el-form-item>
        <el-form-item label="PDF文件">
          <el-upload
            ref="paperUploadRef"
            :action="`http://localhost:9001/upload/paper`"

            :on-success="handlePdfUploadSuccess"
            :on-error="handlePdfUploadError"
            :before-upload="beforePdfUpload"
            :file-list="pdfFileList"
            :limit="1"
            :auto-upload="true"
          >
            <template #trigger>
              <el-button type="primary">选择PDF文件</el-button>
            </template>
            <template #tip>
              <div class="el-upload__tip">
                仅支持PDF格式文件，大小不超过50MB
              </div>
            </template>
          </el-upload>
          <div v-if="paperForm.pdfurl" class="pdf-preview">
            <el-link :href="paperForm.pdfurl" target="_blank" type="primary">预览已上传的PDF</el-link>
          </div>
        </el-form-item>
        <el-form-item label="BibTeX" prop="bib">
          <div class="bib-editor">
            <div class="bib-toolbar">
              <input
                ref="bibFileInputRef"
                type="file"
                accept=".bib,text/plain"
                class="hidden-bib-input"
                @change="handleBibFileChange"
              />
              <el-button :icon="Upload" @click="bibFileInputRef?.click()">
                上传 .bib 文件
              </el-button>
              <span class="bib-upload-tip">上传后自动读取文件内容，也可以直接在下方输入</span>
            </div>
            <el-input
              v-model="paperForm.bib"
              type="textarea"
              :rows="9"
              resize="vertical"
              placeholder="请输入或粘贴完整的 BibTeX 内容，例如：@article{...}"
            />
            <div class="bib-editor-footer">
              <span>{{ paperForm.bib?.length || 0 }} 个字符</span>
              <el-button
                link
                type="primary"
                :icon="CopyDocument"
                :disabled="!paperForm.bib"
                @click="handleCopyBib(paperForm.bib)"
              >
                复制当前内容
              </el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handlePaperClose">取消</el-button>
          <el-button type="primary" @click="handlePaperSubmit" :loading="paperSubmitting">
            {{ paperDialogType == 'add' ? '添加' : '保存' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- PDF预览对话框 -->
    <el-dialog
      v-model="pdfPreviewVisible"
      title="论文预览"
      width="90%"
      top="5vh"
      :before-close="handlePdfClose"
    >
      <div class="pdf-preview-container">
        <iframe
          v-if="currentPdfUrl"
          :src="currentPdfUrl"
          width="100%"
          height="600"
          frameborder="0"
        ></iframe>
        <div v-else class="no-pdf">
          <el-empty description="暂无PDF文件" :image-size="100" />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button v-if="currentPdfUrl" type="primary" @click="downloadPdf">
            <el-icon><Download /></el-icon>
            下载PDF
          </el-button>
          <el-button @click="handlePdfClose">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加/编辑项目对话框 -->
    <el-dialog
      v-model="projectDialogVisible"
      :title="projectDialogTitle"
      width="600px"
      :before-close="handleProjectClose"
    >
      <el-form
        ref="projectFormRef"
        :model="projectForm"
        :rules="projectRules"
        label-width="100px"
        style="padding: 0 20px;"
      >
        <el-form-item label="项目标题" prop="title">
          <el-input
            v-model="projectForm.title"
            placeholder="请输入项目标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="项目类型" prop="type">
          <el-select v-model="projectForm.type" placeholder="请选择项目类型" style="width: 100%">
            <el-option label="横向项目" value="横向项目" />
            <el-option label="纵向项目" value="纵向项目" />
          </el-select>
        </el-form-item>
        <el-form-item label="合作方">
          <el-input
            v-model="projectForm.company"
            placeholder="请输入合作方名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="开始时间" prop="starttime">
          <el-date-picker
            v-model="projectForm.starttime"
            type="date"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="projectForm.endtime"
            type="date"
            placeholder="选择结束时间（留空表示进行中）"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleProjectClose">取消</el-button>
          <el-button type="primary" @click="handleProjectSubmit" :loading="projectSubmitting">
            {{ projectDialogType == 'add' ? '添加' : '保存' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, User, Location, Refresh, Edit, Delete, Download, CopyDocument, Upload } from '@element-plus/icons-vue'
import request from "@/utils/request.ts";
import { copyPlainText } from '@/utils/clipboard'

// 论文接口
interface Paper {
  id?: number
  title: string
  authors: string
  time: string
  place: string
  level?: string
  url?: string
  pdfurl?: string
  bib?: string
}

// 项目接口
interface Project {
  id?: number
  title: string
  starttime: string
  endtime: string
  type: string
  company: string
}

// 响应式数据 - 论文
const paperLoading = ref(false)
const paperDialogVisible = ref(false)
const paperDialogType = ref<'add' | 'edit'>('add')
const paperSubmitting = ref(false)
const paperFormRef = ref<FormInstance>()
const paperYearRange = ref<string[]>([])

// 论文搜索参数
const paperSearchParams = reactive({
  title: '',
  authors: '',
  place: '',
  level: ''
})

// 论文分页参数
const paperPagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 论文表单数据
const paperForm = reactive<Paper>({
  title: '',
  authors: '',
  time: '',
  place: '',
  level: '',
  url: '',
  pdfurl: '',
  bib: ''
})

// 论文表单验证规则
const paperRules: FormRules = {
  title: [
    { required: true, message: '请输入论文标题', trigger: 'blur' },
    { min: 1, max: 200, message: '标题长度在 1 到 200 个字符', trigger: 'blur' }
  ],
  authors: [
    { required: true, message: '请输入作者', trigger: 'blur' },
    { min: 1, max: 100, message: '作者长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  time: [
    { required: true, message: '请选择发表年份', trigger: 'change' }
  ],
  place: [
    { required: true, message: '请输入期刊/会议名称', trigger: 'blur' },
    { min: 1, max: 100, message: '期刊/会议名称长度在 1 到 100 个字符', trigger: 'blur' }
  ]
}

// 响应式数据 - 项目
const projectLoading = ref(false)
const projectDialogVisible = ref(false)
const projectDialogType = ref<'add' | 'edit'>('add')
const projectSubmitting = ref(false)
const projectFormRef = ref<FormInstance>()

// 项目搜索参数
const projectSearchParams = reactive({
  title: '',
  type: '',
  status: '',
  company: ''
})

// 项目分页参数
const projectPagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 项目表单数据
const projectForm = reactive<Project>({
  title: '',
  starttime: '',
  endtime: '',
  type: '',
  company: ''
})

// 项目表单验证规则
const projectRules: FormRules = {
  title: [
    { required: true, message: '请输入项目标题', trigger: 'blur' },
    { min: 1, max: 200, message: '标题长度在 1 到 200 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择项目类型', trigger: 'change' }
  ],
  starttime: [
    { required: false, message: '请选择开始时间', trigger: 'change' }
  ],
  company: [
    { max: 100, message: '合作方名称长度不能超过 100 个字符', trigger: 'blur' }
  ]
}

// PDF上传相关
const paperUploadRef = ref()
const pdfFileList = ref<any[]>([])
const pdfPreviewVisible = ref(false)
const currentPdfUrl = ref('')
const bibFileInputRef = ref<HTMLInputElement>()

// 通用数据
const activeTab = ref<'papers' | 'projects'>('papers')
const papers = ref<Paper[]>([])
const projects = ref<Project[]>([])

// 计算属性
const paperDialogTitle = computed(() => {
  return paperDialogType.value == 'add' ? '添加科研论文' : '编辑科研论文'
})

const projectDialogTitle = computed(() => {
  return projectDialogType.value == 'add' ? '添加科研项目' : '编辑科研项目'
})

// 获取级别标签类型
const getLevelTagType = (level: string) => {
  const typeMap: Record<string, string> = {
    'SCI': 'danger',
    'EI': 'warning',
    '核心': 'success',
    '普通': 'info'
  }
  return typeMap[level] || 'info'
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN')
  } catch (error) {
    return dateString
  }
}

// 论文相关方法
// 获取论文列表（带搜索参数）
const fetchPapers = async () => {
  paperLoading.value = true
  try {
    const params: any = {}

    // 添加搜索参数
    if (paperSearchParams.title) params.title = paperSearchParams.title
    if (paperSearchParams.authors) params.authors = paperSearchParams.authors
    if (paperSearchParams.place) params.place = paperSearchParams.place
    if (paperSearchParams.level) params.level = paperSearchParams.level

    const result = await request({
      url: '/paper/list',
      method: 'GET',
      params: params
    })

    if (result.code == 200) {
      const allPapers = result.data || []
      paperPagination.total = allPapers.length
      // 前端分页处理
      const start = (paperPagination.currentPage - 1) * paperPagination.pageSize
      const end = start + paperPagination.pageSize
      papers.value = allPapers.slice(start, end)
    } else {
      ElMessage.error(result.message || '获取论文列表失败')
    }
  } catch (error) {
    console.error('获取论文列表失败:', error)
    ElMessage.error('获取论文列表失败')
  } finally {
    paperLoading.value = false
  }
}

// 根据年份筛选论文
const fetchPapersByYear = async (startYear: string, endYear: string) => {
  paperLoading.value = true
  try {
    const result = await request({
      url: `/paper/filter`,
      method: 'GET',
      params: {
        startYear: startYear,
        endYear: endYear
      }
    })

    if (result.code == 200) {
      const filteredPapers = result.data || []
      paperPagination.total = filteredPapers.length
      paperPagination.currentPage = 1
      // 前端分页处理
      const start = (paperPagination.currentPage - 1) * paperPagination.pageSize
      const end = start + paperPagination.pageSize
      papers.value = filteredPapers.slice(start, end)
    } else {
      ElMessage.error(result.message || '筛选论文失败')
    }
  } catch (error) {
    console.error('筛选论文失败:', error)
    ElMessage.error('筛选论文失败')
  } finally {
    paperLoading.value = false
  }
}

// 添加论文
const handlePaperAdd = async (paper: Paper) => {
  try {
    const result = await request({
      url: '/paper',
      method: 'POST',
      data: paper
    })

    if (result.code == 200) {
      ElMessage.success('添加论文成功')
      fetchPapers()
    } else {
      ElMessage.error(result.message || '添加论文失败')
    }
  } catch (error) {
    console.error('添加论文失败:', error)
    ElMessage.error('添加论文失败')
  }
}

// 编辑论文
const handlePaperUpdate = async (paper: Paper) => {
  try {
    const result = await request({
      url: '/paper',
      method: 'PUT',
      data: paper
    })

    if (result.code == 200) {
      ElMessage.success('编辑论文成功')
      fetchPapers()
    } else {
      ElMessage.error(result.message || '编辑论文失败')
    }
  } catch (error) {
    console.error('编辑论文失败:', error)
    ElMessage.error('编辑论文失败')
  }
}

// 删除论文
const handlePaperDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇论文吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const result = await request({
      url: `/paper/${id}`,
      method: 'DELETE'
    })

    if (result.code == 200) {
      ElMessage.success('删除论文成功')
      fetchPapers()
    } else {
      ElMessage.error(result.message || '删除论文失败')
    }
  } catch (error) {
    // 用户取消删除
  }
}

// 项目相关方法
// 获取项目列表（带搜索参数）
const fetchProjects = async () => {
  projectLoading.value = true
  try {
    const params: any = {}

    // 添加搜索参数
    if (projectSearchParams.title) params.title = projectSearchParams.title
    if (projectSearchParams.type) params.type = projectSearchParams.type
    if (projectSearchParams.company) params.company = projectSearchParams.company
    if (projectSearchParams.status) {
      if (projectSearchParams.status === 'ongoing') {
        params.endtime = '' // 结题时间为空表示进行中
      } else if (projectSearchParams.status === 'completed') {
        params.endtime = 'notnull' // 结题时间不为空表示已结题
      }
    }

    const result = await request({
      url: '/project/list',
      method: 'GET',
      params: params
    })

    if (result.code == 200) {
      const allProjects = result.data || []
      projectPagination.total = allProjects.length
      // 前端分页处理
      const start = (projectPagination.currentPage - 1) * projectPagination.pageSize
      const end = start + projectPagination.pageSize
      projects.value = allProjects.slice(start, end)
    } else {
      ElMessage.error(result.message || '获取项目列表失败')
    }
  } catch (error) {
    console.error('获取项目列表失败:', error)
    ElMessage.error('获取项目列表失败')
  } finally {
    projectLoading.value = false
  }
}

// 添加项目（正确处理结束时间为空的情况）
const handleProjectAdd = async (project: Project) => {
  try {
    // 对于LocalDate类型，传递null而不是空字符串
    const submitData = {
      ...project,
      endtime: project.endtime || null
    }

    const result = await request({
      url: '/project',
      method: 'POST',
      data: submitData
    })

    if (result.code == 200) {
      ElMessage.success('添加项目成功')
      fetchProjects()
    } else {
      ElMessage.error(result.message || '添加项目失败')
    }
  } catch (error) {
    console.error('添加项目失败:', error)
    ElMessage.error('添加项目失败')
  }
}

// 编辑项目（正确处理结束时间为空的情况）
const handleProjectUpdate = async (project: Project) => {
  try {
    // 对于LocalDate类型，传递null而不是空字符串
    const submitData = {
      ...project,
      endtime: project.endtime || null
    }

    const result = await request({
      url: '/project',
      method: 'PUT',
      data: submitData
    })

    if (result.code == 200) {
      ElMessage.success('编辑项目成功')
      fetchProjects()
    } else {
      ElMessage.error(result.message || '编辑项目失败')
    }
  } catch (error) {
    console.error('编辑项目失败:', error)
    ElMessage.error('编辑项目失败')
  }
}

// 删除项目
const handleProjectDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个项目吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const result = await request({
      url: `/project/${id}`,
      method: 'DELETE'
    })

    if (result.code == 200) {
      ElMessage.success('删除项目成功')
      fetchProjects()
    } else {
      ElMessage.error(result.message || '删除项目失败')
    }
  } catch (error) {
    // 用户取消删除
  }
}

// 事件处理
const handleTabChange = () => {
  if (activeTab.value == 'papers') {
    fetchPapers()
  } else {
    fetchProjects()
  }
}

const handleAdd = () => {
  if (activeTab.value == 'papers') {
    handlePaperDialogOpen('add')
  } else {
    handleProjectDialogOpen('add')
  }
}

// 论文对话框操作
const handlePaperDialogOpen = (type: 'add' | 'edit', row?: Paper) => {
  paperDialogType.value = type
  paperDialogVisible.value = true
  nextTick(() => {
    if (paperFormRef.value) {
      paperFormRef.value.resetFields()
      if (type == 'edit' && row) {
        Object.assign(paperForm, { ...row, bib: row.bib || '' })
        // 设置PDF文件列表
        if (row.pdfurl) {
          pdfFileList.value = [{ name: '已上传PDF', url: row.pdfurl }]
        } else {
          pdfFileList.value = []
        }
      } else {
        Object.assign(paperForm, {
          title: '',
          authors: '',
          time: '',
          place: '',
          level: '',
          url: '',
          pdfurl: '',
          bib: ''
        })
        pdfFileList.value = []
      }
    }
  })
}

const handlePaperEdit = (row: Paper) => {
  handlePaperDialogOpen('edit', row)
}

const handlePaperClose = () => {
  paperDialogVisible.value = false
  if (paperFormRef.value) {
    paperFormRef.value.resetFields()
  }
  pdfFileList.value = []
}

const handlePaperSubmit = async () => {
  if (!paperFormRef.value) return

  const valid = await paperFormRef.value.validate()
  if (!valid) return

  paperSubmitting.value = true
  try {
    if (paperDialogType.value == 'add') {
      await handlePaperAdd(paperForm)
    } else {
      await handlePaperUpdate(paperForm)
    }
    paperDialogVisible.value = false
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    paperSubmitting.value = false
  }
}

// 项目对话框操作
const handleProjectDialogOpen = (type: 'add' | 'edit', row?: Project) => {
  projectDialogType.value = type
  projectDialogVisible.value = true
  nextTick(() => {
    if (projectFormRef.value) {
      projectFormRef.value.resetFields()
      if (type == 'edit' && row) {
        // 确保结束时间为null而不是空字符串，这样日期选择器能正确显示为空
        Object.assign(projectForm, {
          ...row,
          endtime: row.endtime || null
        })
      } else {
        Object.assign(projectForm, {
          title: '',
          starttime: '',
          endtime: null,  // 使用null而不是空字符串
          type: '',
          company: ''
        })
      }
    }
  })
}

const handleProjectEdit = (row: Project) => {
  handleProjectDialogOpen('edit', row)
}

const handleProjectClose = () => {
  projectDialogVisible.value = false
  if (projectFormRef.value) {
    projectFormRef.value.resetFields()
  }
}

const handleProjectSubmit = async () => {
  if (!projectFormRef.value) return

  const valid = await projectFormRef.value.validate()
  if (!valid) return

  projectSubmitting.value = true
  try {
    if (projectDialogType.value == 'add') {
      await handleProjectAdd(projectForm)
    } else {
      await handleProjectUpdate(projectForm)
    }
    projectDialogVisible.value = false
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    projectSubmitting.value = false
  }
}

// 搜索和筛选
const handlePaperSearch = () => {
  paperPagination.currentPage = 1
  fetchPapers()
}

const handlePaperReset = () => {
  paperSearchParams.title = ''
  paperSearchParams.authors = ''
  paperSearchParams.place = ''
  paperSearchParams.level = ''
  paperYearRange.value = []
  paperPagination.currentPage = 1
  fetchPapers()
}

const handlePaperYearFilter = (value: string[]) => {
  if (value && value.length == 2) {
    fetchPapersByYear(value[0] || '', value[1] || '')
  } else {
    fetchPapers()
  }
}

const handleProjectSearch = () => {
  projectPagination.currentPage = 1
  fetchProjects()
}

const handleProjectReset = () => {
  projectSearchParams.title = ''
  projectSearchParams.type = ''
  projectSearchParams.status = ''
  projectSearchParams.company = ''
  projectPagination.currentPage = 1
  fetchProjects()
}

// 分页处理
const handlePaperSizeChange = (size: number) => {
  paperPagination.pageSize = size
  paperPagination.currentPage = 1
  fetchPapers()
}

const handlePaperCurrentChange = (page: number) => {
  paperPagination.currentPage = page
  fetchPapers()
}

const handleProjectSizeChange = (size: number) => {
  projectPagination.pageSize = size
  projectPagination.currentPage = 1
  fetchProjects()
}

const handleProjectCurrentChange = (page: number) => {
  projectPagination.currentPage = page
  fetchProjects()
}

// PDF上传相关方法
const beforePdfUpload = (file: any) => {
  const isPdf = file.type === 'application/pdf'
  const isLt50M = file.size / 1024 / 1024 < 50

  if (!isPdf) {
    ElMessage.error('只能上传PDF文件!')
    return false
  }
  if (!isLt50M) {
    ElMessage.error('PDF文件大小不能超过50MB!')
    return false
  }
  return true
}

const handlePdfUploadSuccess = (response: any) => {
  console.log('PDF上传成功响应:', response)
  if (response.code === '200') {
    paperForm.pdfurl = response.data
    ElMessage.success('PDF上传成功')
  } else {
    ElMessage.error(response.message || 'PDF上传失败')
  }
}

const handlePdfUploadError = (error: any) => {
  console.error('PDF上传失败:', error)
  ElMessage.error('PDF上传失败')
}

// 读取本地 .bib 文件并填入 BibTeX 编辑框
const handleBibFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return

  if (!file.name.toLowerCase().endsWith('.bib')) {
    ElMessage.error('请选择 .bib 格式文件')
    return
  }
  if (file.size > 1024 * 1024) {
    ElMessage.error('.bib 文件大小不能超过 1MB')
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    const content = String(reader.result || '').replace(/^\uFEFF/, '')
    if (!content.trim()) {
      ElMessage.warning('.bib 文件内容为空')
      return
    }
    paperForm.bib = content
    ElMessage.success('BibTeX 文件读取成功')
  }
  reader.onerror = () => ElMessage.error('BibTeX 文件读取失败，请重试')
  reader.readAsText(file, 'UTF-8')
}

const handleCopyBib = async (bib?: string) => {
  if (!bib?.trim()) {
    ElMessage.warning('该论文暂无 BibTeX 内容')
    return
  }
  const copied = await copyPlainText(bib)
  copied ? ElMessage.success('BibTeX 已复制') : ElMessage.error('复制失败，请手动复制')
}

// 预览论文PDF
const handlePreviewPaper = (paper: Paper) => {
  if (paper.pdfurl) {
    currentPdfUrl.value = paper.pdfurl
    pdfPreviewVisible.value = true
  } else {
    ElMessage.warning('该论文暂无PDF文件')
  }
}

// 关闭PDF预览
const handlePdfClose = () => {
  pdfPreviewVisible.value = false
  currentPdfUrl.value = ''
}

// 下载PDF
const downloadPdf = () => {
  if (currentPdfUrl.value) {
    const link = document.createElement('a')
    link.href = currentPdfUrl.value
    link.download = '论文.pdf'
    link.click()
  }
}

// 点击论文标题跳转
const handleTitleClick = (paper: Paper) => {
  if (paper.url) {
    window.open(paper.url, '_blank')
  } else {
    ElMessage.warning('该论文暂无在线链接')
  }
}

// 生命周期
onMounted(() => {
  fetchPapers()
})
</script>

<style scoped>
.publications-page {
  padding: 20px;
}

.main-card {
  min-height: calc(100vh - 100px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.tab-content {
  min-height: 500px;
}

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 4px;
}

.year-filter {
  margin-top: 15px;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

:deep(.el-table .cell) {
  line-height: 1.5;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

.title-link {
  color: #409eff;
  text-decoration: underline;
  cursor: pointer;
}

.title-link:hover {
  color: #66b1ff;
}

.pdf-preview {
  margin-top: 10px;
}

.pdf-preview .el-link {
  font-size: 14px;
}

.bib-table-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.bib-editor {
  width: 100%;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #fafcff;
}

.bib-toolbar,
.bib-editor-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.bib-toolbar {
  justify-content: flex-start;
  margin-bottom: 10px;
}

.bib-upload-tip,
.bib-editor-footer {
  color: #909399;
  font-size: 12px;
}

.bib-editor-footer {
  margin-top: 6px;
}

.hidden-bib-input {
  display: none;
}

:deep(.paper-dialog .el-dialog__body) {
  max-height: 70vh;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .bib-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
