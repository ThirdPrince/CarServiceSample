package com.sample.carservicesample.data.repository

import com.sample.carservicesample.data.source.CarPropertySource
import com.sample.carservicesample.domain.repository.CarRepository
import kotlinx.coroutines.flow.*

class CarRepositoryImpl(
    private val outsideTemperatureSource: CarPropertySource<Float>,
    private val infoModelSource: CarPropertySource<String>
) : CarRepository {

    override fun observeOutsideTemperature(): Flow<Float?> {
        return outsideTemperatureSource.observe()
    }

    override fun observeInfoModel(): Flow<String?> = flow {
        // 静态属性，获取一次初始值即可
        emit(infoModelSource.get())
    }
}
