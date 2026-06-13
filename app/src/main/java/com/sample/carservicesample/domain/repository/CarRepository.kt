package com.sample.carservicesample.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * 车辆数据仓库接口
 * 遵循原子化设计原则，仅提供基础属性的观察流。
 */
interface CarRepository {
    /**
     * 观察室外温度 (Float)
     */
    fun observeOutsideTemperature(): Flow<Float?>

    /**
     * 获取车辆型号 (String)
     */
    fun observeInfoModel(): Flow<String?>
}
