package com.sample.carservicesample.domain.model

/**
 * 车窗状态模型
 * @param areaId 车窗区域 ID (例如左前、右前等)
 * @param position 车窗位置 (0-100, 0 为全闭)
 */
data class WindowState(
    val areaId: Int,
    val position: Int
)
