// API配置工具
export const getApiBaseUrl = () => {
  // 在开发环境中使用代理，在生产环境中使用环境变量
  if (import.meta.env.DEV) {
    return '/api' // 开发环境使用代理
  } else {
    return import.meta.env.VITE_API_BASE_URL || '/api' // 生产环境使用环境变量
  }
}

// 获取完整的API URL
export const getApiUrl = (endpoint: string) => {
  const baseUrl = getApiBaseUrl()
  // 如果baseUrl以/api结尾，避免重复的/api
  if (baseUrl.endsWith('/api') && endpoint.startsWith('/api')) {
    return baseUrl + endpoint.substring(4)
  }
  return baseUrl + endpoint
}
