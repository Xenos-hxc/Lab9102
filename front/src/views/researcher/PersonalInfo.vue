<template>
  <div class="personal-info-container">
    <!-- 页面标题 -->
    <!--    <div class="page-header">-->
    <!--      <h1 class="page-title">个人信息管理</h1>-->
    <!--      <p class="page-subtitle">管理您的个人信息和资料</p>-->
    <!--    </div>-->

    <!-- 主要内容区域 -->
    <div class="content-wrapper">
      <el-row :gutter="24">
        <!-- 左侧个人信息卡片 -->
        <el-col :span="8">
          <el-card class="info-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="card-title">基本信息</span>
                <el-button type="primary" size="small" @click="handleEdit">编辑信息</el-button>
              </div>
            </template>

            <!-- 头像区域 -->
            <div class="avatar-section">
              <el-avatar :size="100" :src="getImageUrl(memberInfo.pictureurl)" fit="cover">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="avatar-actions">
                <el-button type="text" @click="handleAvatarUpload">更换头像</el-button>
              </div>
            </div>

            <!-- 基本信息展示 -->
            <div class="info-display">
              <div class="info-item">
                <label>姓名：</label>
                <span>{{ memberInfo.name || '未设置' }}</span>
              </div>
              <div class="info-item">
                <label>身份：</label>
                <el-tag :type="getIdentityTagType(memberInfo.identity)">
                  {{ getIdentityLabel(memberInfo.identity) || '未设置' }}
                </el-tag>
              </div>
              <div class="info-item">
                <label>邮箱：</label>
                <span>{{ memberInfo.email || '未设置' }}</span>
              </div>
              <div class="info-item">
                <label>入学/职年份：</label>
                <span>{{ formatEntryTime(memberInfo.entrytime) || '未设置' }}</span>
              </div>
              <div class="info-item" v-if="memberInfo.identity === 'graduate'">
                <label>毕业去向：</label>
                <span>{{ memberInfo.workplace || '未设置' }}</span>
              </div>
              <!-- 研究方向展示 -->
              <div class="info-item research-directions">
                <label>研究方向：</label>
                <div class="directions-list">
                  <el-tag
                    v-for="direction in researchDirections"
                    :key="direction"
                    type="primary"
                    class="direction-tag"
                  >
                    {{ direction }}
                  </el-tag>
                  <span v-if="researchDirections.length === 0" class="no-directions">未设置</span>
                </div>
              </div>
              <div class="info-item">
                <label>个人链接：</label>
                <el-link v-if="memberExternalProfileUrl" :href="memberExternalProfileUrl" target="_blank" type="primary">
                  {{ memberInfo.profileLabel || '个人主页' }}
                </el-link>
                <span v-else>未设置</span>
              </div>
              <div v-if="memberInfo.identity !== 'mentor'" class="info-item research-directions">
                <label>我的导师：</label>
                <div class="directions-list">
                  <el-tag v-for="member in selectedMentors" :key="member.id" type="warning">{{ member.name }}</el-tag>
                  <span v-if="selectedMentors.length === 0" class="no-directions">未设置</span>
                </div>
              </div>
              <div v-if="memberInfo.identity === 'mentor'" class="info-item research-directions">
                <label>我的学生：</label>
                <div class="directions-list">
                  <el-tag v-for="member in selectedStudents" :key="member.id" type="success">{{ member.name }}</el-tag>
                  <span v-if="selectedStudents.length === 0" class="no-directions">未设置</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧编辑区域 -->
        <el-col :span="16">
          <el-card class="edit-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="card-title">编辑个人信息</span>
                <el-button type="warning" size="small" @click="passwordDialogVisible = true">修改密码</el-button>
              </div>
            </template>

            <el-form
              ref="formRef"
              :model="editForm"
              :rules="formRules"
              label-width="100px"
              class="edit-form"
            >
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="姓名" prop="name">
                    <el-input v-model="editForm.name" placeholder="请输入姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="身份" prop="identity">
                    <el-select v-model="editForm.identity" placeholder="请选择身份" style="width: 100%">
                      <el-option label="负责人" value="director" />
                      <el-option label="导师" value="mentor" />
                      <el-option label="博士研究生" value="phd" />
                      <el-option label="硕士研究生" value="master" />
                      <el-option label="毕业生" value="graduate" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="邮箱" prop="email">
                <el-input v-model="editForm.email" placeholder="请输入邮箱地址" />
              </el-form-item>

              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="链接名称">
                    <el-input v-model="editForm.profileLabel" placeholder="Google Scholar" />
                  </el-form-item>
                </el-col>
                <el-col :span="16">
                  <el-form-item label="个人链接">
                    <el-input v-model="editForm.profileUrl" placeholder="https://scholar.google.com/..." />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="入学/职年份" prop="entrytime">
                <el-date-picker
                  v-model="editForm.entrytime"
                  type="year"
                  placeholder="选择年份"
                  style="width: 100%"
                  value-format="YYYY"
                />
              </el-form-item>

              <el-form-item v-if="editForm.identity !== 'mentor'" label="我的导师">
                <el-select v-model="editForm.mentorIds" multiple filterable clearable placeholder="从成员中选择导师，可多选" style="width: 100%">
                  <el-option v-for="member in mentorOptions" :key="member.id" :label="member.name" :value="member.id" />
                </el-select>
              </el-form-item>

              <el-form-item v-if="editForm.identity === 'mentor'" label="我的学生">
                <el-select v-model="editForm.studentIds" multiple filterable clearable placeholder="导师可添加或删除学生" style="width: 100%">
                  <el-option v-for="member in studentOptions" :key="member.id" :label="`${member.name}（${getIdentityLabel(member.identity)}）`" :value="member.id" />
                </el-select>
              </el-form-item>

              <el-form-item label="毕业去向" prop="workplace" v-if="editForm.identity === 'graduate'">
                <el-input v-model="editForm.workplace" placeholder="请输入毕业去向" />
              </el-form-item>

              <!-- 研究方向编辑 -->
              <el-form-item label="研究方向" prop="directions">
                <el-select
                  v-model="editForm.directions"
                  multiple
                  filterable
                  placeholder="请选择研究方向"
                  style="width: 100%"
                >
                  <el-option-group
                    v-for="group in directionOptions"
                    :key="group.label"
                    :label="group.label"
                  >
                    <el-option
                      v-for="item in group.options"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-option-group>
                </el-select>
                <div class="directions-tips">
                  <p>可多选研究方向，支持搜索筛选</p>
                  <p>父级研究方向包含其所有子方向</p>
                </div>
              </el-form-item>

              <el-form-item label="个人介绍" prop="introduction">
                <el-input
                  v-model="editForm.introduction"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入个人介绍"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item>
                <div class="form-actions">
                  <el-button type="primary" @click="handleSave" :loading="saving">
                    {{ saving ? '保存中...' : '保存信息' }}
                  </el-button>
                  <el-button @click="handleCancel">取消</el-button>
                  <el-button type="danger" @click="handleLogout">退出登录</el-button>
                </div>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 头像上传对话框 -->
    <el-dialog
      v-model="avatarDialogVisible"
      title="更换头像"
      width="400px"
      :before-close="handleAvatarDialogClose"
    >
      <div class="avatar-upload-dialog">
        <el-upload
          class="avatar-uploader"
          action="#"
          :show-file-list="false"
          :before-upload="beforeAvatarUpload"
          :http-request="handleAvatarUploadRequest"
        >
          <el-avatar v-if="avatarUrl" :size="120" :src="avatarUrl" fit="cover" />
          <div v-else class="avatar-uploader-icon">
            <el-icon><Plus /></el-icon>
            <div>点击上传头像</div>
          </div>
        </el-upload>
        <div class="upload-tips">
          <p>建议上传正方形图片，大小不超过2MB</p>
          <p>支持 JPG、PNG 格式</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="avatarDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAvatarConfirm" :loading="uploading">
          确认上传
        </el-button>
      </template>
    </el-dialog>

    <!-- 密码修改对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="500px"
      :before-close="handlePasswordDialogClose"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordUpdate" :loading="updatingPassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  clearForumUser,
  getForumUser,
  getForumUsername,
  saveForumUser
} from '@/utils/forumUser'
import { ElMessage, ElMessageBox, type FormInstance, type UploadRequestOptions } from 'element-plus'
import { User, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

// 成员信息接口定义
interface Member {
  id: number
  name: string
  identity: string
  introduction: string
  entrytime: string
  email: string
  pictureurl: string
  username: string
  password: string
  workplace?: string
  profileUrl?: string
  profileLabel?: string
}

// 研究方向接口定义
interface Direction {
  id: number
  name: string
  level: number
  parentid?: number
}

// 研究方向选项接口
interface DirectionOption {
  value: number
  label: string
}

// 研究方向分组接口
interface DirectionGroup {
  label: string
  options: DirectionOption[]
}

// 响应式数据
const memberInfo = ref<Member>({
  id: 0,
  name: '',
  identity: '',
  introduction: '',
  entrytime: '',
  email: '',
  pictureurl: '',
  username: '',
  password: ''
})

const editForm = reactive({
  name: '',
  identity: '',
  introduction: '',
  entrytime: '',
  email: '',
  workplace: '',
  profileUrl: '',
  profileLabel: '',
  directions: [] as number[],
  mentorIds: [] as number[],
  studentIds: [] as number[]
})

const saving = ref(false)
const avatarDialogVisible = ref(false)
const avatarUrl = ref('')
const uploading = ref(false)
const researchDirections = ref<string[]>([])
const allDirections = ref<Direction[]>([])
const directionOptions = ref<DirectionGroup[]>([])
const allMembers = ref<Member[]>([])

const mentorOptions = computed(() => allMembers.value.filter(member =>
  member.id !== memberInfo.value.id && member.identity === 'mentor'
))
const studentOptions = computed(() => allMembers.value.filter(member =>
  member.id !== memberInfo.value.id && member.username !== 'admin' && member.identity !== 'mentor'
))
const selectedMentors = computed(() => allMembers.value.filter(member => editForm.mentorIds.includes(member.id)))
const selectedStudents = computed(() => allMembers.value.filter(member => editForm.studentIds.includes(member.id)))
const memberExternalProfileUrl = computed(() => normalizeExternalUrl(memberInfo.value.profileUrl))

const normalizeExternalUrl = (value?: string) => {
  const url = value?.trim()
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  return `https://${url.replace(/^\/+/, '')}`
}

// 密码修改相关数据
const passwordDialogVisible = ref(false)
const updatingPassword = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  identity: [
    { required: true, message: '请选择身份', trigger: 'change' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  entrytime: [
    { required: true, message: '请选择入学/职年份', trigger: 'change' }
  ]
}

// 密码验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 1, max: 20, message: '密码长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value === passwordForm.oldPassword) {
          callback(new Error('新密码不能与原密码相同'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 身份标签类型映射
const getIdentityTagType = (identity: string) => {
  const typeMap: Record<string, string> = {
    'director': 'danger',
    'mentor': 'warning',
    'phd': 'success',
    'master': 'primary',
    'graduate': 'info'
  }
  return typeMap[identity] || 'info'
}

// 身份标签文本映射
const getIdentityLabel = (identity: string) => {
  const labelMap: Record<string, string> = {
    'director': '负责人',
    'mentor': '导师',
    'phd': '博士研究生',
    'master': '硕士研究生',
    'graduate': '毕业生'
  }
  return labelMap[identity] || identity
}

// 格式化入学/职年份
const formatEntryTime = (entrytime: string) => {
  if (!entrytime) return ''
  return entrytime
}

// 获取图片URL
const getImageUrl = (pictureurl: string) => {
  if (!pictureurl) return ''
  if (pictureurl.startsWith('http')) return pictureurl
  return `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:9001'}${pictureurl.startsWith('/') ? '' : '/'}${pictureurl}`
}

// 获取研究方向数据
const fetchDirections = async () => {
  try {
    const response = await request.get('/direction/list')
    if (response.code == 200) {
      allDirections.value = response.data || []
      buildDirectionOptions()
    }
  } catch (error) {
    console.error('获取研究方向数据失败:', error)
  }
}

const fetchMembers = async () => {
  try {
    const response = await request.get('/member/list')
    if (response.code == 200) allMembers.value = response.data || []
  } catch (error) {
    console.error('获取成员列表失败:', error)
  }
}

const fetchRelationships = async () => {
  if (!memberInfo.value.id) return
  try {
    const response = await request.get(`/member/relationships/${memberInfo.value.id}`)
    if (response.code == 200) {
      editForm.mentorIds = response.data?.mentorIds || []
      editForm.studentIds = response.data?.studentIds || []
    }
  } catch (error) {
    console.error('获取导师学生关系失败:', error)
  }
}

// 构建研究方向选项
const buildDirectionOptions = () => {
  const parentDirections = allDirections.value.filter(d => d.level === 1)
  const childDirections = allDirections.value.filter(d => d.level === 2)

  // 创建父级研究方向选项组
  const parentOptions = parentDirections.map(parent => ({
    value: parent.id,
    label: `[父级] ${parent.name}`
  }))

  // 创建子级研究方向选项组，按父级分类
  const childOptions = parentDirections.map(parent => ({
    label: `${parent.name} - 子方向`,
    options: childDirections
      .filter(child => child.parentid === parent.id)
      .map(child => ({
        value: child.id,
        label: child.name
      }))
  }))

  // 合并选项，先显示父级研究方向，再显示子级研究方向
  directionOptions.value = [
    {
      label: '父级研究方向',
      options: parentOptions
    },
    ...childOptions
  ]
}

// 获取当前用户的研究方向
const fetchMemberDirections = async () => {
  try {
    const response = await request.get(`/memberdirection/member/${memberInfo.value.id}`)
    if (response.code == 200 && response.data) {
      // 获取研究方向名称
      const directionIds = response.data.map((rel: any) => rel.directionid)
      const directionNames = directionIds.map((id: number) => {
        const direction = allDirections.value.find(d => d.id === id)
        if (direction) {
          // 如果是父级研究方向，添加标识
          if (direction.level === 1) {
            return `[父级] ${direction.name}`
          }
          return direction.name
        }
        return ''
      }).filter((name: string) => name)

      researchDirections.value = directionNames
      editForm.directions = directionIds
    }
  } catch (error) {
    console.error('获取用户研究方向失败:', error)
  }
}

// 保存研究方向
const saveMemberDirections = async () => {
  try {
    // 先删除原有的研究方向关联
    const currentResponse = await request.get(`/memberdirection/member/${memberInfo.value.id}`)
    if (currentResponse.code == 200 && currentResponse.data.length > 0) {
      for (const relation of currentResponse.data) {
        await request.delete(`/memberdirection/${relation.id}`)
      }
    }

    // 添加新的研究方向关联
    for (const directionId of editForm.directions) {
      await request.post('/memberdirection/save', {
        memberid: memberInfo.value.id,
        directionid: directionId
      })
    }

    return true
  } catch (error) {
    console.error('保存研究方向失败:', error)
    return false
  }
}

// 获取当前用户信息
const fetchMemberInfo = async () => {
  try {
    const response = await request.get('/member/current', {
      params: {
        username: getForumUsername()
      }
    })
    if (response.code == 200) {
      Object.assign(memberInfo.value, response.data)
      const storedUser = getForumUser()
      if (storedUser) {
        saveForumUser({
          ...storedUser,
          id: response.data.id,
          username: response.data.username,
          name: response.data.name,
          identity: response.data.identity
        })
      }
      // 同步到编辑表单
      Object.assign(editForm, {
        name: response.data.name,
        identity: response.data.identity,
        introduction: response.data.introduction,
        entrytime: response.data.entrytime,
        email: response.data.email,
        workplace: response.data.workplace || '',
        profileUrl: response.data.profileUrl || '',
        profileLabel: response.data.profileLabel || ''
      })

      // 获取研究方向
      await fetchMemberDirections()
      await fetchRelationships()
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 编辑信息
const handleEdit = () => {
  ElMessage.info('请在下方的表单中编辑您的信息')
}

// 保存信息
const handleSave = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    saving.value = true

    const { directions, mentorIds, studentIds, ...profileFields } = editForm
    const updateData = {
      ...profileFields,
      id: memberInfo.value.id
    }

    // 更新基本信息
    const response = await request.put('/member/update', updateData)
    if (response.code == 200) {
      // 更新研究方向
      const directionSuccess = await saveMemberDirections()
      const relationshipResponse = await request.put(`/member/relationships/${memberInfo.value.id}`,
        editForm.identity === 'mentor'
          ? { mentorIds: [], studentIds }
          : { mentorIds, studentIds: [] }
      )

      if (directionSuccess && relationshipResponse.code == 200) {
        ElMessage.success('个人信息更新成功')
        // 更新本地数据
        Object.assign(memberInfo.value, updateData)
        const storedUser = getForumUser()
        if (storedUser) {
          saveForumUser({
            ...storedUser,
            name: editForm.name,
            identity: editForm.identity
          })
        }
        // 重新获取研究方向显示
        await fetchMemberDirections()
        await fetchRelationships()
      } else {
        ElMessage.warning('基本信息已更新，但研究方向或人员关系更新失败')
      }
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error: any) {
    if (error.errors) {
      ElMessage.error('请检查表单填写是否正确')
    } else {
      ElMessage.error('保存失败，请稍后重试')
    }
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

// 取消编辑
const handleCancel = () => {
  // 重置表单为原始数据
  Object.assign(editForm, {
    name: memberInfo.value.name,
    identity: memberInfo.value.identity,
    introduction: memberInfo.value.introduction,
    entrytime: memberInfo.value.entrytime,
    email: memberInfo.value.email,
    workplace: memberInfo.value.workplace || '',
    profileUrl: memberInfo.value.profileUrl || '',
    profileLabel: memberInfo.value.profileLabel || ''
  })
  // 重置研究方向
  fetchMemberDirections()
  fetchRelationships()
  ElMessage.info('已取消编辑')
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 清除登录状态
    await request.post('/auth/logout')
    clearForumUser()

    ElMessage.success('退出登录成功')
    router.push('/')
  } catch (error) {
    // 用户取消退出
  }
}

// 头像上传相关方法
const handleAvatarUpload = () => {
  avatarDialogVisible.value = true
  avatarUrl.value = getImageUrl(memberInfo.value.pictureurl)
}

const handleAvatarDialogClose = (done: () => void) => {
  if (avatarUrl.value !== getImageUrl(memberInfo.value.pictureurl)) {
    ElMessageBox.confirm('头像尚未保存，确定要关闭吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      avatarUrl.value = ''
      done()
    }).catch(() => {
      // 取消关闭
    })
  } else {
    done()
  }
}

const beforeAvatarUpload = (file: File) => {
  const isJPGOrPNG = file.type == 'image/jpeg' || file.type == 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPGOrPNG) {
    ElMessage.error('头像图片只能是 JPG 或 PNG 格式!')
    return false
  }
  // if (!isLt2M) {
  //   ElMessage.error('头像图片大小不能超过 2MB!')
  //   return false
  // }

  // 确保返回 true 以继续上传
  return true
}

const handleAvatarUploadRequest = async (options: UploadRequestOptions) => {
  if (!options.file) {
    ElMessage.error('请选择要上传的文件')
    return
  }

  const formData = new FormData()
  formData.append('file', options.file)

  try {
    uploading.value = true
    const response = await request.post('/upload/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (response.code == 200) {
      avatarUrl.value = response.data
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(response.message || '头像上传失败')
    }
  } catch (error: any) {
    console.error('头像上传错误:', error)
    ElMessage.error('头像上传失败: ' + (error.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

const handleAvatarConfirm = async () => {
  if (avatarUrl.value) {
    try {
      const response = await request.put('/member/update-avatar', {
        id: memberInfo.value.id,
        pictureurl: avatarUrl.value
      })
      if (response.code == 200) {
        memberInfo.value.pictureurl = avatarUrl.value
        ElMessage.success('头像更新成功')
        avatarDialogVisible.value = false
      }
    } catch (error) {
      ElMessage.error('头像更新失败')
    }
  }
}

// 密码修改相关方法
const handlePasswordDialogClose = (done: () => void) => {
  if (passwordForm.oldPassword || passwordForm.newPassword || passwordForm.confirmPassword) {
    ElMessageBox.confirm('密码修改尚未保存，确定要关闭吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      resetPasswordForm()
      done()
    }).catch(() => {
      // 取消关闭
    })
  } else {
    done()
  }
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  if (passwordFormRef.value) {
    passwordFormRef.value.clearValidate()
  }
}

const handlePasswordUpdate = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    updatingPassword.value = true

    const updateData = {
      id: memberInfo.value.id,
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    }

    const response = await request.put('/member/update-password', updateData)
    if (response.code == 200) {
      ElMessage.success('密码修改成功')
      passwordDialogVisible.value = false
      resetPasswordForm()
    } else {
      ElMessage.error(response.message || '密码修改失败')
    }
  } catch (error: any) {
    if (error.errors) {
      ElMessage.error('请检查表单填写是否正确')
    } else {
      ElMessage.error('密码修改失败，请稍后重试')
    }
    console.error('密码修改失败:', error)
  } finally {
    updatingPassword.value = false
  }
}

// 组件挂载时获取用户信息和研究方向数据
onMounted(async () => {
  if (!getForumUsername()) {
    ElMessage.warning('请先登录后查看个人信息')
    router.replace('/homePage')
    return
  }
  await Promise.all([fetchDirections(), fetchMembers()])
  await fetchMemberInfo()
})
</script>

<style scoped>
.personal-info-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px 0;
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

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}

.info-card, .edit-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
}

.card-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2c3e50;
}

/* 修改密码按钮样式 */
.card-header .el-button--warning {
  margin-left: 10px;
}

.avatar-section {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.avatar-actions {
  margin-top: 15px;
}

.info-display {
  padding: 0 10px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 8px 0;
  border-bottom: 1px solid #f8f9fa;
}

.info-item label {
  font-weight: 500;
  color: #555;
  min-width: 100px;
}

.info-item span {
  color: #333;
  text-align: right;
}

/* 研究方向特殊样式 */
.info-item.research-directions {
  align-items: flex-start;
  flex-direction: column;
}

.info-item.research-directions label {
  margin-bottom: 8px;
}

.directions-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.direction-tag {
  margin: 2px;
}

.no-directions {
  color: #999;
  font-style: italic;
}

.edit-form {
  padding: 0 20px;
}

.directions-tips {
  margin-top: 8px;
  font-size: 0.85rem;
  color: #666;
}

.directions-tips p {
  margin: 4px 0;
}

.form-actions {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.form-actions .el-button {
  margin: 0 8px;
}

.avatar-upload-dialog {
  text-align: center;
}

.avatar-uploader {
  display: inline-block;
  margin-bottom: 20px;
}

.avatar-uploader-icon {
  width: 120px;
  height: 120px;
  border: 2px dashed #dcdfe6;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #8c939d;
  cursor: pointer;
  transition: border-color 0.3s;
}

.avatar-uploader-icon:hover {
  border-color: #409eff;
}

.upload-tips {
  font-size: 0.9rem;
  color: #666;
  text-align: center;
}

.upload-tips p {
  margin: 4px 0;
}

/* 密码修改对话框样式 */
:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-input) {
  width: 100%;
}
</style>
