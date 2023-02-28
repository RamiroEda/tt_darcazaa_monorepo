package mx.ipn.upiiz.darcazaa.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.ipn.upiiz.darcazaa.utils.toFixedString
import mx.ipn.upiiz.darcazaa.view_models.ChargingStationViewModel

@Composable
fun DroneStats(
    modifier: Modifier = Modifier,
    chargingStationViewModel: ChargingStationViewModel = viewModel()
) {
    return Card(
        modifier = modifier
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = "Estado: ${chargingStationViewModel.systemStatus.value.message}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            BatteryComponent(
                chargingStationViewModel.battery.value
            )
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = "Altitud: ${
                    chargingStationViewModel.position.value?.altitude?.toFixedString(
                        2
                    ) ?: 0
                } m",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "Velocidad: ${
                    chargingStationViewModel.position.value?.speed?.toFixedString(
                        2
                    ) ?: 0.0
                } m/s",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}