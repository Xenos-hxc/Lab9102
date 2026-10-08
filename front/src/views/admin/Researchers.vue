<template>
  <div class="researchers-page">
    <!-- 页面标题和操作栏 -->
    <div class="page-header">
      <h1 class="page-title">科研人员管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd" :icon="Plus">
          添加人员
        </el-button>
        <el-button @click="refreshData" :icon="Refresh">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <el-card class="filter-card">
      <div class="filter-container">
        <el-input
          v-model="searchText"
          placeholder="搜索姓名、邮箱或身份"
          clearable
          style="width: 300px"
          :prefix-icon="Search"
          @input="handleSearch"
        />
        <el-select
          v-model="filterIdentity"
          placeholder="筛选身份"
          clearable
          style="width: 200px; margin-left: 12px"
          @change="handleFilter"
        >
          <el-option
            v-for="identity in identityOptions"
            :key="identity"
            :label="getIdentityLabel(identity)"
            :value="identity"
          />
        </el-select>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card>
      <template #header>
        <div class="table-header">
          <span>科研人员列表</span>
          <span class="total-count">共 {{ filteredMembers.length }} 人</span>
        </div>
      </template>

      <el-table
        :data="pagedMembers"
        v-loading="loading"
        stripe
        style="width: 100%"
        :default-sort="{ prop: 'id', order: 'ascending' }"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />

        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <div class="avatar-container">
              <el-avatar
                v-if="row.pictureurl"
                :size="40"
                :src="getImageUrl(row.pictureurl)"
                fit="cover"
              />
              <el-avatar v-else :size="40" :icon="User" />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="name" label="姓名" width="120" sortable>
          <template #default="{ row }">
            <span class="member-name">{{ row.name }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="identity" label="身份" width="120" sortable>
          <template #default="{ row }">
            <el-tag :type="getIdentityTagType(row.identity)">
              {{ getIdentityLabel(row.identity) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="email-cell">
              <el-icon><Message /></el-icon>
              <span>{{ row.email || '未设置' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="个人链接" width="110" align="center">
          <template #default="{ row }">
            <el-link v-if="row.profileUrl" :href="normalizeExternalUrl(row.profileUrl)" target="_blank" type="primary" :underline="false">
              {{ row.profileLabel || '个人主页' }}
            </el-link>
            <span v-else class="no-direction">未设置</span>
          </template>
        </el-table-column>

        <el-table-column label="管理权限" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.username === 'admin' || row.isAdmin ? 'success' : 'info'">
              {{ row.username === 'admin' ? '超级管理员' : row.isAdmin ? '管理员' : '普通成员' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="entrytime" label="入职/入学时间" width="140" sortable>
          <template #default="{ row }">
            {{ formatEntryTime(row.entrytime) }}
          </template>
        </el-table-column>

        <el-table-column prop="introduction" label="个人介绍" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.introduction || '暂无介绍' }}
          </template>
        </el-table-column>

        <el-table-column label="研究方向" min-width="150">
          <template #default="{ row }">
            <div class="directions-cell">
              <el-tag
                v-for="direction in getMemberDirections(row.id)"
                :key="direction.id"
                size="small"
                type="info"
                class="direction-tag"
              >
                {{ direction.name }}
              </el-tag>
              <span v-if="getMemberDirections(row.id).length == 0" class="no-direction">
                未设置
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              :icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="success"
              size="small"
              :icon="View"
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button
              v-if="canGrantPermissions"
              type="warning"
              size="small"
              :icon="Key"
              @click="openPermissionDialog(row)"
            >
              权限
            </el-button>
            <el-button
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredMembers.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="760px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="memberFormRef"
        :model="memberForm"
        :rules="memberFormRules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="memberForm.name" placeholder="请输入姓名" />
        </el-form-item>

        <el-form-item label="身份" prop="identity">
          <el-select v-model="memberForm.identity" placeholder="请选择身份" style="width: 100%">
            <el-option
              v-for="identity in identityOptions"
              :key="identity"
              :label="getIdentityLabel(identity)"
              :value="identity"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="毕业去向" prop="workplace" v-if="memberForm.identity === 'graduate'">
          <el-input v-model="memberForm.workplace" placeholder="请输入毕业去向" />
        </el-form-item>

        <el-form-item label="入职/入学时间" prop="entrytime">
          <el-date-picker
            v-model="memberForm.entrytime"
            type="year"
            placeholder="选择年份"
            value-format="YYYY"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="账号" prop="username">
          <el-input v-model="memberForm.username" placeholder="请输入账号" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="memberForm.password"
            type="password"
            :placeholder="isEditMode ? '******** (留空表示不修改)' : '请输入密码'"
            show-password
          />
        </el-form-item>

        <el-form-item label="研究方向" prop="directions">
          <el-select
            v-model="memberForm.directions"
            multiple
            placeholder="请选择研究方向"
            style="width: 100%"
          >
            <el-option
              v-for="direction in allDirections"
              :key="direction.id"
              :label="getDirectionLabel(direction)"
              :value="direction.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="memberForm.email" placeholder="请输入邮箱（可选）" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="链接名称">
              <el-input v-model="memberForm.profileLabel" placeholder="如 Google Scholar" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="个人链接">
              <el-input v-model="memberForm.profileUrl" placeholder="https://scholar.google.com/..." />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item v-if="memberForm.identity !== 'mentor'" label="导师关系">
          <el-select v-model="memberForm.mentorIds" multiple filterable clearable placeholder="可选择多位导师" style="width: 100%">
            <el-option v-for="member in mentorOptions" :key="member.id" :label="member.name" :value="member.id" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="memberForm.identity === 'mentor'" label="学生关系">
          <el-select v-model="memberForm.studentIds" multiple filterable clearable placeholder="导师可选择多位学生" style="width: 100%">
            <el-option v-for="member in studentOptions" :key="member.id" :label="`${member.name}（${getIdentityLabel(member.identity)}）`" :value="member.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="个人介绍">
          <el-input
            v-model="memberForm.introduction"
            type="textarea"
            :rows="3"
            placeholder="请输入个人介绍（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="handleAvatarUpload"
          >
            <el-avatar v-if="memberForm.pictureurl" :size="100" :src="getImageUrl(memberForm.pictureurl)" />
            <div v-else class="avatar-uploader-icon">
              <el-icon><Plus /></el-icon>
              <div>上传头像（可选）</div>
            </div>
          </el-upload>
          <div class="upload-tip">支持 JPG/PNG 格式，文件大小不超过 2MB</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEditMode ? '更新' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="permissionDialogVisible" title="管理员菜单权限" width="620px">
      <div v-if="permissionMember" class="permission-dialog">
        <div class="permission-user">
          <el-avatar :size="48" :src="getImageUrl(permissionMember.pictureurl)" :icon="User" />
          <div><strong>{{ permissionMember.name }}</strong><span>{{ permissionMember.username }}</span></div>
        </div>
        <el-alert v-if="permissionMember.username === 'admin'" type="info" :closable="false" title="admin 为超级管理员，始终拥有全部菜单权限。" />
        <template v-else>
          <el-switch v-model="permissionForm.isAdmin" active-text="允许进入管理员端" />
          <el-checkbox-group v-model="permissionForm.permissions" class="permission-grid" :disabled="!permissionForm.isAdmin">
            <el-checkbox v-for="item in ADMIN_MENU_OPTIONS" :key="item.key" :value="item.key" border>
              {{ item.label }}
            </el-checkbox>
          </el-checkbox-group>
        </template>
      </div>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="permissionMember?.username === 'admin'" :loading="permissionSaving" @click="savePermissions">保存权限</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="科研人员详情"
      width="500px"
    >
      <div v-if="currentMember" class="member-detail">
        <div class="detail-header">
          <el-avatar :size="80" :src="getImageUrl(currentMember.pictureurl)" :icon="User" />
          <div class="detail-info">
            <h3>{{ currentMember.name }}</h3>
            <el-tag :type="getIdentityTagType(currentMember.identity)">
              {{ getIdentityLabel(currentMember.identity) }}
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="1" border>
          <el-descriptions-item label="邮箱">
            {{ currentMember.email || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="入职/入学时间">
            {{ formatEntryTime(currentMember.entrytime) }}
          </el-descriptions-item>
          <el-descriptions-item label="个人介绍">
            {{ currentMember.introduction || '暂无介绍' }}
          </el-descriptions-item>
          <el-descriptions-item label="毕业去向" v-if="currentMember.identity === 'graduate'">
            {{ currentMember.workplace || '暂无' }}
          </el-descriptions-item>
          <el-descriptions-item label="研究方向">
            <div class="detail-directions">
              <el-tag
                v-for="direction in getMemberDirections(currentMember.id)"
                :key="direction.id"
                type="primary"
                size="small"
              >
                {{ direction.name }}
              </el-tag>
              <span v-if="getMemberDirections(currentMember.id).length == 0">未设置</span>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Refresh,
  Search,
  Edit,
  View,
  Delete,
  User,
  Message,
  Key
} from '@element-plus/icons-vue'
import Request from "@/utils/request.ts"
import { getApiBaseUrl } from '@/utils/apiConfig'
import { getForumUser } from '@/utils/forumUser'
import { ADMIN_MENU_OPTIONS, isRootAdmin } from '@/utils/adminPermissions'

// 类型定义
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
  isAdmin?: boolean
  adminPermissions?: string
}

interface Direction {
  id: number
  name: string
  level: number
  parentid?: number
}

interface MemberDirection {
  id: number
  memberid: number
  directionid: number
}

// 响应式数据
const loading = ref(false)
const searchText = ref('')
const filterIdentity = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const submitting = ref(false)
const permissionSaving = ref(false)
const isEditMode = ref(false)

const allMembers = ref<Member[]>([])
const allDirections = ref<Direction[]>([])
const memberDirections = ref<MemberDirection[]>([])
const currentMember = ref<Member | null>(null)
const permissionMember = ref<Member | null>(null)
const permissionForm = reactive({ isAdmin: false, permissions: [] as string[] })

const memberFormRef = ref<FormInstance>()
const memberForm = ref({
  id: 0,
  name: '',
  identity: '',
  introduction: '',
  entrytime: '',
  email: '',
  pictureurl: '',
  username: '',
  password: '',
  workplace: '',
  profileUrl: '',
  profileLabel: '',
  directions: [] as number[],
  mentorIds: [] as number[],
  studentIds: [] as number[]
})

// 表单验证规则
const memberFormRules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 0, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  identity: [
    { required: true, message: '请选择身份', trigger: 'change' }
  ],
  entrytime: [
    { required: true, message: '请选择入职/入学时间', trigger: 'change' }
  ],
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 0, max: 20, message: '账号长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: false, message: '请输入密码', trigger: 'blur' },
    { min: 0, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 身份选项
const identityOptions = ['mentor', 'phd', 'master', 'graduate']

// 身份标签映射
const identityMap = {

  'mentor': '导师',
  'phd': '博士研究生',
  'master': '硕士研究生',
  'graduate': '毕业生'
}

// 身份标签类型映射
const identityTagTypeMap = {
  'mentor': 'warning',
  'phd': 'success',
  'master': 'primary',
  'graduate': 'info'
}

// 计算属性
const filteredMembers = computed(() => {
  let filtered = allMembers.value

  // 搜索筛选
  if (searchText.value) {
    const keyword = searchText.value.toLowerCase()
    filtered = filtered.filter(member =>
      member.name.toLowerCase().includes(keyword) ||
      member.email?.toLowerCase().includes(keyword) ||
      getIdentityLabel(member.identity).toLowerCase().includes(keyword)
    )
  }

  // 身份筛选
  if (filterIdentity.value) {
    filtered = filtered.filter(member => member.identity == filterIdentity.value)
  }

  return filtered
})

const pagedMembers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredMembers.value.slice(start, end)
})

const mentorOptions = computed(() => allMembers.value.filter(member =>
  member.id !== memberForm.value.id && member.identity === 'mentor'
))

const studentOptions = computed(() => allMembers.value.filter(member =>
  member.id !== memberForm.value.id && member.username !== 'admin' && member.identity !== 'mentor'
))

const canGrantPermissions = computed(() => isRootAdmin(getForumUser()))

const normalizeExternalUrl = (value?: string) => {
  const url = value?.trim()
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  return `https://${url.replace(/^\/+/, '')}`
}

const dialogTitle = computed(() => {
  return isEditMode.value ? '编辑科研人员' : '添加科研人员'
})

// 方法
const getIdentityLabel = (identity: string) => {
  return identityMap[identity as keyof typeof identityMap] || identity
}

const getIdentityTagType = (identity: string) => {
  return identityTagTypeMap[identity as keyof typeof identityTagTypeMap] || 'info'
}

const getDirectionLabel = (direction: Direction) => {
  if (direction.level == 1) {
    return `[父级] ${direction.name}`
  } else {
    const parent = allDirections.value.find(d => d.id == direction.parentid)
    return parent ? `${parent.name} - ${direction.name}` : direction.name
  }
}

const formatEntryTime = (entrytime: string) => {
  if (!entrytime) return '未知时间'
  return `${entrytime}年`
}

const getImageUrl = (pictureurl: string) => {
  if (!pictureurl) return ''
  if (pictureurl.startsWith('http')) return pictureurl
  return `${getApiBaseUrl()}${pictureurl.startsWith('/') ? '' : '/'}${pictureurl}`
}

const getMemberDirections = (memberId: number) => {
  const directionIds = memberDirections.value
    .filter(md => md.memberid == memberId)
    .map(md => md.directionid)

  return allDirections.value.filter(d => directionIds.includes(d.id))
}

// API调用方法
const fetchMembers = async () => {
  try {
    loading.value = true
    const result = await Request('/member/list')
    if (result.code == 200) {
      allMembers.value = result.data
    } else {
      ElMessage.error('获取科研人员数据失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请检查后端服务是否启动')
  } finally {
    loading.value = false
  }
}

const fetchDirections = async () => {
  try {
    const result = await Request('/direction/list')
    if (result.code == 200) {
      allDirections.value = result.data
    }
  } catch (error) {
    console.error('获取研究方向数据失败:', error)
  }
}

const fetchMemberDirections = async () => {
  try {
    const result = await Request('/memberdirection/list')
    if (result.code == 200) {
      memberDirections.value = result.data
    }
  } catch (error) {
    console.error('获取成员研究方向关联失败:', error)
  }
}

// 事件处理
const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

const handleAdd = () => {
  isEditMode.value = false
  memberForm.value = {
    id: 0,
    name: '',
    identity: '',
    introduction: '',
    entrytime: '',
    email: '',
    pictureurl: '',
    username: '',
    password: '',
    workplace: '',
    profileUrl: '',
    profileLabel: '',
    directions: [],
    mentorIds: [],
    studentIds: []
  }
  dialogVisible.value = true
}

const handleEdit = async (member: Member) => {
  isEditMode.value = true
  let relationships = { mentorIds: [] as number[], studentIds: [] as number[] }
  try {
    const response = await Request(`/member/relationships/${member.id}`)
    if (response.code == 200) relationships = response.data || relationships
  } catch (error) {
    console.error('获取导师学生关系失败:', error)
  }
  memberForm.value = {
    id: member.id,
    name: member.name,
    identity: member.identity,
    introduction: member.introduction || '',
    entrytime: member.entrytime || '',
    email: member.email || '',
    pictureurl: member.pictureurl || '',
    username: member.username || '',
    password: '', // 编辑时不显示原密码，需要重新输入
    workplace: member.workplace || '',
    profileUrl: member.profileUrl || '',
    profileLabel: member.profileLabel || '',
    directions: getMemberDirections(member.id).map(d => d.id),
    mentorIds: relationships.mentorIds || [],
    studentIds: relationships.studentIds || []
  }
  dialogVisible.value = true
}

const handleView = (member: Member) => {
  currentMember.value = member
  viewDialogVisible.value = true
}

const openPermissionDialog = (member: Member) => {
  permissionMember.value = member
  permissionForm.isAdmin = member.username === 'admin' || Boolean(member.isAdmin)
  permissionForm.permissions = member.username === 'admin' || member.adminPermissions === '*'
    ? ADMIN_MENU_OPTIONS.map(item => item.key)
    : (member.adminPermissions || '').split(',').map(item => item.trim()).filter(Boolean)
  permissionDialogVisible.value = true
}

const savePermissions = async () => {
  if (!permissionMember.value || permissionMember.value.username === 'admin') return
  permissionSaving.value = true
  try {
    const response = await Request(`/member/permissions/${permissionMember.value.id}`, {
      method: 'PUT',
      data: {
        isAdmin: permissionForm.isAdmin,
        adminPermissions: permissionForm.isAdmin ? permissionForm.permissions.join(',') : ''
      }
    })
    if (response.code == 200) {
      ElMessage.success('管理员权限已更新')
      permissionDialogVisible.value = false
      await fetchMembers()
    } else {
      ElMessage.error(response.data || '权限保存失败')
    }
  } finally {
    permissionSaving.value = false
  }
}

const handleDelete = async (member: Member) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除科研人员 "${member.name}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    if (member.username=='admin'){
      ElMessage.error('不能删除管理员')
      return
    }
    const result = await Request(`/member/${member.id}`, {
      method: 'DELETE'
    })

    if (result.code == 200) {
      ElMessage.success('删除成功')
      await fetchMembers()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    // 用户取消删除
  }
}

const handleSubmit = async () => {
  if (!memberFormRef.value) return

  try {
    const valid = await memberFormRef.value.validate()
    if (!valid) return

    submitting.value = true

    // 处理密码：编辑时如果密码为空，则不更新密码
    const { directions, mentorIds, studentIds, ...memberFields } = memberForm.value
    const submitData: Record<string, unknown> = { ...memberFields }

    // 编辑模式下，如果密码为空，则删除密码字段（不更新密码）
    if (isEditMode.value && !submitData.password) {
      delete submitData.password
    }

    const result = await Request('/member/save', {
      method: 'POST',
      data: submitData
    })

    if (result.code == 200) {
      const savedMember = result.data

      // 保存研究方向关联
      // 先删除旧的研究方向关联，再按当前选择重建，允许清空全部方向。
      const oldRelations = memberDirections.value.filter(md => md.memberid == savedMember.id)
      for (const relation of oldRelations) {
        await Request(`/memberdirection/${relation.id}`, { method: 'DELETE' })
      }
      for (const directionId of directions) {
        await Request('/memberdirection/save', {
          method: 'POST',
          data: { memberid: savedMember.id, directionid: directionId }
        })
      }

      await Request(`/member/relationships/${savedMember.id}`, {
        method: 'PUT',
        data: memberForm.value.identity === 'mentor'
          ? { mentorIds: [], studentIds }
          : { mentorIds, studentIds: [] }
      })

      ElMessage.success(isEditMode.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      await refreshData()
    } else {
      ElMessage.error(result.data)
    }
  } catch (error) {
    ElMessage.error('操作失败，请检查网络连接')
  } finally {
    submitting.value = false
  }
}

const handleDialogClose = () => {
  dialogVisible.value = false
  memberFormRef.value?.clearValidate()
}

const refreshData = async () => {
  await Promise.all([
    fetchMembers(),
    fetchDirections(),
    fetchMemberDirections()
  ])
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
}

const beforeAvatarUpload = (file: File) => {
  const isJPGOrPNG = file.type == 'image/jpeg' || file.type == 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 100

  if (!isJPGOrPNG) {
    ElMessage.error('头像只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

const handleAvatarUpload = async (options: any) => {
  const { file } = options

  try {
    // 创建FormData对象
    const formData = new FormData()
    formData.append('file', file)

    // 上传文件到后端
    const result = await Request('/upload/avatar', {
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (result.code == 200) {
      // 上传成功，设置头像URL
      memberForm.value.pictureurl = result.data
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error('头像上传失败')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败，请检查网络连接')
  }
}

// 生命周期
onMounted(() => {
  refreshData()
})
</script>

<style scoped>
.researchers-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-container {
  display: flex;
  align-items: center;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-count {
  color: #909399;
  font-size: 14px;
}

.avatar-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.member-name {
  font-weight: 600;
  color: #303133;
}

.email-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.directions-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.direction-tag {
  margin: 2px;
}

.no-direction {
  color: #c0c4cc;
  font-style: italic;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.avatar-uploader {
  display: flex;
  justify-content: center;
  margin-bottom: 8px;
}

.avatar-uploader-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 100px;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  color: #8c939d;
  font-size: 12px;
}

.avatar-uploader-icon:hover {
  border-color: #409eff;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  text-align: center;
}

.member-detail {
  padding: 10px;
}

.detail-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
}

.detail-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
}

.detail-directions {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.permission-dialog {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.permission-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 12px;
  background: #f5f8fc;
}

.permission-user strong,
.permission-user span {
  display: block;
}

.permission-user span {
  color: #8492a6;
  font-size: 12px;
  margin-top: 2px;
}

.permission-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.permission-grid :deep(.el-checkbox) {
  width: 100%;
  margin: 0;
}
</style>
