package com.sample.carservicesample.domain.model

/**
 * 通用车辆属性模型
 * @param name 属性中文名 (如: 当前档位)
 * @param value 格式化后的值 (如: "D档" 或 "已拉起")
 * @param propertyId 原始 ID
 */
data class VehicleProperty(
    val name: String,
    val value: String,
    val propertyId: Int
)
