package mx.ipn.upiiz.darcazaa.ui.components

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.ipn.upiiz.darcazaa.R
import mx.ipn.upiiz.darcazaa.ui.screens.DriverActivity
import mx.ipn.upiiz.darcazaa.ui.screens.TrackActivity
import mx.ipn.upiiz.darcazaa.view_models.ChargingStationViewModel

@Composable
fun DroneInfoCard(
    droneViewModel: ChargingStationViewModel = viewModel()
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.padding(bottom = 16.dp),
        backgroundColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.height(100.dp),
                painter = painterResource(id = R.drawable.drone),
                contentDescription = "Drone"
            )
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = "Estado: ${droneViewModel.systemStatus.value.message}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            BatteryComponent(
                droneViewModel.battery.value
            )
            Row(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Button(
                    onClick = {
                        context.startActivity(Intent(context, TrackActivity::class.java))
                    },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GpsFixed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = "Rastrear",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Button(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = {
                        context.startActivity(Intent(context, DriverActivity::class.java))
                    },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Flight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = "Conducir",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}