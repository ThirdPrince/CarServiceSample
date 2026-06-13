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
    getVehiclePropertiesUseCase: GetVehiclePropertiesUseCase,
) : ViewModel() {

    /**
     * 使用 stateIn 将业务流转换为 UI 状态流。
     * 这种方式比在 init 块中使用 viewModelScope.launch 更简洁且响应式。
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
