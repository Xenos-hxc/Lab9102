<template>
  <div class="equipment-page">
    <div class="page-shell">
      <aside class="category-sidebar">
        <div class="sidebar-title">
          <el-icon><Cpu /></el-icon>
          <div><strong>团队设备</strong><span>EQUIPMENT</span></div>
        </div>
        <el-menu :default-active="activeCategory" class="category-menu" @select="selectCategory">
          <el-menu-item index="all"><el-icon><List /></el-icon><span>全部设备</span></el-menu-item>
          <el-menu-item v-for="category in sectionCategories" :key="category.key" :index="category.key">
            <el-icon><Cpu /></el-icon><span>{{ category.name }}</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <main ref="allAnchor" class="equipment-content">
        <div v-if="loading" class="equipment-grid">
          <el-skeleton v-for="index in 3" :key="index" animated class="equipment-skeleton" />
        </div>
        <div v-else-if="sectionCategories.length" class="equipment-stream">
          <section
            v-for="category in sectionCategories"
            :key="category.key"
            :ref="element => setCategorySection(element, category.key)"
            class="category-section"
            :data-category="category.key"
          >
            <header class="category-heading">
              <div><span class="heading-accent"></span><h2>{{ category.name }}</h2></div>
              <span>{{ equipmentByCategory(category).length }} 台</span>
            </header>
            <div v-if="equipmentByCategory(category).length" class="equipment-grid">
              <article v-for="item in equipmentByCategory(category)" :key="item.id" class="equipment-card">
                <div class="equipment-image-wrap">
                  <img v-if="item.pictureUrl" :src="fileUrl(item.pictureUrl)" :alt="item.name" class="equipment-image" />
                  <div v-else class="equipment-placeholder"><el-icon><Cpu /></el-icon></div>
                  <span class="date-badge">{{ formatDate(item.purchaseDate) }} 引入</span>
                </div>
                <div class="equipment-body">
                  <span class="category-badge">{{ item.categoryName || '未分类' }}</span>
                  <h3>{{ item.name }}</h3>
                  <p>{{ item.introduction || '暂无设备简介' }}</p>
                </div>
              </article>
            </div>
            <el-empty v-else :description="`${category.name}分类下暂无设备`" :image-size="95" class="category-empty" />
          </section>
        </div>
        <el-empty v-else description="暂无设备分类和设备记录" :image-size="120" />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { Cpu, List } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getApiBaseUrl } from '@/utils/apiConfig'

interface EquipmentCategory { id?: number; name: string; description?: string; sortOrder?: number }
interface Equipment {
  id: number
  name: string
  categoryId?: number
  categoryName?: string
  purchaseDate: string
  introduction: string
  pictureUrl: string
}
interface CategorySection extends EquipmentCategory { key: string }

const equipment = ref<Equipment[]>([])
const categories = ref<EquipmentCategory[]>([])
const loading = ref(false)
const activeCategory = ref('all')
const allAnchor = ref<HTMLElement | null>(null)
const categorySections = new Map<string, HTMLElement>()
let scrollFrame = 0

const sectionCategories = computed<CategorySection[]>(() => {
  const result = categories.value.map(category => ({ ...category, key: String(category.id) }))
  if (equipment.value.some(item => !item.categoryId || !item.categoryName)) {
    result.push({ name: '未分类', key: 'unclassified', sortOrder: Number.MAX_SAFE_INTEGER })
  }
  return result
})

const equipmentByCategory = (category: CategorySection) => category.key === 'unclassified'
  ? equipment.value.filter(item => !item.categoryId || !item.categoryName)
  : equipment.value.filter(item => String(item.categoryId) === category.key)

const setCategorySection = (element: unknown, key: string) => {
  if (element instanceof HTMLElement) categorySections.set(key, element)
  else categorySections.delete(key)
}

const updateActiveCategory = () => {
  scrollFrame = 0
  const firstCategory = sectionCategories.value[0]
  const firstSection = firstCategory && categorySections.get(firstCategory.key)
  if (firstSection && firstSection.getBoundingClientRect().top > 120) {
    activeCategory.value = 'all'
    return
  }
  const visibleCategory = sectionCategories.value.find(category =>
    (categorySections.get(category.key)?.getBoundingClientRect().bottom || 0) > 160
  )
  if (visibleCategory) activeCategory.value = visibleCategory.key
}

const handleScroll = () => {
  if (!scrollFrame) scrollFrame = window.requestAnimationFrame(updateActiveCategory)
}

const selectCategory = async (key: string) => {
  activeCategory.value = key
  await nextTick()
  if (key === 'all') {
    document.querySelector('.equipment-page')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  } else {
    categorySections.get(key)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

const formatDate = (value: string) => value ? `${value.slice(0, 4)}年${value.slice(5, 7)}月` : '未知时间'
const fileUrl = (value: string) => {
  if (!value) return ''
  if (/^https?:\/\//i.test(value)) return value
  return `${getApiBaseUrl()}${value.startsWith('/') ? '' : '/'}${value}`
}

const loadEquipment = async () => {
  loading.value = true
  try {
    const [equipmentResponse, categoryResponse] = await Promise.all([
      request.get('/equipment/list'),
      request.get('/equipment/categories')
    ])
    if (equipmentResponse.code == 200) equipment.value = equipmentResponse.data || []
    if (categoryResponse.code == 200) categories.value = categoryResponse.data || []
    await nextTick()
    updateActiveCategory()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  loadEquipment()
})
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  if (scrollFrame) window.cancelAnimationFrame(scrollFrame)
})
</script>

<style scoped>
.equipment-page { min-height: 70vh; padding: 28px 0 60px; background: linear-gradient(180deg, #f7faff 0%, #fff 45%); }
.page-shell { display: grid; grid-template-columns: 220px minmax(0, 1fr); gap: 28px; width: min(92%, 1600px); margin: 0 auto; }
.category-sidebar { position: sticky; top: 18px; align-self: start; overflow: hidden; background: #fff; border: 1px solid #e6edf8; border-radius: 18px; box-shadow: 0 14px 35px rgba(30, 58, 138, .08); }
.sidebar-title { display: flex; align-items: center; gap: 12px; padding: 22px 20px; color: #fff; background: linear-gradient(135deg, #173a89, #2866c7); }
.sidebar-title > .el-icon { font-size: 30px; }
.sidebar-title strong, .sidebar-title span { display: block; }
.sidebar-title span { margin-top: 2px; font-size: 10px; opacity: .75; letter-spacing: 1.5px; }
.category-menu { max-height: calc(100vh - 120px); overflow-y: auto; padding: 8px; border-right: 0; }
.category-menu :deep(.el-menu-item) { height: 48px; margin: 3px 0; border-radius: 10px; }
.category-menu :deep(.el-menu-item.is-active) { color: #1e50a2; background: #eaf2ff; font-weight: 700; }
.equipment-content { min-width: 0; }
.equipment-stream { display: flex; flex-direction: column; gap: 36px; }
.category-section { scroll-margin-top: 20px; }
.category-heading { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; padding: 17px 20px; color: #1c3154; border: 1px solid #dfe8f5; border-radius: 12px; background: #fff; box-shadow: 0 6px 18px rgba(44, 62, 80, .06); }
.category-heading > div { display: flex; align-items: center; gap: 13px; }
.heading-accent { width: 5px; height: 31px; border-radius: 8px; background: linear-gradient(180deg, #2b63b8, #67a2ff); }
.category-heading h2 { margin: 0; font-size: 25px; }
.category-heading > span { color: #73839a; }
.equipment-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 22px; }
.equipment-card { overflow: hidden; background: #fff; border: 1px solid #e4ebf5; border-radius: 18px; box-shadow: 0 10px 28px rgba(27, 55, 99, .08); transition: .25s ease; }
.equipment-card:hover { transform: translateY(-6px); box-shadow: 0 18px 38px rgba(27, 55, 99, .14); }
.equipment-image-wrap { position: relative; height: 245px; overflow: hidden; background: #e9f0fa; }
.equipment-image { width: 100%; height: 100%; object-fit: cover; transition: transform .45s ease; }
.equipment-card:hover .equipment-image { transform: scale(1.035); }
.equipment-placeholder { display: grid; place-items: center; height: 100%; color: #6b86ad; font-size: 64px; }
.date-badge { position: absolute; bottom: 14px; left: 16px; padding: 7px 12px; color: #fff; border-radius: 999px; background: rgba(12, 34, 72, .78); backdrop-filter: blur(8px); font-size: 12px; }
.equipment-body { padding: 18px 21px 24px; }
.category-badge { display: inline-flex; padding: 4px 10px; margin-bottom: 10px; color: #2459a7; border-radius: 999px; background: #eaf2ff; font-size: 12px; font-weight: 600; }
.equipment-body h3 { margin: 0 0 11px; color: #1c2f50; font-size: 19px; }
.equipment-body p { display: -webkit-box; overflow: hidden; margin: 0; color: #63738b; line-height: 1.8; -webkit-line-clamp: 4; -webkit-box-orient: vertical; }
.equipment-skeleton { padding: 18px; background: #fff; border-radius: 16px; }
.category-empty { padding: 28px 0; }
@media (max-width: 1150px) { .equipment-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 760px) {
  .page-shell { grid-template-columns: 1fr; width: min(94%, 680px); }
  .category-sidebar { position: static; }
  .category-menu { display: flex; overflow-x: auto; }
  .category-menu :deep(.el-menu-item) { flex: 0 0 auto; }
  .equipment-grid { grid-template-columns: 1fr; }
}
</style>
