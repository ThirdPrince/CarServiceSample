package com.sample.carservicesample.domain.usecase

import com.sample.carservicesample.domain.model.VehicleProperty
import com.sample.carservicesample.domain.repository.CarRepository
import kotlinx.coroutines.flow.Flow

/**
 * 既然我们要获取带名称的属性列表，我们将此用例更新为获取 [VehicleProperty]。
 * 它将调用仓库中重构后的 getVehicleProperties 方法。
 */
class GetWindowStatesUseCase(private val repository: CarRepository) {
    operator fun invoke(): Flow<List<VehicleProperty>> {
        return repository.getVehicleProperties()
    }
}
