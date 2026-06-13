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
    // 1. 定义专门用于车载服务的后台线程
    single(named("CarThread")) {
        HandlerThread("Car-Service-Thread").apply { start() }
    }

    // 2. 创建关联该线程的 Handler
    single(named("CarHandler")) {
        Handler(get<HandlerThread>(named("CarThread")).looper)
    }

    // 3. 在初始化 Car 时，显式传入这个后台 Handler，确保回调运行在独立线程
    single<Car> {
        Car.createCar(
            androidContext(),
            get<Handler>(named("CarHandler"))
        )
    }

    // 4. 获取 CarPropertyManager
    single<CarPropertyManager> {
        get<Car>().getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
    }

    // 5. 让 CoroutineDispatcher 复用上面的 CarHandler 线程
    // 这样所有的协程逻辑和系统回调都在同一个单线程中，实现线程归一化
    single<CoroutineDispatcher> { 
        get<Handler>(named("CarHandler")).asCoroutineDispatcher("CarPropertyThread")
    }

    // 6. 提供数据源 (注入 Manager 和单线程 Dispatcher)
    single<CarPropertySource<Float>>(named("outside_temp")) { OutsideTemperatureSource(get(), get()) }
    single<CarPropertySource<String>>(named("info_model")) { InfoModelSource(get(), get()) }

    // 7. 注入到仓库中
    single<CarRepository> { 
        CarRepositoryImpl(
            outsideTemperatureSource = get(named("outside_temp")),
            infoModelSource = get(named("info_model"))
        ) 
    }
    
    factory { GetVehiclePropertiesUseCase(get()) }
    
    viewModel { CarViewModel(get()) }
}
