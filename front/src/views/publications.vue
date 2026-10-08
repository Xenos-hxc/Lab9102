<template>
  <div class="publications-page">


    <div class="layout-container">
      <!-- 侧边导航栏 -->
      <div class="sidebar">
        <!-- 导航菜单 -->
        <div class="nav-menu">
          <el-menu
            :default-active="activeTab"
            class="nav-menu-list"
            @select="handleTabSelect"
          >
            <el-menu-item index="papers">
              <div class="menu-item-content">
                <el-icon><Document /></el-icon>
                <span class="menu-title">科研论文</span>
              </div>
            </el-menu-item>
            <el-menu-item index="projects">
              <div class="menu-item-content">
                <el-icon><FolderOpened /></el-icon>
                <span class="menu-title">科研项目</span>
              </div>
            </el-menu-item>
          </el-menu>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="main-content">
        <!-- 科研论文页面 -->
        <div v-if="activeTab === 'papers'" class="papers-section">
          <!-- 筛选区域 -->
          <div class="filter-section">
            <div class="filter-row">
              <div class="filter-item author-search">
                <span class="filter-label">作者搜索：</span>
                <el-input
                  v-model="authorKeyword"
                  clearable
                  :prefix-icon="Search"
                  placeholder="输入中文名或英文作者名"
                />
              </div>
              <div class="filter-item">
                <span class="filter-label">发表年份：</span>
                <el-date-picker
                  v-model="yearRange"
                  type="yearrange"
                  range-separator="至"
                  start-placeholder="开始年份"
                  end-placeholder="结束年份"
                  value-format="YYYY"
                  @change="handleYearChange"
                  clearable
                />
              </div>
              <div class="filter-item">
                <span class="filter-label">论文级别：</span>
                <el-select
                  style="width: 120px"
                  v-model="levelFilter"
                  placeholder="全部级别"
                  @change="handleLevelChange"
                  clearable
                >
                  <el-option
                    v-for="level in paperLevels"
                    :key="level"
                    :label="level"
                    :value="level"
                  />
                </el-select>
              </div>
              <div class="filter-item">
                <span class="filter-label">排序方式：</span>
                <el-select
                  style="width: 120px"
                  v-model="sortOrder"
                  placeholder="请选择排序方式"
                  @change="handleSortChange"
                >
                  <el-option label="年份降序" value="desc" />
                  <el-option label="年份升序" value="asc" />
                </el-select>
              </div>
            </div>
          </div>

          <!-- 论文列表 -->
          <div class="papers-list">
            <div
              v-for="paper in sortedPapers"
              :key="paper.id"
              class="paper-card"
            >
              <el-card shadow="hover" class="paper-item">
                <div class="paper-header">
                  <h3 class="paper-title" @click="handleTitleClick(paper)">
                    <a href="javascript:void(0)" class="title-link">{{ paper.title }}</a>
                  </h3>
                  <div class="paper-actions">
                    <el-button
                      size="small"
                      type="success"
                      plain
                      :disabled="!paper.bib"
                      @click="handleCopyBib(paper.bib)"
                    >
                      <el-icon><CopyDocument /></el-icon>
                      复制 Bib
                    </el-button>
                    <el-button
                      v-if="paper.pdfurl"
                      size="small"
                      type="primary"
                      @click="handlePreviewPaper(paper)"
                    >
                      <el-icon><View /></el-icon>
                      预览论文
                    </el-button>
                    <el-button
                      v-else
                      size="small"
                      type="info"
                      disabled
                    >
                      <el-icon><Document /></el-icon>
                      暂无论文
                    </el-button>
                  </div>
                </div>

                <div class="paper-content">
                  <div class="paper-info">
                    <div class="info-item">
                      <el-icon><User /></el-icon>
                      <span class="info-label">作者：</span>
                      <span class="info-value" style="margin-left: -30px">{{ formatAuthors(paper.authors) }}</span>
                    </div>

                    <div class="info-item">
                      <el-icon><Calendar /></el-icon>
                      <span class="info-label">发表年份：</span>
                      <span class="info-value">{{ paper.time }}</span>
                    </div>

                    <div class="info-item">
                      <el-icon><Reading /></el-icon>
                      <span class="info-label">期刊/会议：</span>
                      <span class="info-value">{{ paper.place }}</span>
                    </div>

                    <div class="info-item">
                      <el-icon><Star /></el-icon>
                      <span class="info-label">论文级别：</span>
                      <span class="info-value">
                        <template v-if="paper.level">
                          <el-tag
                            v-for="(tag, index) in paper.level.split(/,|，/).filter(t => t.trim())"
                            :key="index"
                            type="danger"
                            size="small"
                            style="margin-right: 5px"
                          >
                            {{ tag.trim() }}
                          </el-tag>
                        </template>
                        <el-tag v-else type="info" size="small">
                          未设置
                        </el-tag>
                      </span>
                    </div>
                  </div>
                </div>
              </el-card>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="sortedPapers.length === 0 && !loading" class="empty-state">
            <el-empty description="暂无相关科研论文" :image-size="100" />
          </div>

          <!-- 加载状态 -->
          <div v-if="loading" class="loading-state">
            <el-skeleton :rows="3" animated />
          </div>
        </div>

        <!-- 科研项目页面 -->
        <div v-if="activeTab === 'projects'" class="projects-section">
          <!-- 项目列表 -->
          <div class="projects-list">
            <div
              v-for="project in filteredProjects"
              :key="project.id"
              class="project-card"
            >
              <el-card shadow="hover" class="project-item">
                <div class="project-header">
                  <h3 class="project-title">{{ project.title }}</h3>
                  <div class="project-tags">
                    <el-tag :type="getProjectTypeTag(project.type)" size="small">
                      {{ project.type }}
                    </el-tag>
                    <el-tag v-if="!project.endtime" type="warning" size="small">
                      进行中
                    </el-tag>
                    <el-tag v-else type="success" size="small">
                      已结题
                    </el-tag>
                  </div>
                </div>

                <div class="project-content">
                  <div class="project-info">
                    <div class="info-item">
                      <el-icon><Calendar /></el-icon>
                      <span class="info-label">开始时间：</span>
                      <span class="info-value">{{ formatDate(project.starttime) }}</span>
                    </div>

                    <div class="info-item">
                      <el-icon><Calendar /></el-icon>
                      <span class="info-label">结束时间：</span>
                      <span class="info-value">{{ project.endtime ? formatDate(project.endtime) : '进行中' }}</span>
                    </div>

                    <div class="info-item">
                      <el-icon><User /></el-icon>
                      <span class="info-label">合作方：</span>
                      <span class="info-value">{{ project.company || '未指定' }}</span>
                    </div>
                  </div>
                </div>
              </el-card>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="filteredProjects.length === 0 && !loading" class="empty-state">
            <el-empty description="暂无相关科研项目" :image-size="100" />
          </div>

          <!-- 加载状态 -->
          <div v-if="loading" class="loading-state">
            <el-skeleton :rows="3" animated />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, computed, onMounted, onUnmounted, watch} from 'vue'
import {
  Document, FolderOpened, User, Calendar, Reading, View, Star, CopyDocument, Search
} from '@element-plus/icons-vue'
import Request from "@/utils/request.ts";
import { ElMessage } from 'element-plus'
import { copyPlainText } from '@/utils/clipboard'

// 论文类型定义 - 根据后端实体类字段
interface Paper {
  id: number
  title: string
  authors: string  // 后端存储为字符串，前端需要解析
  time: number     // 发表年份
  place: string    // 期刊/会议
  url: string      // 论文链接
  pdfurl: string   // PDF文件链接
  level: string    // 论文级别
  bib?: string     // BibTeX 引用内容
}

// 项目类型定义 - 根据后端实体类字段
interface Project {
  id: number
  title: string
  starttime: string  // 开始时间
  endtime: string    // 结束时间，为空表示进行中
  type: string      // 项目类型：横向项目、纵向项目
  company: string   // 合作方
}

interface Member {
  id: number
  name: string
  username: string
  email?: string
}

// 响应式数据
const activeTab = ref('papers')
const yearRange = ref<[string, string] | null>(null)
const levelFilter = ref('')
const authorKeyword = ref('')
const sortOrder = ref('desc')
const loading = ref(false)
const paperLevels = ref<string[]>([])

// 论文数据
const papers = ref<Paper[]>([])
const members = ref<Member[]>([])
// 项目数据
const projects = ref<Project[]>([])

// 计算属性：过滤论文
const filteredPapers = computed(() => {
  let filtered = papers.value

  // 年份筛选
  if (yearRange.value && yearRange.value.length === 2) {
    const [startYear, endYear] = yearRange.value
    const start = parseInt(startYear)
    const end = parseInt(endYear)

    filtered = filtered.filter(paper =>
      paper.time >= start && paper.time <= end
    )
  }

  // 级别筛选
  if (levelFilter.value) {
    filtered = filtered.filter(paper => {
      if (!paper.level) return false
      const levels = paper.level.split(/,|，/).map(l => l.trim())
      // 标准化后进行匹配，支持 CCF A 和 CCF-A 互通
      const normalizedLevels = levels.map(l => normalizeLevel(l))
      return normalizedLevels.includes(levelFilter.value)
    })
  }

  const rawAuthorKeyword = authorKeyword.value.trim()
  if (rawAuthorKeyword) {
    const normalizedKeyword = normalizeAuthorText(rawAuthorKeyword)
    const aliases = new Set<string>([normalizedKeyword])
    members.value
      .filter(member => member.name?.includes(rawAuthorKeyword))
      .forEach(member => {
        const emailName = normalizeAuthorText(member.email?.split('@')[0] || '')
        const username = normalizeAuthorText(member.username || '')
        if (emailName) aliases.add(emailName)
        if (username.length >= 3) aliases.add(username)
      })
    filtered = filtered.filter(paper => {
      const authors = normalizeAuthorText(paper.authors)
      return [...aliases].some(alias => authors.includes(alias) || (alias.length >= 4 && isSubsequence(alias, authors)))
    })
  }

  return filtered
})

// 计算属性：排序论文
const sortedPapers = computed(() => {
  const filtered = [...filteredPapers.value]
  return filtered.sort((a, b) => {
    if (sortOrder.value === 'desc') {
      return b.time - a.time
    } else {
      return a.time - b.time
    }
  })
})

// 计算属性：过滤项目
const filteredProjects = computed(() => {
  return projects.value
})

// 方法
const handleTabSelect = (index: string) => {
  activeTab.value = index
}

const normalizeAuthorText = (value: string) => (value || '')
  .toLowerCase()
  .normalize('NFKD')
  .replace(/[\s.,，·\-_'"()（）]/g, '')

const isSubsequence = (needle: string, haystack: string) => {
  let index = 0
  for (const char of haystack) {
    if (char === needle[index]) index += 1
    if (index === needle.length) return true
  }
  return false
}

const handleYearChange = () => {
  // 年份范围变化处理
  console.log('年份范围变化:', yearRange.value)
}

const handleLevelChange = () => {
  // 级别筛选变化处理
  console.log('级别筛选变化:', levelFilter.value)
}

const handleSortChange = () => {
  // 排序方式变化处理
  console.log('排序方式变化:', sortOrder.value)
}

const normalizeLevel = (level: string) => {
  // 统一转换为 CCF-X 格式，处理 CCF A, CCF-A, ccf a 等情况
  return level.replace(/^CCF\s*[-]?\s*([ABC])$/i, 'CCF-$1')
}

const getProjectTypeTag = (type: string) => {
  return type === '纵向项目' ? 'success' : 'primary'
}

const getLevelTagType = (level: string) => {
  const typeMap: Record<string, string> = {
    'SCI': 'danger',
    'EI': 'warning',
    '核心': 'success',
    '普通': 'info'
  }
  return typeMap[level] || 'info'
}

// 点击论文标题
const handleTitleClick = (paper: Paper) => {
  if (paper.url) {
    window.open(paper.url, '_blank')
  } else {
    ElMessage.warning('该论文暂无在线链接')
  }
}

// 预览论文PDF
const handlePreviewPaper = (paper: Paper) => {
  if (paper.pdfurl) {
    window.open(paper.pdfurl, '_blank')
  } else {
    ElMessage.warning('该论文暂无PDF文件')
  }
}

const handleCopyBib = async (bib?: string) => {
  if (!bib?.trim()) {
    ElMessage.warning('该论文暂无 BibTeX 内容')
    return
  }
  const copied = await copyPlainText(bib)
  copied ? ElMessage.success('BibTeX 已复制') : ElMessage.error('复制失败，请手动复制')
}

// 格式化作者信息（后端存储为字符串，前端解析显示）
const formatAuthors = (authors: string) => {
  if (!authors) return '未知作者'
  try {
    // 假设作者信息以逗号分隔
    return authors.split(',').join('，')
  } catch (error) {
    return authors
  }
}

// 格式化日期显示
const formatDate = (dateString: string) => {
  if (!dateString) return '未知时间'
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN')
  } catch (error) {
    return dateString
  }
}

// API调用方法
const fetchPapers = async () => {
  try {
    loading.value = true
    const result = await Request('/paper/list')

    if (result.code === 200) {
      papers.value = result.data || []

      // 提取所有论文级别并去重
      const levels = new Set<string>()
      papers.value.forEach(paper => {
        if (paper.level) {
          // 支持中英文逗号分隔
          const splitLevels = paper.level.split(/,|，/).map(l => l.trim()).filter(l => l)
          splitLevels.forEach(l => levels.add(normalizeLevel(l)))
        }
      })
      paperLevels.value = Array.from(levels).sort()

      console.log('获取论文数据:', papers.value)
    } else {
      ElMessage.error('获取论文数据失败: ' + result.message)
      papers.value = []
    }
  } catch (error) {
    console.error('获取论文数据失败:', error)
    ElMessage.error('网络错误，请检查后端服务是否启动')
    papers.value = []
  } finally {
    loading.value = false
  }
}

const fetchMembers = async () => {
  try {
    const result = await Request('/member/list')
    if (result.code == 200) members.value = result.data || []
  } catch (error) {
    console.error('获取作者中文名映射失败:', error)
  }
}

const fetchProjects = async () => {
  try {
    loading.value = true
    const result = await Request('/project/list')

    if (result.code === 200) {
      projects.value = result.data || []
      console.log(projects.value)
    } else {
      ElMessage.error('获取项目数据失败: ' + result.message)
      projects.value = []
    }
  } catch (error) {
    console.error('获取项目数据失败:', error)
    ElMessage.error('网络错误，请检查后端服务是否启动')
    projects.value = []
  } finally {
    loading.value = false
  }
}

// 监听标签切换，动态加载数据
const loadDataByTab = () => {
  if (activeTab.value === 'papers') {
    fetchPapers()
  } else if (activeTab.value === 'projects') {
    fetchProjects()
  }
}

onMounted(() => {
  // 页面加载时默认加载论文数据
  fetchPapers()
  fetchMembers()

  // 监听标签切换
  const unwatch = watch(activeTab, () => {
    loadDataByTab()
  })

  // 组件卸载时取消监听
  onUnmounted(() => {
    unwatch()
  })
})
</script>

<style scoped>
.publications-page {
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 2.5rem;
  color: #2c3e50;
  margin-bottom: 10px;
  font-weight: 600;
}

.page-subtitle {
  font-size: 1.1rem;
  color: #7f8c8d;
  margin-bottom: 20px;
}

.layout-container {
  display: flex;
  gap: 24px;
  width: min(96%, 1680px);
  margin: 0 auto;
}

/* 侧边栏样式 */
.sidebar {
  width: 240px;
  flex-shrink: 0;
}

.nav-menu {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 10px 0;
}

.nav-menu-list {
  border: none;
}

.menu-item-content {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.menu-title {
  flex: 1;
  font-weight: 500;
}

/* 主内容区域样式 */
.main-content {
  flex: 1;
  min-width: 0;
}

/* 筛选区域样式 */
.filter-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.filter-row {
  display: grid;
  grid-template-columns: minmax(260px, 1.35fr) minmax(290px, 1.25fr) minmax(200px, .8fr) minmax(210px, .8fr);
  gap: 16px 20px;
  align-items: center;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.author-search :deep(.el-input) {
  width: 100%;
}

.filter-label {
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}

.filter-item :deep(.el-date-editor) {
  width: 100%;
  min-width: 0;
}

@media (max-width: 1380px) {
  .filter-row {
    grid-template-columns: repeat(2, minmax(280px, 1fr));
  }
}

/* 论文卡片样式 */
.papers-list,
.projects-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.paper-card,
.project-card {
  animation: fadeInUp 0.5s ease-out;
}

.paper-item,
.project-item {
  border: none;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.paper-item:hover,
.project-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.paper-header,
.project-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.paper-title,
.project-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  line-height: 1.4;
  flex: 1;
  margin-right: 16px;
}

.paper-actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 8px;
}

.paper-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.project-tags {
  display: flex;
  gap: 8px;
}

/* 信息项样式 */
.paper-content,
.project-content {
  padding: 0;
}

.paper-info,
.project-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.info-label {
  color: #909399;
  font-weight: 500;
  min-width: 70px;
}

.info-value {
  color: #606266;
  flex: 1;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

/* 加载状态样式 */
.loading-state {
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .layout-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    margin-bottom: 20px;
  }

  .filter-row {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .filter-item {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .paper-header,
  .project-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .paper-title,
  .project-title {
    margin-right: 0;
  }

  .paper-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .paper-info,
  .project-info {
    grid-template-columns: 1fr;
  }
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
