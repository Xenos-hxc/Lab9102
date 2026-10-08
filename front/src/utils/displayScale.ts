import request from './request'

interface ResolutionSetting {
  width: number
  height: number
  scale: number
  enabled: boolean
}

export const DISPLAY_SCALE_EVENT = 'lab-display-scale-changed'

const applyScale = (scaleValue: number) => {
  const scale = Number.isFinite(scaleValue) ? Math.min(2, Math.max(0.5, scaleValue)) : 1
  const body = document.body
  body.style.zoom = scale === 1 ? '' : String(scale)
  // Chromium 会根据 CSS zoom 自动调整布局视口；反向补偿 body 宽高会导致
  // 页面被重复缩放，表现为右侧留白或横向滚动条。
  body.style.width = ''
  body.style.minHeight = ''
  body.dataset.displayScale = scale.toFixed(2)
  window.dispatchEvent(new CustomEvent(DISPLAY_SCALE_EVENT, { detail: scale }))
}

export const refreshDisplayScale = async () => {
  try {
    const response = await request.get('/resolution-setting/list')
    const rules: ResolutionSetting[] = response.code == 200 ? (response.data || []) : []
    const screenWidth = Math.round(window.screen.width)
    const screenHeight = Math.round(window.screen.height)
    const matched = rules.find(rule => rule.enabled !== false && (
      (rule.width === screenWidth && rule.height === screenHeight)
      || (rule.height === screenWidth && rule.width === screenHeight)
    ))
    applyScale(matched ? Number(matched.scale) : 1)
    return matched || null
  } catch (error) {
    applyScale(1)
    console.warn('分辨率缩放规则加载失败，已使用系统默认大小', error)
    return null
  }
}

export const resetDisplayScale = () => applyScale(1)
