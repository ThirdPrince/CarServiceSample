package com.sample.carservicesample.data.source

import android.car.VehiclePropertyIds
import android.car.hardware.property.CarPropertyManager
import kotlinx.coroutines.CoroutineDispatcher

/**
 * 车辆型号数据源实现
 */
class InfoModelSource(
    carPropertyManager: CarPropertyManager,
    carDispatcher: CoroutineDispatcher
) : BaseCarPropertySource<String>(
    carPropertyManager = carPropertyManager,
    propertyId = VehiclePropertyIds.INFO_MODEL,
    carDispatcher = carDispatcher
) {
    override fun mapValue(rawValue: Any?): String? {
        return rawValue?.toString()
    }
}
