<template>
  <div class="home-page">
    <!-- 走马灯 -->
    <div class="carousel-section">
      <el-carousel :interval="4000" type="card" height="430px">
        <el-carousel-item v-for="item in carouselItems" :key="item.id">
          <div class="carousel-content">
            <h3>{{ item.title }}</h3>
            <div class="carousel-image-container">
              <img
                v-if="item.pictureurl"
                :src="item.pictureurl"
                :alt="item.title"
                class="carousel-image"
              />
              <div v-else class="carousel-image-placeholder">
                <el-icon size="60"><Picture /></el-icon>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <el-row :gutter="30">
        <el-col :span="24">
          <div class="welcome-section">
            <div class="welcome-header">
              <h2>欢迎来到{{ labInfo.labName || '智能控制与先进系统实验室' }}</h2>
              <p>{{ labInfo.introduction || '我们致力于智能控制理论、先进系统设计、人工智能应用等前沿领域的研究，培养高素质科研人才，推动科技创新发展。' }}</p>
            </div>

            <el-divider></el-divider>

            <div class="home-split">
            <!-- 研究方向 - 横向滚动卡片样式 -->
            <div class="research-directions-section">
              <div class="research-header">
                <h2 class="research-title">研究方向</h2>
                <p class="research-subtitle">RESEARCH AREAS</p>
              </div>

              <div v-if="loading" class="loading-container">
                <el-skeleton :rows="5" animated />
              </div>

              <div v-else class="research-scroll-container">
                <div
                  v-for="(direction, index) in researchDirections"
                  :key="direction.id"
                  class="research-item"
                  :class="{ 'expanded': selectedIndex === index, [`research-item-${index}`]: true }"
                  @click="selectArea(index)"
                >
                  <!-- 折叠状态的标题卡片 -->
                  <div class="research-tab">
                    <h3 class="tab-title">{{ direction.name }}</h3>
                  </div>

                  <!-- 展开状态的详细内容 -->
                  <div class="research-content">
                    <div class="content-inner">
                      <div class="content-header">
                        <h3 class="content-title">{{ direction.name }}</h3>
                      </div>
                      <div class="content-body">
                        <p class="content-description">{{ direction.introduction || '暂无简介' }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 空状态 -->
              <div v-if="!loading && researchDirections.length === 0" class="empty-state">
                <el-empty description="暂无研究方向数据" :image-size="100" />
              </div>
            </div>

            <!-- 近期新闻 -->
            <div class="recent-news-section">
              <div class="news-header">
                <h3>近期新闻</h3>
                <p class="news-subtitle">LATEST NEWS</p>
              </div>
              <el-card shadow="never" class="news-list-card">
                <div v-if="newsLoading" class="loading-container">
                  <el-skeleton :rows="3" animated />
                </div>
                <div v-else-if="recentNews.length === 0" class="empty-state">
                  <el-empty description="暂无新闻数据" :image-size="80" />
                </div>
                <el-timeline v-else>
                  <el-timeline-item
                    v-for="(news, index) in recentNews.slice(0, 4)"
                    :key="news.id"
                    :timestamp="formatDate(news.time)"
                    :type="getNewsType(index)"
                    :icon="getNewsIcon(index)"
                  >
                    <div class="news-item" @click="viewNewsDetail(news.id)">
                      <h4 class="news-title-link">{{ news.title }}</h4>
                      <p>{{ news.summary || news.content || '暂无摘要' }}</p>
                    </div>
                  </el-timeline-item>
                </el-timeline>
              </el-card>
            </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

import { Picture, Promotion, Trophy, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'


// 实验室信息接口定义
interface LabInfo {
  id: number
  labName: string
  introduction: string
  university: string
  college: string
  phone: string
  address: string
  email: string
}

// 研究方向接口定义
interface ResearchDirection {
  id: number
  name: string
  introduction: string
  pictureurl: string
  level: number
  parentid?: number
}

// 新闻接口定义
interface News {
  id: number
  title: string
  summary: string
  content: string
  time: string
  type: string
  pictureurl: string
}

interface CarouselItem {
  id: number
  title: string
  pictureurl: string
}

// 获取路由实例
const router = useRouter()

const selectedIndex = ref<number | null>(null)
const carouselItems = ref<CarouselItem[]>([])
const researchDirections = ref<ResearchDirection[]>([])
const recentNews = ref<News[]>([])
const labInfo = ref<LabInfo>({
  id: 0,
  labName: '',
  introduction: '',
  university: '',
  college: '',
  phone: '',
  address: '',
  email: ''
})
const loading = ref(false)
const newsLoading = ref(false)
const labInfoLoading = ref(false)

const selectArea = (index: number) => {
  selectedIndex.value = selectedIndex.value === index ? null : index
}

// 查看新闻详情
const viewNewsDetail = (newsId: number) => {
  router.push(`/news-detail/${newsId}`)
}

// 获取实验室信息
const fetchLabInfo = async () => {
  try {
    labInfoLoading.value = true
    const response = await request.get('/labinfo/detail')

    if (response.code === 200) {
      const data = response.data
      if (data) {
        labInfo.value = {
          id: data.id || 0,
          labName: data.labName || '智能控制与先进系统实验室',
          introduction: data.introduction || '我们致力于智能控制理论、先进系统设计、人工智能应用等前沿领域的研究，培养高素质科研人才，推动科技创新发展。',
          university: data.university || '',
          college: data.college || '',
          phone: data.phone || '',
          address: data.address || '',
          email: data.contactPerson1Email || ''
        }
      }
    } else {
      console.error('获取实验室信息失败:', response.msg)
      ElMessage.warning('获取实验室信息失败，使用默认信息')
    }
  } catch (error) {
    console.error('获取实验室信息出错:', error)
    ElMessage.warning('获取实验室信息出错，使用默认信息')
  } finally {
    labInfoLoading.value = false
  }
}

// 获取走马灯数据
const fetchCarouselData = async () => {
  try {
    const response = await request.get('/carousel/list')
    console.log('response', response.code)
    if (response.code == 200) {
      carouselItems.value = response.data || []
    } else {
      console.error('获取走马灯数据失败:', response.msg)
      // 使用默认数据作为后备
      carouselItems.value = getDefaultCarouselData()
    }
  } catch (error) {
    console.error('获取走马灯数据出错:', error)
    // 使用默认数据作为后备
    carouselItems.value = getDefaultCarouselData()
  }
}

// 获取研究方向数据
const fetchResearchDirections = async () => {
  try {
    loading.value = true
    const response = await request.get('/direction/parent')

    if (response.code == 200) {
      researchDirections.value = response.data || []
      // 移除默认展开第一个研究方向的功能
      // if (researchDirections.value.length > 0) {
      //   selectedIndex.value = 0
      // }
    } else {
      console.error('获取研究方向数据失败:', response.msg)
      ElMessage.error('获取研究方向数据失败')
      // 使用默认数据作为后备
      researchDirections.value = getDefaultResearchDirections()
    }
  } catch (error) {
    console.error('获取研究方向数据出错:', error)
    ElMessage.error('获取研究方向数据出错，请检查网络连接')
    // 使用默认数据作为后备
    researchDirections.value = getDefaultResearchDirections()
  } finally {
    loading.value = false
  }
}

// 获取最新新闻数据
const fetchRecentNews = async () => {
  try {
    newsLoading.value = true
    const response = await request.get('/news/latest')
    console.log('response', response)
    if (response.code == 200) {
      recentNews.value = response.data || []
    } else {
      console.error('获取新闻数据失败:', response.msg)
      ElMessage.error('获取新闻数据失败')
      // 使用默认数据作为后备
      recentNews.value = getDefaultNewsData()
    }
  } catch (error) {
    console.error('获取新闻数据出错:', error)
    ElMessage.error('获取新闻数据出错，请检查网络连接')
    // 使用默认数据作为后备
    recentNews.value = getDefaultNewsData()
  } finally {
    newsLoading.value = false
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '未知日期'
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  } catch (error) {
    return dateString
  }
}

// 根据索引获取新闻类型
const getNewsType = (index: number) => {
  const types = ['primary', 'success', 'warning', 'info', 'danger']
  return types[index % types.length]
}

// 根据索引获取新闻图标
const getNewsIcon = (index: number) => {
  const icons = [Promotion, Trophy, User, Promotion, Trophy]
  return icons[index % icons.length]
}

// 走马灯数据
const getDefaultCarouselData = () => {
  return [
    {
      id: 1,
      title: '智能控制理论研究',
      pictureurl: ''
    },
    {
      id: 2,
      title: '先进系统设计',
      pictureurl: ''
    },
    {
      id: 3,
      title: '科研成果展示',
      pictureurl: ''
    },
    {
      id: 4,
      title: '团队建设',
      pictureurl: ''
    }
  ]
}

// 默认研究方向数据
const getDefaultResearchDirections = () => {
  return [
    {
      id: 1,
      name: '智能控制算法',
      introduction: '研究基于深度学习和强化学习的智能控制算法，提升系统自适应能力和智能化水平。重点研究神经网络控制、模糊控制、自适应控制等先进算法在复杂系统中的应用。',
      pictureurl: '',
      level: 1
    },
    {
      id: 2,
      name: '系统优化设计',
      introduction: '开发多目标优化算法，提高复杂系统的性能和可靠性。研究遗传算法、粒子群优化、模拟退火等优化技术在工程系统设计中的应用。',
      pictureurl: '',
      level: 1
    },
    {
      id: 3,
      name: '人工智能应用',
      introduction: '将AI技术应用于工业自动化、智能制造等实际工程领域。研究计算机视觉、自然语言处理、知识图谱等技术在工业场景中的落地应用。',
      pictureurl: '',
      level: 1
    },
    {
      id: 4,
      name: '机器人技术',
      introduction: '研究智能机器人感知、决策和控制技术。重点研究机器人运动规划、SLAM技术、人机交互等前沿方向，推动机器人智能化发展。',
      pictureurl: '',
      level: 1
    },
    {
      id: 5,
      name: '物联网系统',
      introduction: '开发基于物联网的智能监控和管理系统。研究传感器网络、边缘计算、云平台集成等技术，构建智能化的物联网应用体系。',
      pictureurl: '',
      level: 1
    },
    {
      id: 6,
      name: '数据分析',
      introduction: '利用大数据技术分析系统运行数据，优化控制策略。研究数据挖掘、机器学习、统计分析等方法在工业大数据中的应用。',
      pictureurl: '',
      level: 1
    }
  ]
}

// 默认新闻数据
const getDefaultNewsData = () => {
  return [
    {
      id: 1,
      title: '国家自然科学基金项目获批',
      summary: '实验室成功获批国家自然科学基金重点项目，资助金额200万元',
      content: '实验室成功approved国家自然科学基金重点项目，资助金额200万元',
      time: '2024-12-15',
      type: '科研动态',
      pictureurl: ''
    },
    {
      id: 2,
      title: '国际会议最佳论文奖',
      summary: '实验室研究生在国际智能控制会议上荣获最佳论文奖',
      content: '实验室研究生在国际智能控制会议上荣获最佳论文奖',
      time: '2024-12-08',
      type: '学术成果',
      pictureurl: ''
    },
    {
      id: 3,
      title: '新成员加入',
      summary: '欢迎3名博士研究生和5名硕士研究生加入实验室团队',
      content: '欢迎3名博士研究生和5名硕士研究生加入实验室团队',
      time: '2024-12-01',
      type: '团队建设',
      pictureurl: ''
    },
    {
      id: 4,
      title: '校企合作项目启动',
      summary: '与某知名企业合作开展智能制造技术研发项目',
      content: '与某知名企业合作开展智能制造技术研发项目',
      time: '2024-11-20',
      type: '合作交流',
      pictureurl: ''
    },
    {
      id: 5,
      title: '实验室开放日活动',
      summary: '实验室举办开放日活动，邀请校内外师生参观交流',
      content: '实验室举办开放日活动，邀请校内外师生参观交流',
      time: '2024-11-10',
      type: '活动通知',
      pictureurl: ''
    }
  ]
}

// 组件挂载时获取数据
onMounted(() => {
  fetchLabInfo()
  fetchCarouselData()
  fetchResearchDirections()
  fetchRecentNews()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.carousel-section {
  width: min(96%, 1700px);
  margin: 0 auto;
  padding: 26px 20px 34px;
  background: linear-gradient(135deg, white 0%, white 100%);
}

/* 走马灯容器样式 */
:deep(.el-carousel) {
  position: relative;
}

/* 走马灯项目样式 */
:deep(.el-carousel__item) {
  transition: all 0.5s ease;
  border-radius: 18px;
  overflow: hidden;
}

/* 中间图片放大并置于前方 */
:deep(.el-carousel__item.is-active) {
  z-index: 4;
  box-shadow: 0 24px 55px rgba(20, 50, 100, .25);
}

/* 旁边两张图片缩小并置于后方 */
:deep(.el-carousel__item:not(.is-active)) {
  opacity: 0.72;
  z-index: 2;
  filter: saturate(.8) brightness(.9);
}

.carousel-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: center;
  color: white;
  text-align: center;
  padding: 20px;
  position: relative;
}

.carousel-content h3 {
  font-size: 24px;
  margin-bottom: 10px;
  background: rgba(0, 0, 0, 0.5);
  padding: 10px 20px;
  border-radius: 8px;
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 3;
  backdrop-filter: blur(5px);
}

.carousel-image-container {
  width: 100%;
  height: 390px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 10px;
}

/* 中间图片更大 */
:deep(.el-carousel__item.is-active) .carousel-image-container {
  height: 410px;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.carousel-image-placeholder {
  color: rgba(255, 255, 255, 0.7);
  font-size: 60px;
}

.main-content {
  flex: 1;
  padding: 40px 20px;
  max-width: 1600px;
  width: 92%;
  margin: 0 auto;
}

.home-split {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(360px, .85fr);
  gap: 28px;
  align-items: start;
}

.welcome-section h2 {
  color: #1e3a8a;
  margin-bottom: 20px;
  font-size: 28px;
}

.welcome-section p {
  font-size: 16px;
  line-height: 1.6;
  color: #666;
}

/* 研究方向横向滚动样式 */
.research-directions-section {
  margin-top: 40px;
}

.research-header {
  text-align: center;
  margin-bottom: 40px;
  animation: fadeInUp 1s ease-out;
}

.research-title {
  font-size: 2.5rem;
  font-weight: 800;
  background: linear-gradient(135deg, #1e3a8a, #3b82f6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 15px;
  position: relative;
  text-shadow: 0 2px 4px rgba(30, 58, 138, 0.3);
  letter-spacing: 1px;
}
.research-title::after {
  content: '';
  position: absolute;
  bottom: -15px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, #3b82f6, #1e3a8a);
  border-radius: 2px;
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.3);
}

.research-subtitle {
  font-size: 1.1rem;
  color: #64748b;
  letter-spacing: 2px;
  margin-top: 20px;
  font-weight: 300;
}

/* 加载状态样式 */
.loading-container {
  padding: 40px 0;
}

/* 横向滚动容器 */
.research-scroll-container {
  display: flex;
  gap: 0;
  margin-top: 40px;
  height: 525px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(30, 58, 138, 0.15);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  /* 隐藏滚动条 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

/* 隐藏Webkit浏览器的滚动条 */
.research-scroll-container::-webkit-scrollbar {
  display: none;
}

/* 研究方向项目 */
.research-item {
  position: relative;
  flex: 1;
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.1));
}

.research-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  opacity: 0.3;
  transform: rotate(5deg) scale(1.1);
  transition: all 0.8s ease;
  z-index: 1;
}

.research-item::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2;
}

.research-item.expanded {
  flex: 3;
}

.research-item.expanded::before {
  opacity: 0.6;
  transform: rotate(0deg) scale(1.05);
}

.research-item:not(.expanded):hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.3);
}

.research-item:not(.expanded):hover::before {
  opacity: 0.5;
  transform: rotate(3deg) scale(1.08);
}

/* 智能控制算法 - 蓝色主题 */
.research-item-0::after {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.3), rgba(37, 99, 235, 0.2));
}

.research-item-0.expanded::after {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(37, 99, 235, 0.1));
}

.research-item-0:hover::after {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.25), rgba(37, 99, 235, 0.15));
}

/* 系统优化设计 - 橙色主题 */
.research-item-1::before {
  transform: scaleX(-1) rotate(5deg) scale(1.1);
}

.research-item-1::after {
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.3), rgba(234, 88, 12, 0.2));
}

.research-item-1.expanded::after {
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.15), rgba(234, 88, 12, 0.1));
}

.research-item-1:hover::after {
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.25), rgba(234, 88, 12, 0.15));
}

/* 人工智能应用 - 绿色主题 */
.research-item-2::before {
  transform: scaleY(-1) rotate(5deg) scale(1.1);
}

.research-item-2::after {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.3), rgba(22, 163, 74, 0.2));
}

.research-item-2.expanded::after {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.15), rgba(22, 163, 74, 0.1));
}

.research-item-2:hover::after {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.25), rgba(22, 163, 74, 0.15));
}

/* 机器人技术 - 紫色主题 */
.research-item-3::before {
  transform: scaleX(-1) scaleY(-1) rotate(5deg) scale(1.1);
}

.research-item-3::after {
  background: linear-gradient(135deg, rgba(168, 85, 247, 0.3), rgba(147, 51, 234, 0.2));
}

.research-item-3.expanded::after {
  background: linear-gradient(135deg, rgba(168, 85, 247, 0.15), rgba(147, 51, 234, 0.1));
}

.research-item-3:hover::after {
  background: linear-gradient(135deg, rgba(168, 85, 247, 0.25), rgba(147, 51, 234, 0.15));
}

/* 物联网系统 - 青色主题 */
.research-item-4::before {
  transform: scaleX(0.8) rotate(5deg) scale(1.1);
}

.research-item-4::after {
  background: linear-gradient(135deg, rgba(6, 182, 212, 0.3), rgba(8, 145, 178, 0.2));
}

.research-item-4.expanded::after {
  background: linear-gradient(135deg, rgba(6, 182, 212, 0.15), rgba(8, 145, 178, 0.1));
}

.research-item-4:hover::after {
  background: linear-gradient(135deg, rgba(6, 182, 212, 0.25), rgba(8, 145, 178, 0.15));
}

/* 数据分析 - 粉色主题 */
.research-item-5::before {
  transform: scaleY(0.8) rotate(5deg) scale(1.1);
}

.research-item-5::after {
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.3), rgba(219, 39, 119, 0.2));
}

.research-item-5.expanded::after {
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.15), rgba(219, 39, 119, 0.1));
}

.research-item-5:hover::after {
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.25), rgba(219, 39, 119, 0.15));
}

/* 折叠状态标题卡片 - 竖向展示 */
.research-tab {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
  padding: 20px;
  color: white;
  text-align: center;
}

.tab-title {
  font-size: 20px;
  font-weight: 600;
  line-height: 1.3;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  writing-mode: vertical-rl;
  text-orientation: mixed;
  letter-spacing: 4px;
}

/* 展开状态详细内容 */
.research-content {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 3;
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.6s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.research-item.expanded .research-content {
  opacity: 1;
  transform: scale(1);
}

.content-inner {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 30px;
  max-width: 90%;
  margin: 0 auto;
  text-align: center;
}

.content-title {
  font-size: 24px;
  color: #1e3a8a;
  margin-bottom: 16px;
  font-weight: 600;
}

.content-description {
  font-size: 16px;
  line-height: 1.6;
  color: #4b5563;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
}

/* 近期新闻样式 */
.recent-news-section {
  margin-top: 40px;
}

.news-header {
  text-align: center;
  margin-bottom: 30px;
}

.news-header h3 {
  font-size: 2rem;
  color: #1e3a8a;
  margin-bottom: 10px;
  font-weight: 600;
}

.news-subtitle {
  font-size: 1rem;
  color: #64748b;
  letter-spacing: 2px;
}

.news-list-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

/* 新闻项样式 */
.news-item {
  cursor: pointer;
  padding: 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.news-item:hover {
  background-color: #f8fafc;
  border-color: #e2e8f0;
  transform: translateX(5px);
}

.news-title-link {
  color: #1e3a8a;
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 8px;
  transition: color 0.3s ease;
}

.news-item:hover .news-title-link {
  color: #3b82f6;
}

.news-item p {
  color: #64748b;
  line-height: 1.5;
  margin: 0;
}

/* 时间线样式调整 */
:deep(.el-timeline) {
  padding-left: 0;
}

:deep(.el-timeline-item__node) {
  background-color: #3b82f6;
}

:deep(.el-timeline-item__timestamp) {
  color: #64748b;
  font-weight: 500;
}

/* 动画效果 */
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

/* 响应式设计 */
@media (max-width: 768px) {
  .home-split {
    grid-template-columns: 1fr;
  }
  .research-title {
    font-size: 2rem;
  }

  .research-scroll-container {
    height: 450px;
  }

  .news-header h3 {
    font-size: 1.5rem;
  }

  .news-item {
    padding: 12px;
  }

  .news-title-link {
    font-size: 1rem;
  }
}
</style>
