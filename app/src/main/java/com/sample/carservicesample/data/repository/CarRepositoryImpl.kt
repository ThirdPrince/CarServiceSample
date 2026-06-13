package com.sample.carservicesample.data.repository

import android.car.VehiclePropertyIds
import android.util.Log
import com.sample.carservicesample.data.source.CarPropertySource
import com.sample.carservicesample.domain.model.VehicleProperty
import com.sample.carservicesample.domain.repository.CarRepository
import kotlinx.coroutines.flow.*

class CarRepositoryImpl(
    private val outsideTemperatureSource: CarPropertySource<Float>,
    private val infoModelSource: CarPropertySource<String>
) : CarRepository {

    private val tag = "CarRepository"

    override fun observeOutsideTemperature(): Flow<Float?> {
        return outsideTemperatureSource.observe()
    }

    override fun observeInfoModel(): Flow<String?> = flow {
        emit(infoModelSource.get())
    }

    override fun getVehicleProperties(): Flow<List<VehicleProperty>> {
        return combine(
            observeOutsideTemperature(),
            observeInfoModel()
        ) { temp, model ->
            Log.d(tag, "getVehicleProperties: 合并数据更新 - Model: $model, Temp: $temp")
            listOf(
                VehicleProperty(
                    "车辆型号",
                    model ?: "未知",
                    VehiclePropertyIds.INFO_MODEL
                ),
                VehicleProperty(
                    "室外温度",
                    "${temp ?: 0f} °C",
                    VehiclePropertyIds.ENV_OUTSIDE_TEMPERATURE
                )
            )
        }
    }
}
