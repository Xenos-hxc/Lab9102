<template>
  <div class="contact-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <p class="page-subtitle">智能控制与先进系统实验室 - 期待与您的交流与合作</p>
    </div>

    <!-- 主要内容区域 -->
    <div class="contact-content">
      <!-- 左侧联系信息 -->
      <div class="contact-info">
        <el-card class="info-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><LocationInformation /></el-icon>
              <span>实验室信息</span>
            </div>
          </template>

          <!-- 地址信息 -->
          <div class="info-section">
            <h3 class="section-title">
              <el-icon><Location /></el-icon>
              实验室地址
            </h3>
            <p class="address-text">
              {{ labinfo.address}}<br>
              {{ labinfo.university}} {{ labinfo.college}}
            </p>
          </div>

          <!-- 联系人员 -->
          <div class="info-section">
            <h3 class="section-title">
              <el-icon><User /></el-icon>
              联系人员
            </h3>
            <div v-if="loading" class="loading-container">
              <el-skeleton :rows="3" animated />
            </div>
            <div v-else class="contact-persons">
              <div v-for="person in contactPersons" :key="person.name" class="person-item">
                <div class="person-info">
                  <span class="person-name">{{ person.name }}</span>
                  <span class="person-email">{{ person.email }}</span>
                </div>
                <el-button
                  type="primary"
                  link
                  @click="copyEmail(person.email)"
                  class="copy-btn"
                >
                  <el-icon><DocumentCopy /></el-icon>
                  复制邮箱
                </el-button>
              </div>
            </div>
          </div>

          <!-- 在线时间 -->
          <div class="info-section">
            <h3 class="section-title">
              <el-icon><Clock /></el-icon>
              在线时间
            </h3>
            <div class="office-hours">
              <div class="time-item">
                <span class="time-label">工作日：</span>
                <span class="time-value">{{ labinfo.workdayHours || '09:00 - 17:00' }}</span>
              </div>
              <div class="time-item">
                <span class="time-label">周末：</span>
                <span class="time-value">{{ labinfo.weekendHours || '10:00 - 16:00' }}</span>
              </div>
              <div class="time-item">
                <span class="time-label">节假日：</span>
                <span class="time-value">{{ labinfo.holidayNote || '请提前预约' }}</span>
              </div>
            </div>
          </div>

          <!-- 快速联系按钮 -->
          <div class="quick-contact">
            <el-button type="primary" @click="sendEmail" class="contact-btn">
              <el-icon><Message /></el-icon>
              发送邮件
            </el-button>
            <el-button @click="makeCall" class="contact-btn">
              <el-icon><Phone /></el-icon>
              拨打电话
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- 右侧地图 -->
      <div class="map-container">
        <el-card class="map-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><MapLocation /></el-icon>
              <span>实验室位置</span>
            </div>
          </template>

          <!-- 高德地图iframe -->
          <iframe
            class="map-frame"
            :src="labinfo.mapUrl || 'https://surl.amap.com/7v8SZdS1A66k'"
            width="100%"
            height="450"
            style="border:0;"
            allowfullscreen
            loading="lazy"
            title="智能控制与先进系统实验室位置"
          ></iframe>

          <!-- 地图说明 -->
          <div class="map-note">
            <p>地图显示：{{ labinfo.addressDetail || '西南交通大学信息科学与技术学院' }}</p>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 底部说明 -->
    <div class="footer-note">
      <el-alert
        title="温馨提示"
        type="info"
        :description="`如有任何问题或建议，欢迎随时联系我们。我们将尽快回复您的邮件或电话。联系电话：${labinfo.phone || '028-8760XXXX'}`"
        :closable="false"
        show-icon
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Location,
  User,
  Clock,
  Message,
  Phone,
  MapLocation,
  DocumentCopy,
  LocationInformation
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 实验室信息数据
interface Labinfo {
  labName: string
  university: string
  college: string
  address: string
  addressDetail: string
  phone: string
  workdayHours: string
  weekendHours: string
  holidayNote: string
  mapUrl: string
}

// 联系人员数据
interface ContactPerson {
  name: string
  email: string
}

const labinfo = ref<Labinfo>({
  labName: '',
  address: '',
  college: '',
  university: '',
  addressDetail: '',
  phone: '',
  workdayHours: '',
  weekendHours: '',
  holidayNote: '',
  mapUrl: ''
})

const contactPersons = ref<ContactPerson[]>([])
const loading = ref(false)

// 获取实验室信息
const fetchLabinfo = async () => {
  try {
    loading.value = true
    const response = await request.get('/labinfo/detail')

    if (response.code === 200) {
      const data = response.data
      labinfo.value = {
        labName: data.labName || '智能控制与先进系统实验室',
        address: `${data.addressProvince || '四川省'}${data.addressCity || '成都市'}${data.addressDistrict || '郫都区'}${data.addressStreet || '犀安路999号'}`,
        addressDetail: data.addressDetail || '西南交通大学信息科学与技术学院',
        phone: data.phone || '028-8760XXXX',
        university: data.university || '西南交通大学',
        college: data.college || '信息科学与技术学院',
        workdayHours: data.workdayHours || '09:00 - 17:00',
        weekendHours: data.weekendHours || '10:00 - 16:00',
        holidayNote: data.holidayNote || '请提前预约',
        mapUrl: data.mapUrl || 'https://surl.amap.com/7v8SZdS1A66k'
      }
    } else {
      ElMessage.error('获取实验室信息失败')
    }
  } catch (error) {
    console.error('获取实验室信息出错:', error)
    ElMessage.error('获取实验室信息出错，请检查网络连接')
  } finally {
    loading.value = false
  }
}

// 获取联系人员列表
const fetchContactPersons = async () => {
  try {
    const response = await request.get('/labinfo/contacts')

    if (response.code === 200) {
      contactPersons.value = response.data || []
    } else {
      // 使用默认数据作为后备
      contactPersons.value = [
        { name: '张教授', email: 'contact@example.com' },
        { name: '李老师', email: 'contact@example.com' },
        { name: '王博士', email: 'contact@example.com' },
        { name: '刘助理', email: 'contact@example.com' }
      ]
    }
  } catch (error) {
    console.error('获取联系人员列表出错:', error)
    // 使用默认数据作为后备
    contactPersons.value = [
      { name: '张教授', email: 'contact@example.com' },
      { name: '李老师', email: 'contact@example.com' },
      { name: '王博士', email: 'contact@example.com' },
      { name: '刘助理', email: 'contact@example.com' }
    ]
  }
}

// 联系方法
const copyEmail = (email: string) => {
  navigator.clipboard.writeText(email).then(() => {
    ElMessage.success(`已复制邮箱: ${email}`)
  }).catch(() => {
    // 兼容性处理
    const textArea = document.createElement('textarea')
    textArea.value = email
    document.body.appendChild(textArea)
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    ElMessage.success(`已复制邮箱: ${email}`)
  })
}

const sendEmail = () => {
  const email = contactPersons.value[0]?.email || 'contact@example.com'
  window.open(`mailto:${email}?subject=咨询智能控制与先进系统实验室&body=尊敬的实验室老师：`)
}

const makeCall = () => {
  ElMessage.info(`请拨打实验室电话：${labinfo.value.phone || '028-8760XXXX'}`)
}

// 组件挂载时获取数据
onMounted(() => {
  fetchLabinfo()
  fetchContactPersons()
})
</script>

<style scoped>
.contact-page {
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
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

.contact-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto 40px;
}

.info-card, .map-card {
  border: none;
  border-radius: 16px;
  transition: all 0.3s ease;
}

.info-card:hover, .map-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.2rem;
  font-weight: 600;
  color: #2c3e50;
}

.card-header .el-icon {
  color: #1890ff;
}

.info-section {
  margin-bottom: 30px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.1rem;
  color: #2c3e50;
  margin-bottom: 15px;
  font-weight: 600;
}

.section-title .el-icon {
  color: #1890ff;
}

.address-text {
  font-size: 1rem;
  line-height: 1.6;
  color: #5a6c7d;
  margin: 0;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #1890ff;
}

.contact-persons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.person-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.person-item:hover {
  background: #e3f2fd;
  transform: translateX(4px);
}

.person-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.person-name {
  font-weight: 600;
  color: #2c3e50;
}

.person-email {
  font-size: 0.9rem;
  color: #1890ff;
}

.copy-btn {
  padding: 4px 8px;
  font-size: 0.85rem;
}

.office-hours {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.time-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.time-label {
  font-weight: 500;
  color: #5a6c7d;
}

.time-value {
  color: #2c3e50;
  font-weight: 600;
}

.quick-contact {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.contact-btn {
  flex: 1;
  padding: 12px;
  font-size: 1rem;
}

.map-frame {
  width: 100%;
  height: 450px;
  border-radius: 8px;
  overflow: hidden;
}

.map-note {
  text-align: center;
  margin-top: 10px;
  color: #666;
  font-size: 0.9rem;
}

.footer-note {
  max-width: 1200px;
  margin: 0 auto;
}

.loading-container {
  padding: 20px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .contact-content {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .page-title {
    font-size: 2rem;
  }

  .quick-contact {
    flex-direction: column;
  }

  .person-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .copy-btn {
    align-self: flex-end;
  }

  .map-frame {
    height: 350px;
  }
}

@media (max-width: 480px) {
  .contact-page {
    padding: 15px;
  }

  .page-title {
    font-size: 1.8rem;
  }

  .map-frame {
    height: 300px;
  }
}
</style>
