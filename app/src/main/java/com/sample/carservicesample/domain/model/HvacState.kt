package com.sample.carservicesample.domain.model

/**
 * 空调风量状态
 * @param areaId 区域 ID (例如驾驶员侧、副驾驶侧)
 * @param fanSpeed 风量档位
 */
data class HvacState(
    val areaId: Int,
    val fanSpeed: Int
)
