package com.sample.carservicesample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sample.carservicesample.domain.model.VehicleProperty
import com.sample.carservicesample.presentation.CarUiState
import com.sample.carservicesample.presentation.CarViewModel
import com.sample.carservicesample.ui.theme.CarServiceSampleTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val carViewModel: CarViewModel = koinViewModel()
            // 观察统一的 UI 状态
            val uiState by carViewModel.uiState.collectAsStateWithLifecycle()

            CarServiceSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CarStatusScreen(
                        uiState = uiState,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CarStatusScreen(
    uiState: CarUiState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (uiState.error != null) {
            Text(
                text = "错误: ${uiState.error}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    Text(
                        text = "车载数据监控 (Clean + UIState)",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (uiState.properties.isEmpty()) {
                    item {
                        Text("暂无可访问的车辆属性", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    items(uiState.properties) { property ->
                        PropertyRow(property)
                    }
                }
            }
        }
    }
}

@Composable
fun PropertyRow(property: VehicleProperty) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = "${property.name}: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = property.value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
