<template>
  <div class="research-page">
    <!-- 页面标题 -->
    <!--    <div class="page-header">-->
    <!--     -->
    <!--    </div>-->

    <div class="layout-container">
      <!-- 侧边导航栏 -->
      <div class="sidebar">
        <!-- 搜索框 -->
        <div class="search-section">
          <el-input
            v-model="searchText"
            placeholder="搜索研究方向"
            clearable
            class="search-input"
            :prefix-icon="Search"
            size="large"
            @input="handleSearch"
          />
        </div>

        <!-- 研究方向导航菜单 -->
        <div class="research-menu">
          <el-menu
            :default-active="activeResearch"
            class="research-menu-list"
            @select="handleMenuSelect"
          >
            <template v-for="category in filteredCategories" :key="category.id">
              <el-menu-item :index="category.id.toString()" class="parent-menu-item">
                <div class="menu-item-content">
                  <el-icon><component :is="category.children?.length ? FolderOpened : Document" /></el-icon>
                  <span class="menu-title">{{ category.name }}</span>
                </div>
              </el-menu-item>
              <el-menu-item
                v-for="child in category.children || []"
                :key="child.id"
                :index="child.id.toString()"
                class="sub-menu-item"
              >
                <div class="menu-item-content">
                  <el-icon><Folder /></el-icon>
                  <span class="menu-title">{{ child.name }}</span>
                </div>
              </el-menu-item>
            </template>
          </el-menu>

          <!-- 空状态 -->
          <div v-if="filteredCategories.length === 0" class="empty-menu">
            <el-empty description="暂无相关研究方向" :image-size="80" />
          </div>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="main-content">
        <!-- 研究方向详情 -->
        <div v-if="currentResearch" class="research-detail">
          <!-- 研究方向头部 -->
          <div class="research-header">
            <div class="research-title-section">
              <h2 class="research-title">{{ currentResearch.name }}</h2>
              <div class="research-meta">
                <span class="research-category">{{ getParentCategory(currentResearch) }}</span>
                <el-tag
                  v-if="currentResearch.level === 2"
                  type="success"
                  size="small"
                >
                  子方向
                </el-tag>
              </div>
            </div>

          </div>

          <!-- 研究方向图片 -->
          <div class="research-image-section">
            <el-image
              :src="currentResearch.pictureurl"
              :alt="currentResearch.name"
              class="research-image"
              fit="cover"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>图片加载失败</span>
                </div>
              </template>
              <template #placeholder>
                <div class="image-loading">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span>图片加载中...</span>
                </div>
              </template>
            </el-image>
          </div>

          <!-- 研究方向简介 -->
          <div class="research-intro">
            <h3 class="section-title">研究方向简介</h3>
            <div class="intro-content">
              <p>{{ currentResearch.introduction || '暂无简介' }}</p>
            </div>
          </div>

          <!-- 相关科研人员 -->
          <div class="researchers-section">
            <h3 class="section-title">相关科研人员</h3>
            <div v-if="currentResearch.researchers && currentResearch.researchers.length > 0" class="researchers-grid">
              <el-card
                v-for="researcher in currentResearch.researchers"
                :key="researcher.id"
                class="researcher-card"
                shadow="hover"
              >
                <div class="researcher-info">
                  <el-avatar
                    :size="60"
                    :src="researcher.pictureurl"
                    class="researcher-avatar"
                  >
                    {{ researcher.name.charAt(0) }}
                  </el-avatar>
                  <div class="researcher-details">
                    <h4 class="researcher-name">{{ researcher.name }}</h4>
                    <p class="researcher-title">{{ researcher.identity }}</p>
                    <p class="researcher-email">{{ researcher.email }}</p>
                  </div>
                </div>
                <div class="researcher-actions">
                  <el-button type="primary" text @click="viewResearcherDetail(researcher)">
                    查看详情
                  </el-button>
                </div>
              </el-card>
            </div>
            <div v-else class="no-researchers">
              <el-empty description="暂无相关科研人员" :image-size="60" />
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-research">
          <el-empty description="请选择研究方向查看详情" :image-size="120">
            <p class="empty-tip">从左侧导航栏选择一个研究方向来查看详细信息</p>
          </el-empty>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import {
  Search, User, Share, Picture, Loading, Star, Trophy,
  FolderOpened, Folder, Document
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
// 路由实例
const router = useRouter()




// 研究方向类型定义
interface ResearchCategory {
  id: number
  name: string
  introduction: string
  pictureurl: string
  level: number
  parentid?: number
  children?: ResearchCategory[]
  researchers?: Researcher[]
}

interface Researcher {
  id: number
  name: string
  identity: string
  email: string
  pictureurl: string
  introduction: string
}

// 响应式数据
const searchText = ref('')
const activeResearch = ref('')
const researchCategories = ref<ResearchCategory[]>([])
const allMembers = ref<Researcher[]>([])
const loading = ref(false)

// 计算属性：过滤研究方向
const filteredCategories = computed(() => {
  if (!searchText.value) {
    return researchCategories.value
  }

  const searchLower = searchText.value.toLowerCase()
  return researchCategories.value.filter(category => {
    // 检查父级分类
    if (category.name.toLowerCase().includes(searchLower) ||
      category.introduction?.toLowerCase().includes(searchLower)) {
      return true
    }

    // 检查子级分类
    if (category.children) {
      const filteredChildren = category.children.filter(child =>
        child.name.toLowerCase().includes(searchLower) ||
        child.introduction?.toLowerCase().includes(searchLower)
      )
      if (filteredChildren.length > 0) {
        return true
      }
    }

    return false
  })
})

// 当前选中的研究方向
const currentResearch = computed(() => {
  if (!activeResearch.value) return null

  const researchId = parseInt(activeResearch.value)

  // 在所有分类中查找（包括子分类）
  for (const category of researchCategories.value) {
    if (category.id === researchId) {
      return category
    }
    if (category.children) {
      const child = category.children.find(c => c.id === researchId)
      if (child) return child
    }
  }
  return null
})

// 方法
const handleMenuSelect = (index: string) => {
  activeResearch.value = index
}

const handleSearch = () => {
  // 搜索逻辑已经在计算属性中处理
}

const getParentCategory = (research: ResearchCategory) => {
  if (research.level === 1) return '主要研究方向'

  for (const category of researchCategories.value) {
    if (category.children?.some(child => child.id === research.id)) {
      return category.name
    }
  }
  return '未知分类'
}

const viewResearcherDetail = (researcher: Researcher) => {

  router.push(`/researcher/${researcher.id}`)
}

// 获取研究方向数据
const fetchResearchData = async () => {
  try {
    loading.value = true

    // 获取所有成员数据
    const membersResponse = await request.get('/member/list')
    allMembers.value = membersResponse.data || []

    // 获取父级研究方向
    const parentResponse = await request.get('/direction/parent')
    const parentDirections = parentResponse.data || []

    // 为每个父级研究方向获取子方向
    const categoriesWithChildren = await Promise.all(
      parentDirections.map(async (parent: ResearchCategory) => {
        try {
          const childResponse = await request.get(`/direction/child/${parent.id}`)
          const childDirections = childResponse.data || []

          return {
            ...parent,
            children: childDirections
          }
        } catch (error) {
          console.error(`获取父级研究方向 ${parent.name} 的子方向失败:`, error)
          return {
            ...parent,
            children: []
          }
        }
      })
    )

    researchCategories.value = categoriesWithChildren

    // 默认选中第一个研究方向
    if (researchCategories.value.length > 0) {
      activeResearch.value = String(researchCategories.value[0]?.id || '')
    }
  } catch (error) {
    console.error('获取研究方向数据失败:', error)
    ElMessage.error('获取研究方向数据失败，请检查后端服务')
  } finally {
    loading.value = false
  }
}

// 获取研究方向相关的科研人员
const fetchResearchersByDirection = async (directionId: number) => {
  try {
    const response = await request.get(`/member/direction/${directionId}`)
    return response.data || []
  } catch (error) {
    console.error(`获取研究方向 ${directionId} 的科研人员失败:`, error)
    return []
  }
}

// 监听当前研究方向的变化，动态加载科研人员数据
watch(currentResearch, async (newResearch) => {
  if (newResearch) {
    try {
      const researchers = await fetchResearchersByDirection(newResearch.id)
      // 更新当前研究方向的科研人员数据
      if (newResearch.level === 1) {
        // 父级研究方向：查找对应的研究方向对象并更新
        const category = researchCategories.value.find(cat => cat.id === newResearch.id)
        if (category) {
          category.researchers = researchers
        }
      } else {
        // 子级研究方向：查找对应的子方向对象并更新
        for (const category of researchCategories.value) {
          if (category.children) {
            const child = category.children.find(c => c.id === newResearch.id)
            if (child) {
              child.researchers = researchers
              break
            }
          }
        }
      }
    } catch (error) {
      console.error('加载科研人员数据失败:', error)
    }
  }
})

onMounted(() => {
  fetchResearchData()
})
</script>

<style scoped>
.research-page {
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
  margin: 0;
}

.layout-container {
  display: flex;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 侧边栏样式 */
.sidebar {
  width: 320px;
  flex-shrink: 0;
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  height: fit-content;
  position: sticky;
  top: 20px;
}

.search-section {
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.research-menu {
  max-height: none;
  overflow: visible;
}

.research-menu-list {
  border: none;
}

.research-menu-list :deep(.el-sub-menu__title),
.research-menu-list :deep(.el-menu-item) {
  height: auto;
  padding: 12px 16px;
  margin: 4px 0;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.research-menu-list :deep(.el-sub-menu__title:hover),
.research-menu-list :deep(.el-menu-item:hover) {
  background: #f8fafc;
}

.research-menu-list :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.menu-item-content {
  display: flex;
  align-items: center;
  width: 100%;
}

.menu-item-content .el-icon {
  margin-right: 12px;
  font-size: 18px;
}

.menu-title {
  flex: 1;
  font-weight: 500;
}

.menu-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  min-width: 24px;
  text-align: center;
}

.sub-menu-item {
  padding-left: 40px !important;
  background: #f8faff;
  font-size: 13px;
}

.parent-menu-item {
  margin-top: 5px;
  font-weight: 700;
  color: #233c65;
}

.empty-menu {
  text-align: center;
  padding: 40px 0;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  min-width: 0;
}

.research-detail {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 研究方向头部 */
.research-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e2e8f0;
}

.research-title-section {
  flex: 1;
}

.research-title {
  font-size: 2rem;
  color: #2c3e50;
  margin-bottom: 8px;
  font-weight: 600;
}

.research-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.research-category {
  color: #7f8c8d;
  font-size: 0.9rem;
}

.research-actions {
  display: flex;
  gap: 12px;
}

/* 研究方向图片 */
.research-image-section {
  margin-bottom: 24px;
}

.research-image {
  width: 100%;
  height: 300px;
  border-radius: 8px;
  overflow: hidden;
}

.image-error,
.image-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: #f8fafc;
  color: #7f8c8d;
}

.image-error .el-icon,
.image-loading .el-icon {
  font-size: 3rem;
  margin-bottom: 8px;
}

/* 内容区块 */
.section-title {
  font-size: 1.3rem;
  color: #2c3e50;
  margin-bottom: 16px;
  font-weight: 600;
  padding-bottom: 8px;
  border-bottom: 2px solid #667eea;
}

.intro-content {
  line-height: 1.6;
  color: #5a6c7d;
  font-size: 1rem;
}

/* 特色网格 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.feature-icon {
  color: #667eea;
  margin-right: 12px;
  font-size: 1.2rem;
}

.feature-text {
  color: #5a6c7d;
}

/* 成果列表 */
.achievements-list {
  margin-top: 16px;
}

.achievement-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
}

.achievement-item:last-child {
  border-bottom: none;
}

.achievement-icon {
  color: #f59e0b;
  margin-right: 12px;
  font-size: 1.1rem;
}

.achievement-text {
  color: #5a6c7d;
}

/* 科研人员网格 */
.researchers-section {
  margin-top: 32px;
}

.researchers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.researcher-card {
  transition: all 0.3s ease;
}

.researcher-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.researcher-info {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.researcher-avatar {
  margin-right: 16px;
}

.researcher-details {
  flex: 1;
}

.researcher-name {
  font-size: 1.1rem;
  color: #2c3e50;
  margin-bottom: 4px;
  font-weight: 600;
}

.researcher-title {
  color: #7f8c8d;
  font-size: 0.9rem;
  margin-bottom: 4px;
}

.researcher-email {
  color: #667eea;
  font-size: 0.85rem;
}

.researcher-actions {
  text-align: center;
}

.no-researchers {
  text-align: center;
  padding: 40px 0;
}

/* 空状态 */
.empty-research {
  background: white;
  border-radius: 12px;
  padding: 60px 32px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.empty-tip {
  color: #7f8c8d;
  margin-top: 12px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .layout-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    position: static;
  }

  .research-header {
    flex-direction: column;
    gap: 16px;
  }

  .research-actions {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .research-page {
    padding: 10px;
  }

  .research-detail {
    padding: 20px;
  }

  .research-title {
    font-size: 1.5rem;
  }

  .research-image {
    height: 200px;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .researchers-grid {
    grid-template-columns: 1fr;
  }
}

/* 动画效果 */
.research-detail {
  animation: fadeInUp 0.6s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
