package com.sample.carservicesample.data.source

import android.car.VehiclePropertyIds
import android.car.hardware.property.CarPropertyManager
import kotlinx.coroutines.CoroutineDispatcher

/**
 * 室外温度数据源实现
 */
class OutsideTemperatureSource(
    carPropertyManager: CarPropertyManager,
    carDispatcher: CoroutineDispatcher
) : BaseCarPropertySource<Float>(
    carPropertyManager = carPropertyManager,
    propertyId = VehiclePropertyIds.ENV_OUTSIDE_TEMPERATURE,
    areaId = 0,
    carDispatcher = carDispatcher
) {
    override fun mapValue(rawValue: Any?): Float? {
        return rawValue as? Float
    }
}
