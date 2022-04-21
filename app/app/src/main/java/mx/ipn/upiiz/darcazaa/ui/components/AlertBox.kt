package mx.ipn.upiiz.darcazaa.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.ipn.upiiz.darcazaa.data.models.SyncingStatus
import mx.ipn.upiiz.darcazaa.view_models.ConnectionViewModel
import mx.ipn.upiiz.darcazaa.view_models.ChargingStationViewModel

@Composable
fun AlertBox(
    connectionViewModel: ConnectionViewModel = viewModel(),
    droneViewModel: ChargingStationViewModel = viewModel()
) {
    val isConnected = connectionViewModel.isConnected.value
    val isNotSynced = droneViewModel.routines.any {
        !it.routine.isSynced
    }
    val isSyncing = connectionViewModel.syncingStatus.value == SyncingStatus.SYNCING
    val hasSyncError = connectionViewModel.syncingStatus.value == SyncingStatus.ERROR

    AnimatedVisibility(
        visible = !isConnected || isNotSynced || isSyncing || hasSyncError,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (!isConnected) {
                        Color(0xFFdc3545)
                    } else if (hasSyncError) {
                        Color(0xFFdc3545)
                    } else if (isSyncing) {
                        Color(0xFF2196F3)
                    } else if (isNotSynced) {
                        Color(0xFFFF9800)
                    } else MaterialTheme.colorScheme.background
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (!isConnected) {
                    "No está conectado a la estación de carga"
                }else if(hasSyncError){
                    "Error al sincronizar"
                } else if(isSyncing){
                    "Sincronizando..."
                } else if(isNotSynced){
                    "Conectar para sincronizar"
                }else  "",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}