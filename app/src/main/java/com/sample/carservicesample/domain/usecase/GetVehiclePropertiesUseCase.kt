package com.sample.carservicesample.domain.usecase

import android.car.VehiclePropertyIds
import com.sample.carservicesample.domain.model.VehicleProperty
import com.sample.carservicesample.domain.repository.CarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * 核心用例：负责聚合多个原子数据流并转换为 UI 需要的 VehicleProperty 列表。
 * 它是 Clean Architecture 中的业务逻辑核心。
 */
class GetVehiclePropertiesUseCase(private val repository: CarRepository) {
    operator fun invoke(): Flow<List<VehicleProperty>> {
        return combine(
            repository.observeOutsideTemperature(),
            repository.observeInfoModel()
        ) { temp, model ->
            listOf(
                VehicleProperty(
                    name = "车辆型号",
                    value = model ?: "未知",
                    propertyId = VehiclePropertyIds.INFO_MODEL
                ),
                VehicleProperty(
                    name = "室外温度",
                    value = "${temp ?: 0f} °C",
                    propertyId = VehiclePropertyIds.ENV_OUTSIDE_TEMPERATURE
                )
            )
        }
    }
}
