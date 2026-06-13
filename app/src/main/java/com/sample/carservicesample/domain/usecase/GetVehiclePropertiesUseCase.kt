package com.sample.carservicesample.domain.usecase

import com.sample.carservicesample.domain.model.VehicleProperty
import com.sample.carservicesample.domain.repository.CarRepository
import kotlinx.coroutines.flow.Flow

/**
 * 获取所有可见车辆属性及其名称映射的用例
 */
class GetVehiclePropertiesUseCase(private val repository: CarRepository) {
    operator fun invoke(): Flow<List<VehicleProperty>> {
        return repository.getVehicleProperties()
    }
}
