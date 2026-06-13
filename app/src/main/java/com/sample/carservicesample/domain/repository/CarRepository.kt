package com.sample.carservicesample.domain.repository

import com.sample.carservicesample.domain.model.VehicleProperty
import kotlinx.coroutines.flow.Flow

interface CarRepository {
    /**
     * 获取聚合后的车辆属性列表流（用于全量展示）
     */
    fun getVehicleProperties(): Flow<List<VehicleProperty>>

    /**
     * 观察室外温度 (Float)
     */
    fun observeOutsideTemperature(): Flow<Float?>

    /**
     * 观察车辆型号 (String)
     */
    fun observeInfoModel(): Flow<String?>
}
