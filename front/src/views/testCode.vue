<template>
  <div>
    <h2>测试页面</h2>
    <el-button @click="fetchData">测试按钮</el-button>
  </div>
</template>
<script setup lang="ts">
// 在Vue组件中
import request from '@/utils/request'
import { getApiBaseUrl } from '@/utils/apiConfig'

// 使用示例
const fetchData = async () => {
  try {
    const response = await request.get('/carousel/list')
    console.log('API基础URL:', getApiBaseUrl())
    console.log('获取到的数据:', response)
    return response
  } catch (error) {
    console.error('请求失败:', error)
  }
}
</script>

<style scoped>

</style>
