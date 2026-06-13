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
 */
abstract class BaseCarPropertySource<T>(
    protected val carPropertyManager: CarPropertyManager,
    private val propertyId: Int,
    private val areaId: Int = 0,
    private val carDispatcher: CoroutineDispatcher
) : CarPropertySource<T> {

    private val tag = "CarPropertySource"

    /**
     * 通过反射获取属性 ID 在 VehiclePropertyIds 中对应的常量名
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
        Log.d(tag, "[$propertyName] 正在获取初始值 (Area: $areaId) 线程: ${Thread.currentThread().name}")
        runCatching {
            val property = carPropertyManager.getProperty<Any>(propertyId, areaId)
            val result = mapValue(property.value)
            Log.d(tag, "[$propertyName] 初始值读取成功: $result")
            result
        }.onFailure {
            Log.e(tag, "[$propertyName] 读取初始值失败: ${it.message}")
        }.getOrNull()
    }

    override fun observe(): Flow<T?> = callbackFlow {
        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                if (value.propertyId == propertyId && value.areaId == areaId) {
                    val rawValue = value.value
                    val mappedValue = mapValue(rawValue)
                    Log.d(tag, "[$propertyName] 收到变化回调: 原始值=$rawValue -> 映射值=$mappedValue (线程: ${Thread.currentThread().name})")
                    trySend(mappedValue)
                }
            }

            override fun onErrorEvent(propertyId: Int, areaId: Int) {
                Log.w(tag, "[$propertyName] VHAL 报错 (Area: $areaId)")
            }
        }

        try {
            Log.d(tag, "[$propertyName] 正在订阅属性变更...")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                carPropertyManager.subscribePropertyEvents(propertyId, CarPropertyManager.SENSOR_RATE_ONCHANGE, callback)
            } else {
                @Suppress("DEPRECATION")
                carPropertyManager.registerCallback(callback, propertyId, CarPropertyManager.SENSOR_RATE_ONCHANGE)
            }
        } catch (e: SecurityException) {
            Log.e(tag, "[$propertyName] 订阅失败：权限不足")
            close(e)
        } catch (e: Exception) {
            Log.e(tag, "[$propertyName] 订阅异常: ${e.message}")
            close(e)
        }

        awaitClose {
            Log.i(tag, "[$propertyName] Flow 关闭，注销监听器 (线程: ${Thread.currentThread().name})")
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
    .flowOn(carDispatcher)
}
