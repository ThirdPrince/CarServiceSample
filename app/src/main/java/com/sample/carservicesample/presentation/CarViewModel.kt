package com.sample.carservicesample.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.carservicesample.domain.model.VehicleProperty
import com.sample.carservicesample.domain.usecase.GetVehiclePropertiesUseCase
import kotlinx.coroutines.flow.*

/**
 * 聚合 UI 状态
 */
data class CarUiState(
    val properties: List<VehicleProperty> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class CarViewModel(
    private val getVehiclePropertiesUseCase: GetVehiclePropertiesUseCase
) : ViewModel() {

    /**
     * 恢复标准架构：ViewModel 重新依赖 UseCase。
     * 聚合逻辑（Combine）已下沉回领域层的 UseCase 中。
     */
    val uiState: StateFlow<CarUiState> = getVehiclePropertiesUseCase()
        .map { properties ->
            CarUiState(
                properties = properties,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CarUiState()
        )
}
