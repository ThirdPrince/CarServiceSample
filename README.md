# CarServiceSample: 基于 Clean Architecture + Koin 的 Android Automotive 实战

## 🚀 项目简介

`CarServiceSample` 是一个演示如何以现代 Android 开发最佳实践构建 **Android Automotive OS (AAOS)** 应用的项目。它展示了如何通过 `CarPropertyManager` 与车辆硬件（VHAL）进行高性能、可扩展且线程安全的交互。

## 🏗 核心架构：Clean Architecture

项目遵循领域驱动设计，通过三层架构彻底解耦业务逻辑与硬件细节：

-   **Data (数据层)**: 封装 `CarPropertySource` 实现原子化的属性读取，并利用专用的**单线程调度器**实现线程归一化。
-   **Domain (领域层)**: 定义纯 Kotlin 的 `VehicleProperty` 业务模型及 `CarRepository` 接口，隔离硬件 SDK 依赖。
-   **Presentation (表现层)**: 使用 Jetpack Compose 配合 `UIState` 驱动界面，确保 UI 的响应式刷新。

## 🛠 技术亮点

### 1. 线程归一化 (Thread Normalization)
在车载开发中，高频数据（如 RPM、车速）默认在主线程回调。我们通过 Koin 定义了一套后台单线程闭环方案：

```kotlin
// AppModule.kt: 定义专用单线程
single<CoroutineDispatcher> { 
    Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "CarPropertyThread")
    }.asCoroutineDispatcher() 
}

// BaseCarPropertySource.kt: 强制在后台执行订阅和数据转换
override fun observe(): Flow<T?> = callbackFlow {
    // ...
}.flowOn(carDispatcher) 
```

### 2. 原子化数据源 (CarPropertySource)
为了避免 Repository 膨胀，我们将每个 VHAL 属性抽象为独立的 Source，复用订阅、注销及版本兼容逻辑（API 33+）：

```kotlin
class OutsideTemperatureSource(cpm: CarPropertyManager, dispatcher: CoroutineDispatcher) : 
    BaseCarPropertySource<Float>(cpm, VehiclePropertyIds.ENV_OUTSIDE_TEMPERATURE, dispatcher) {
    override fun mapValue(rawValue: Any?): Float? = rawValue as? Float
}
```

### 3. ID 映射与数据格式化
项目实现了精准的 ID 到业务名称的映射，并处理了 VHAL 中常见的**数组乱码**（如 `Integer[]` 导致的内存地址显示问题）以及数值缩放转换。

## 🔧 调试与避坑指南

### VHAL 权限处理
车窗（`WINDOW_POS`）和空调属于特权权限。在模拟器上开发时，必须手动执行授权并强制重启 App：

```bash
adb shell pm grant com.sample.carservicesample android.car.permission.CONTROL_CAR_WINDOWS
adb shell am force-stop com.sample.carservicesample
```

### 静态属性 (Static Properties)
诸如 `INFO_MODEL` 等静态属性不需要 `callback` 监听。在 `CarRepositoryImpl` 中，我们通过 `get()` 同步拉取一次数据，并与动态流合并，节省系统资源。

## 🧪 如何运行

1.  克隆项目并导入 Android Studio。
2.  确保已下载 **Android Automotive** 模拟器镜像。
3.  运行项目，连接成功后在 **Extended Controls -> Car -> VHAL Control** 中调节数值。
4.  观察 App 界面上的实时数据变化及 Logcat 中的线程日志。

## 📦 技术栈

-   **DI**: Koin
-   **Concurrency**: Kotlin Coroutines & Flow
-   **UI**: Jetpack Compose
-   **AAOS**: CarPropertyManager (API 33/34+)

---

希望这个项目能为您的车载开发之路提供一份标准化的参考模板！
