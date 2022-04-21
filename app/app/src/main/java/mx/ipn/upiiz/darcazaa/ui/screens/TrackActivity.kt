package mx.ipn.upiiz.darcazaa.ui.screens

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FlightLand
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.addPolygon
import dagger.hilt.android.AndroidEntryPoint
import mx.ipn.upiiz.darcazaa.R
import mx.ipn.upiiz.darcazaa.data.models.SystemStatus
import mx.ipn.upiiz.darcazaa.ui.components.BatteryComponent
import mx.ipn.upiiz.darcazaa.ui.components.MapView
import mx.ipn.upiiz.darcazaa.ui.theme.DARCAZAATheme
import mx.ipn.upiiz.darcazaa.utils.drawDrone
import mx.ipn.upiiz.darcazaa.utils.drawRoutineArea
import mx.ipn.upiiz.darcazaa.utils.toFixedString
import mx.ipn.upiiz.darcazaa.view_models.ChargingStationViewModel

@AndroidEntryPoint
class TrackActivity : AppCompatActivity() {
    private val chargingStationViewModel: ChargingStationViewModel by viewModels()

    companion object {
        const val CAMERA_ANGLE = 40f
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var canUpdateCamera = true

        setContent {
            DARCAZAATheme {
                val droneStatus = chargingStationViewModel.systemStatus.value
                Scaffold(
                    floatingActionButtonPosition = FabPosition.Center,
                    floatingActionButton = {
                        if (droneStatus == SystemStatus.FLYING) {
                            ExtendedFloatingActionButton(
                                text = {
                                    Text(text = "Cancelar")
                                },
                                icon = {
                                       Icon(
                                           imageVector = Icons.Rounded.FlightLand,
                                           contentDescription = null
                                       )
                                },
                                onClick = {
                                    chargingStationViewModel.cancelRoutine()
                                },
                                contentColor = MaterialTheme.colorScheme.error,
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        }
                    }
                ) {
                    val dronePosition = chargingStationViewModel.position.value
                    val currentRoutine = chargingStationViewModel.currentRoutine.value
                    val colorScheme = MaterialTheme.colorScheme
                    val currentTilt = if (arrayOf(
                            SystemStatus.FLYING,
                            SystemStatus.LANDING,
                            SystemStatus.STARTING,
                            SystemStatus.CANCELING
                        ).contains(
                            chargingStationViewModel.systemStatus.value
                        )
                    ) {
                        CAMERA_ANGLE
                    } else {
                        0f
                    }

                    val currentZoom = if (arrayOf(
                            SystemStatus.FLYING,
                            SystemStatus.LANDING,
                            SystemStatus.STARTING,
                            SystemStatus.CANCELING
                        ).contains(
                            chargingStationViewModel.systemStatus.value
                        )
                    ) {
                        16f
                    } else {
                        18f
                    }

                    Box {
                        MapView(
                            modifier = Modifier.fillMaxSize(),
                            mapFinish = {
                                it.mapType = GoogleMap.MAP_TYPE_SATELLITE
                                it.isBuildingsEnabled = true
                                it.uiSettings.isScrollGesturesEnabled = false
                                it.uiSettings.isZoomGesturesEnabled = false
                                it.uiSettings.isRotateGesturesEnabled = false
                                it.uiSettings.isTiltGesturesEnabled = false
                                it.uiSettings.isCompassEnabled = false

                                it.setOnCameraIdleListener { canUpdateCamera = true }
                            }
                        ) {
                            dronePosition?.let { latLng ->
                                val position = LatLng(latLng.latitude, latLng.longitude)
                                it.drawDrone(latLng)
                                if (canUpdateCamera) {
                                    it.moveCamera(
                                        CameraUpdateFactory.newCameraPosition(
                                            CameraPosition.builder().target(position).tilt(
                                                currentTilt
                                            ).zoom(currentZoom).build()
                                        )
                                    )
                                }
                            }
                            currentRoutine?.let { routine ->
                                it.drawRoutineArea(routine.routine, colorScheme)
                            }
                        }
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
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
                }
            }
        }
    }
}