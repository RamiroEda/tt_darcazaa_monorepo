package mx.ipn.upiiz.darcazaa.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.ipn.upiiz.darcazaa.data.models.PreferenceKeys
import mx.ipn.upiiz.darcazaa.data.models.SocketProvider
import mx.ipn.upiiz.darcazaa.data.models.SyncingStatus
import mx.ipn.upiiz.darcazaa.data.models.UserPreferences
import mx.ipn.upiiz.darcazaa.view_models.ChargingStationViewModel
import mx.ipn.upiiz.darcazaa.view_models.ConnectionViewModel

@Composable
fun AlertBox(
    preferences: UserPreferences,
    socketProvider: SocketProvider,
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
        Column(
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
            horizontalAlignment = Alignment.CenterHorizontally
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
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            if (!isConnected){
                OutlinedButton(
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = {
                        preferences.remove(PreferenceKeys.Url)
                        socketProvider.socket.disconnect()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color.White,
                    )
                ) {
                    Text(text = "DESCONECTAR")
                }
            }
        }
    }
}