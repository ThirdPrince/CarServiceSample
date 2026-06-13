package com.sample.carservicesample.data.source

import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 车辆属性基础数据源
 * 封装了 CarPropertyManager 的订阅、注销、初始值拉取等样板代码
 *
 * @param carDispatcher 注入的单线程调度器，用于承载所有车载数据逻辑
 */
abstract class BaseCarPropertySource<T>(
    protected val carPropertyManager: CarPropertyManager,
    private val propertyId: Int,
    private val areaId: Int = 0,
    private val carDispatcher: CoroutineDispatcher
) : CarPropertySource<T> {

    private val tag = "CarPropertySource"

    /**
     * 通过反射获取属性 ID 在 VehiclePropertyIds 中对应的常量名，方便调试
     */
    private val propertyName: String by lazy {
        try {
            VehiclePropertyIds::class.java.fields.firstOrNull { field ->
                field.getInt(null) == propertyId
            }?.name ?: "ID($propertyId)"
        } catch (e: Exception) {
            "ID($propertyId)"
        }
    }

    /**
     * 子类实现：将 VHAL 的原始 Any 类型映射为具体的业务类型 T
     */
    protected abstract fun mapValue(rawValue: Any?): T?

    override suspend fun get(): T? = withContext(carDispatcher) {
        runCatching {
            // 使用属性名打印日志
            Log.d(tag, "get() name=$propertyName running on thread: ${Thread.currentThread().name}")
            val property = carPropertyManager.getProperty<Any>(propertyId, areaId)
            mapValue(property.value)
        }.onFailure {
            Log.e(tag, "Get property $propertyName failed: ${it.message}")
        }.getOrNull()
    }

    override fun observe(): Flow<T?> = callbackFlow {
        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                // 收到回调
                Log.d(
                    tag,
                    "onChangeEvent: name=$propertyName, thread=${Thread.currentThread().name}"
                )
                if (value.propertyId == propertyId && value.areaId == areaId) {
                    trySend(mapValue(value.value))
                }
            }

            override fun onErrorEvent(propertyId: Int, areaId: Int) {
                Log.w(tag, "VHAL Error: name=$propertyName, areaId=$areaId")
            }
        }

        try {
            // 订阅逻辑 (兼容 API 33+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                carPropertyManager.subscribePropertyEvents(
                    propertyId,
                    CarPropertyManager.SENSOR_RATE_ONCHANGE,
                    callback
                )
            } else {
                @Suppress("DEPRECATION")
                carPropertyManager.registerCallback(
                    callback,
                    propertyId,
                    CarPropertyManager.SENSOR_RATE_ONCHANGE
                )
            }
        } catch (e: SecurityException) {
            Log.e(tag, "Missing permission for property $propertyName")
            close(e)
        } catch (e: Exception) {
            Log.e(tag, "Subscription failed for property $propertyName: ${e.message}")
            close(e)
        }

        awaitClose {
            // 核心改进：通过 flowOn 强制切换，使 awaitClose 也运行在 CarPropertyThread 线程
            Log.d(
                tag,
                "awaitClose name=$propertyName running on thread: ${Thread.currentThread().name}"
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                carPropertyManager.unsubscribePropertyEvents(callback)
            } else {
                @Suppress("DEPRECATION")
                carPropertyManager.unregisterCallback(callback)
            }
        }
    }
    .onStart { emit(get()) }
    .distinctUntilChanged()
    .flowOn(carDispatcher) // 关键：确保 upstream (callbackFlow 的主体和 awaitClose) 都在后台单线程执行
}
