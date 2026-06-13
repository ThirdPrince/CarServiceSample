package com.sample.carservicesample.di

import android.car.Car
import android.car.hardware.property.CarPropertyManager
import android.os.Handler
import android.os.HandlerThread
import com.sample.carservicesample.data.repository.CarRepositoryImpl
import com.sample.carservicesample.data.source.*
import com.sample.carservicesample.domain.repository.CarRepository
import com.sample.carservicesample.domain.usecase.GetVehiclePropertiesUseCase
import com.sample.carservicesample.presentation.CarViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.android.asCoroutineDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    // 1. 专门用于车载服务的后台线程与 Handler
    single(named("CarThread")) {
        HandlerThread("Car-Service-Thread").apply { start() }
    }
    single(named("CarHandler")) {
        Handler(get<HandlerThread>(named("CarThread")).looper)
    }

    // 2. 初始化 Car 实例
    single<Car> {
        Car.createCar(androidContext(), get<Handler>(named("CarHandler")))
    }

    // 3. 提供 CarPropertyManager
    single<CarPropertyManager> {
        get<Car>().getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
    }

    // 4. 让 CoroutineDispatcher 复用上面的后台线程，实现线程归一化
    single<CoroutineDispatcher> { 
        get<Handler>(named("CarHandler")).asCoroutineDispatcher("CarPropertyThread")
    }

    // 5. 提供具体数据源 (注入 Manager 和单线程 Dispatcher)
    single<CarPropertySource<Float>>(named("outside_temp")) { OutsideTemperatureSource(get(), get()) }
    single<CarPropertySource<String>>(named("info_model")) { InfoModelSource(get(), get()) }

    // 6. 注入到仓库中
    single<CarRepository> { 
        CarRepositoryImpl(
            outsideTemperatureSource = get(named("outside_temp")),
            infoModelSource = get(named("info_model"))
        ) 
    }
    
    // 7. 业务逻辑层：重新引入 UseCase
    factory { GetVehiclePropertiesUseCase(get()) }
    
    // 8. 表现层：ViewModel 现在依赖 UseCase 而非 Repository
    viewModel { CarViewModel(get()) }
}
