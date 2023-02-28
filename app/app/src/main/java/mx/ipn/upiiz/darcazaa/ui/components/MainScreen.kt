package mx.ipn.upiiz.darcazaa.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.ipn.upiiz.darcazaa.data.models.SocketProvider
import mx.ipn.upiiz.darcazaa.data.models.UserPreferences
import mx.ipn.upiiz.darcazaa.ui.screens.AddRoutineActivity
import mx.ipn.upiiz.darcazaa.view_models.ChargingStationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    preferences: UserPreferences,
    socketProvider: SocketProvider,
    droneViewModel: ChargingStationViewModel = viewModel()
) {
    val context = LocalContext.current
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "Nueva rutina") },
                icon = {
                   Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                },
                onClick = {
                    context.startActivity(Intent(context, AddRoutineActivity::class.java))
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            AlertBox(
                preferences,
                socketProvider
            )
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .padding(bottom = 92.dp)
                    .padding(horizontal = 32.dp)
            ) {
                Text(
                    modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
                    text = "Mis rutinas",
                    style = MaterialTheme.typography.headlineLarge
                )
                DroneInfoCard()
                droneViewModel.routines.forEach {
                    RoutineItem(routineWithWaypoints = it)
                }
                if (droneViewModel.routines.isEmpty()){
                    Box(
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Sin rutinas")
                    }
                }
            }
        }
    }
}