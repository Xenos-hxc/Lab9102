<template>
  <div class="editor-page">
    <div class="editor-shell">
      <header class="editor-header">
        <div>
          <div class="eyebrow">ISAC · 研学社区</div>
          <h1>{{ isEdit ? '编辑研学文章' : '记录新的研学心得' }}</h1>
          <p>把阅读、实验和科研中的思考整理成一篇可以被再次找到的文章。</p>
        </div>
        <el-button @click="router.push('/community/mine')">
          <el-icon><Notebook /></el-icon>
          我的文章
        </el-button>
      </header>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="article-form"
      >
        <section class="form-card primary-card">
          <el-form-item label="文章标题" prop="title">
            <el-input
              v-model="form.title"
              size="large"
              maxlength="200"
              show-word-limit
              placeholder="用一句清晰的标题概括这次分享"
            />
          </el-form-item>

          <div class="form-row">
            <el-form-item label="文章分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option
                  v-for="category in categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="可见范围" prop="visibility">
              <el-radio-group v-model="form.visibility">
                <el-radio-button value="MEMBER">
                  <el-icon><Lock /></el-icon>
                  仅实验室成员
                </el-radio-button>
                <el-radio-button value="PUBLIC">
                  <el-icon><View /></el-icon>
                  公开
                </el-radio-button>
              </el-radio-group>
            </el-form-item>
          </div>

          <el-form-item label="文章摘要">
            <el-input
              v-model="form.summary"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="选填；留空时系统会根据正文自动生成摘要"
            />
          </el-form-item>

          <el-form-item label="文章标签">
            <el-select
              v-model="form.tags"
              multiple
              filterable
              allow-create
              default-first-option
              :multiple-limit="8"
              placeholder="选择或输入标签，最多8个"
              style="width: 100%"
            >
              <el-option v-for="tag in availableTags" :key="tag.id" :label="tag.name" :value="tag.name" />
            </el-select>
          </el-form-item>
        </section>

        <section class="form-card">
          <div class="card-heading">
            <div>
              <h2>正文内容</h2>
              <p>支持标题、列表、引用、代码块、链接、表格和图片。</p>
            </div>
            <el-button text type="primary" @click="previewVisible = true">
              <el-icon><View /></el-icon>
              预览
            </el-button>
          </div>
          <el-form-item prop="content" class="content-item">
            <div class="wang-editor">
              <Toolbar
                :editor="editorRef"
                :default-config="toolbarConfig"
                mode="default"
                class="editor-toolbar"
              />
              <Editor
                v-model="form.content"
                :default-config="editorConfig"
                mode="default"
                class="editor-content"
                @on-created="handleEditorCreated"
              />
            </div>
          </el-form-item>
        </section>

        <section class="form-card media-card">
          <div class="media-grid">
            <div>
              <div class="card-heading compact">
                <div>
                  <h2>文章封面</h2>
                  <p>选填；建议使用 16:9 横图。</p>
                </div>
              </div>
              <el-upload
                :http-request="uploadCover"
                :show-file-list="false"
                :before-upload="beforeCoverUpload"
                class="cover-uploader"
              >
                <div v-if="form.coverUrl" class="cover-preview">
                  <el-image :src="form.coverUrl" fit="cover" />
                  <div class="replace-mask">点击更换封面</div>
                </div>
                <div v-else class="cover-placeholder">
                  <el-icon><Picture /></el-icon>
                  <span>上传封面图片</span>
                </div>
              </el-upload>
              <el-button v-if="form.coverUrl" text type="danger" @click="form.coverUrl = ''">移除封面</el-button>
            </div>

            <div>
              <div class="card-heading compact">
                <div>
                  <h2>文章附件</h2>
                  <p>支持 PDF、Office、文本和压缩包，最多10个。</p>
                </div>
              </div>
              <el-upload
                :http-request="uploadAttachment"
                :show-file-list="false"
                :before-upload="beforeAttachmentUpload"
                multiple
                class="attachment-uploader"
              >
                <el-button type="primary" plain>
                  <el-icon><Paperclip /></el-icon>
                  选择附件
                </el-button>
              </el-upload>
              <div class="uploaded-files" v-if="form.attachments.length">
                <div v-for="(file, index) in form.attachments" :key="file.fileUrl" class="uploaded-file">
                  <div>
                    <strong>{{ file.fileName }}</strong>
                    <span>{{ formatFileSize(file.fileSize) }}</span>
                  </div>
                  <el-button text type="danger" @click="form.attachments.splice(index, 1)">移除</el-button>
                </div>
              </div>
            </div>
          </div>
        </section>

        <footer class="editor-actions">
          <div class="status-tip">
            <el-icon><InfoFilled /></el-icon>
            提交后将进入管理员审核，审核通过后正式发布。
          </div>
          <div>
            <el-button size="large" @click="save(false)" :loading="saving">保存草稿</el-button>
            <el-button size="large" @click="previewVisible = true">预览文章</el-button>
            <el-button type="primary" size="large" @click="save(true)" :loading="submitting">
              提交审核
              <el-icon class="el-icon--right"><Promotion /></el-icon>
            </el-button>
          </div>
        </footer>
      </el-form>
    </div>

    <el-dialog v-model="previewVisible" title="文章预览" width="900px" top="4vh">
      <div class="preview-article">
        <span>{{ categoryName }}</span>
        <h1>{{ form.title || '尚未填写标题' }}</h1>
        <p class="preview-summary">{{ form.summary || '摘要将在保存后自动生成' }}</p>
        <el-image v-if="form.coverUrl" :src="form.coverUrl" fit="cover" class="preview-cover" />
        <div v-if="form.content" class="preview-content" v-html="form.content"></div>
        <el-empty v-else description="尚未填写正文" :image-size="100" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import {
  InfoFilled, Lock, Notebook, Paperclip, Picture, Promotion, View
} from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import request from '@/utils/request'
import { getForumUsername } from '@/utils/forumUser'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const editorRef = shallowRef()
const saving = ref(false)
const submitting = ref(false)
const previewVisible = ref(false)
const categories = ref<any[]>([])
const availableTags = ref<any[]>([])
const username = getForumUsername()
const isEdit = computed(() => Boolean(route.params.id))

const form = reactive({
  id: undefined as number | undefined,
  title: '',
  categoryId: undefined as number | undefined,
  summary: '',
  content: '',
  coverUrl: '',
  visibility: 'MEMBER',
  tags: [] as string[],
  attachments: [] as any[]
})

const rules: FormRules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { min: 2, max: 200, message: '标题长度应为2到200个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择文章分类', trigger: 'change' }
  ],
  content: [
    {
      validator: (_rule: any, value: string, callback: any) => {
        const plain = (value || '').replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim()
        plain ? callback() : callback(new Error('请填写文章正文'))
      },
      trigger: 'change'
    }
  ]
}

const toolbarConfig = {
  excludeKeys: ['group-video', 'insertVideo']
}

const editorConfig: any = {
  placeholder: '从一次阅读、一组实验或一个困惑开始，写下你的思考……',
  autoFocus: false,
  scroll: true,
  MENU_CONF: {
    uploadImage: {
      async customUpload(file: File, insertFn: any) {
        try {
          const uploaded = await uploadForumFile(file)
          insertFn(uploaded.url, file.name, uploaded.url)
        } catch {
          ElMessage.error('正文图片上传失败')
        }
      }
    }
  }
}

const categoryName = computed(() => {
  return categories.value.find(item => item.id === form.categoryId)?.name || '研学分享'
})

const fetchOptions = async () => {
  const [categoryResponse, tagResponse] = await Promise.all([
    request.get('/forum/categories'),
    request.get('/forum/tags')
  ])
  if (categoryResponse.code == 200) {
    categories.value = categoryResponse.data || []
    if (!form.categoryId && categories.value.length) form.categoryId = categories.value[0].id
  }
  if (tagResponse.code == 200) {
    availableTags.value = tagResponse.data || []
  }
}

const fetchPost = async () => {
  if (!route.params.id) return
  const response = await request.get(`/forum/posts/${route.params.id}`, {
    params: { username }
  })
  if (response.code != 200) {
    ElMessage.error(response.msg || '文章加载失败')
    router.push('/community/mine')
    return
  }
  const data = response.data
  Object.assign(form, {
    id: data.id,
    title: data.title || '',
    categoryId: data.categoryId,
    summary: data.summary || '',
    content: data.content || '',
    coverUrl: data.coverUrl || '',
    visibility: data.visibility || 'MEMBER',
    tags: data.tags || [],
    attachments: data.attachments || []
  })
}

const save = async (submit: boolean) => {
  if (!username) {
    ElMessage.warning('请先登录后再发表文章')
    return
  }
  if (submit) {
    try {
      await formRef.value?.validate()
    } catch {
      ElMessage.warning('请检查标题、分类和正文')
      return
    }
  } else if (!form.title.trim()) {
    ElMessage.warning('至少填写文章标题后再保存草稿')
    return
  }
  try {
    submit ? submitting.value = true : saving.value = true
    const response = await request.post('/forum/posts/save', {
      ...form,
      username,
      submit
    })
    if (response.code == 200) {
      form.id = response.data.id
      if (submit) {
        ElMessage.success('文章已提交审核')
        router.push('/community/mine')
      } else {
        ElMessage.success('草稿已保存')
        if (!route.params.id) router.replace(`/community/editor/${response.data.id}`)
      }
    } else {
      ElMessage.error(response.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存文章失败', error)
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
    submitting.value = false
  }
}

const uploadForumFile = async (file: File) => {
  const data = new FormData()
  data.append('file', file)
  const response = await request.post('/upload/forum', data, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
  if (response.code != 200) throw new Error(response.msg || '上传失败')
  return response.data
}

const uploadCover = async (options: any) => {
  try {
    const uploaded = await uploadForumFile(options.file)
    form.coverUrl = uploaded.url
    options.onSuccess(uploaded)
    ElMessage.success('封面上传成功')
  } catch (error) {
    options.onError(error)
    ElMessage.error('封面上传失败')
  }
}

const uploadAttachment = async (options: any) => {
  if (form.attachments.length >= 10) {
    ElMessage.warning('最多上传10个附件')
    options.onError(new Error('附件数量已达上限'))
    return
  }
  try {
    const uploaded = await uploadForumFile(options.file)
    form.attachments.push(uploaded)
    options.onSuccess(uploaded)
    ElMessage.success('附件上传成功')
  } catch (error) {
    options.onError(error)
    ElMessage.error('附件上传失败')
  }
}

const beforeCoverUpload = (file: File) => {
  const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!allowed) {
    ElMessage.error('封面仅支持 JPG、PNG、GIF 或 WebP')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('封面图片不能超过10MB')
    return false
  }
  return true
}

const beforeAttachmentUpload = (file: File) => {
  if (file.size > 80 * 1024 * 1024) {
    ElMessage.error('单个附件不能超过80MB')
    return false
  }
  return true
}

const handleEditorCreated = (editor: any) => {
  editorRef.value = editor
}

const formatFileSize = (bytes?: number) => {
  const value = Number(bytes || 0)
  if (value < 1024) return `${value} B`
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`
  return `${(value / 1024 / 1024).toFixed(1)} MB`
}

onMounted(async () => {
  if (!username) {
    ElMessage.warning('请先登录后再进入文章编辑页')
    router.push('/community')
    return
  }
  await fetchOptions()
  await fetchPost()
})

onBeforeUnmount(() => {
  editorRef.value?.destroy()
})
</script>

<style scoped>
.editor-page {
  min-height: 100vh;
  padding: 34px 20px 70px;
  background:
    linear-gradient(180deg, #edf3fc 0, #f5f7fb 210px, #f5f7fb 100%);
}

.editor-shell {
  max-width: 1080px;
  margin: 0 auto;
}

.editor-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 25px;
}

.eyebrow {
  color: #3864a4;
  font-size: 12px;
  letter-spacing: 2px;
}

.editor-header h1 {
  margin: 8px 0 6px;
  color: #18315e;
  font-size: 30px;
}

.editor-header p {
  margin: 0;
  color: #708099;
}

.article-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-card {
  padding: 27px 30px;
  border: 1px solid #e3eaf4;
  border-radius: 12px;
  background: white;
  box-shadow: 0 7px 24px rgba(32, 58, 105, .05);
}

.primary-card {
  border-top: 3px solid #315fa4;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 22px;
}

.card-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.card-heading.compact {
  margin-bottom: 13px;
}

.card-heading h2 {
  margin: 0;
  color: #263b5f;
  font-size: 19px;
}

.card-heading p {
  margin: 4px 0 0;
  color: #8a97aa;
  font-size: 12px;
}

.content-item {
  margin-bottom: 0;
}

.wang-editor {
  width: 100%;
  overflow: hidden;
  border: 1px solid #dfe6ef;
  border-radius: 8px;
}

.editor-toolbar {
  border-bottom: 1px solid #e7ebf1;
}

.editor-content {
  min-height: 430px;
  overflow-y: auto;
}

.media-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 35px;
}

.cover-uploader {
  display: block;
}

.cover-uploader :deep(.el-upload) {
  display: block;
  width: 100%;
}

.cover-preview,
.cover-placeholder {
  position: relative;
  width: 100%;
  height: 185px;
  overflow: hidden;
  border: 1px dashed #bac8dc;
  border-radius: 9px;
  background: #f7f9fd;
}

.cover-preview :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.replace-mask {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  padding: 8px;
  color: white;
  text-align: center;
  background: rgba(15, 35, 68, .62);
}

.cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #8291a7;
}

.cover-placeholder .el-icon {
  font-size: 34px;
  color: #6d8fbe;
}

.attachment-uploader {
  margin-bottom: 12px;
}

.uploaded-files {
  max-height: 210px;
  overflow-y: auto;
}

.uploaded-file {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-bottom: 1px solid #edf0f5;
}

.uploaded-file strong,
.uploaded-file span {
  display: block;
  max-width: 280px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.uploaded-file strong {
  color: #465671;
  font-size: 13px;
}

.uploaded-file span {
  margin-top: 3px;
  color: #9ba6b7;
  font-size: 11px;
}

.editor-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border: 1px solid #e1e8f2;
  border-radius: 12px;
  background: white;
  box-shadow: 0 7px 24px rgba(32, 58, 105, .06);
}

.status-tip {
  display: flex;
  align-items: center;
  gap: 7px;
  color: #76849a;
  font-size: 13px;
}

.preview-article {
  padding: 10px 25px 35px;
}

.preview-article > span {
  display: inline-block;
  padding: 4px 9px;
  color: #315c9a;
  border-radius: 4px;
  background: #edf4ff;
}

.preview-article h1 {
  margin: 16px 0 10px;
  color: #1b3157;
}

.preview-summary {
  color: #77859a;
  line-height: 1.8;
}

.preview-cover {
  width: 100%;
  height: 320px;
  margin: 15px 0;
  border-radius: 8px;
}

.preview-content {
  color: #364763;
  line-height: 1.9;
}

.preview-content :deep(img) {
  max-width: 100%;
}

@media (max-width: 760px) {
  .editor-page {
    padding: 22px 10px 50px;
  }

  .editor-header,
  .editor-actions {
    align-items: stretch;
    flex-direction: column;
    gap: 15px;
  }

  .form-card {
    padding: 22px 16px;
  }

  .form-row,
  .media-grid {
    grid-template-columns: 1fr;
  }

  .editor-actions > div:last-child {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>
