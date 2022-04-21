package mx.ipn.upiiz.darcazaa.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.ipn.upiiz.darcazaa.ui.screens.AddRoutineActivity
import mx.ipn.upiiz.darcazaa.view_models.ChargingStationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
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
        Column {
            AlertBox()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
            ) {
                Text(
                    modifier = Modifier.padding(top = 32.dp),
                    text = "Mis rutinas",
                    style = MaterialTheme.typography.headlineLarge
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        top = 24.dp,
                        bottom = 80.dp
                    )
                ){
                    item {
                        DroneInfoCard()
                    }
                    items(droneViewModel.routines){
                        RoutineItem(routineWithWaypoints = it)
                    }
                    if (droneViewModel.routines.isEmpty()){
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Sin rutinas")
                            }
                        }
                    }
                }
            }
        }
    }
}