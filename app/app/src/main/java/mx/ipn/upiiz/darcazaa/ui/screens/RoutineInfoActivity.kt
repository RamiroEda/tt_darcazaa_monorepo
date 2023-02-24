package mx.ipn.upiiz.darcazaa.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FlightLand
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.*
import dagger.hilt.android.AndroidEntryPoint
import mx.ipn.upiiz.darcazaa.R
import mx.ipn.upiiz.darcazaa.data.models.RoutineWithWaypoints
import mx.ipn.upiiz.darcazaa.data.models.SystemStatus
import mx.ipn.upiiz.darcazaa.data.models.viewModelFactory
import mx.ipn.upiiz.darcazaa.data.repositories.HistoryRepository
import mx.ipn.upiiz.darcazaa.ui.components.BatteryComponent
import mx.ipn.upiiz.darcazaa.ui.components.HistoryItem
import mx.ipn.upiiz.darcazaa.ui.components.ValueDisplay
import mx.ipn.upiiz.darcazaa.ui.theme.DARCAZAATheme
import mx.ipn.upiiz.darcazaa.ui.theme.LightThemeColors
import mx.ipn.upiiz.darcazaa.utils.*
import mx.ipn.upiiz.darcazaa.view_models.ChargingStationViewModel
import mx.ipn.upiiz.darcazaa.view_models.HistoryViewModel
import javax.inject.Inject
import kotlin.math.ceil

@AndroidEntryPoint
class RoutineInfoActivity : AppCompatActivity() {
    private val chargingStationViewModel: ChargingStationViewModel by viewModels()
    private lateinit var historyViewModel: HistoryViewModel

    @Inject
    lateinit var historyRepository: HistoryRepository

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val routineWithWaypoints = intent.getParcelableExtra<RoutineWithWaypoints>("routine")

        if (routineWithWaypoints == null) {
            finish()
            return
        }

        historyViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                HistoryViewModel(
                    routineWithWaypoints.routine.hash,
                    historyRepository
                )
            }
        ).get(HistoryViewModel::class.java)

        val polygon = PolyUtil.decode(routineWithWaypoints.routine.polygon)
        val bounds = LatLngBounds.builder().also { builder ->
            polygon.forEach {
                builder.include(it)
            }
        }.build()

        setContent {
            DARCAZAATheme {
                val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
                val droneLocation = chargingStationViewModel.position.value
                val currentRoutine = chargingStationViewModel.currentRoutine.value
                val systemStatus = chargingStationViewModel.systemStatus.value
                val battery = chargingStationViewModel.battery.value
                val cameraPositionState = rememberCameraPositionState()
                val heading by animateFloatAsState(
                    targetValue = droneLocation?.heading?.toFloat() ?: 0f
                )
                val lat by animateFloatAsState(
                    targetValue = droneLocation?.latitude?.toFloat() ?: 0f,
                    animationSpec = tween(
                        easing = LinearEasing,
                        durationMillis = 500
                    )
                )
                val lng by animateFloatAsState(
                    targetValue = droneLocation?.longitude?.toFloat() ?: 0f,
                    animationSpec = tween(
                        easing = LinearEasing,
                        durationMillis = 500
                    )
                )

                BottomSheetScaffold(
                    sheetContent = {
                        RideInfo(
                            routineWithWaypoints = routineWithWaypoints,
                            historyViewModel = historyViewModel
                        )
                    },
                    sheetShape = RoundedCornerShape(
                        topStart = 32.dp,
                        topEnd = 32.dp
                    ),
                    sheetPeekHeight = 400.dp,
                    floatingActionButton = {
                        Row {
                            AnimatedVisibility(visible = systemStatus == SystemStatus.IDLE || systemStatus == SystemStatus.WAITING_FOR_BATTERY) {
                                FloatingActionButton(
                                    modifier = Modifier.padding(start = 16.dp),
                                    onClick = {
                                        chargingStationViewModel.runRoutine(routineWithWaypoints.routine)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.FlightTakeoff,
                                        contentDescription = null
                                    )
                                }
                            }
                            AnimatedVisibility(visible = currentRoutine?.routine?.id == routineWithWaypoints.routine.id && systemStatus != SystemStatus.IDLE) {
                                Row {
                                    FloatingActionButton(
                                        modifier = Modifier.padding(start = 8.dp),
                                        onClick = {
                                            startActivity(
                                                Intent(
                                                    this@RoutineInfoActivity,
                                                    TrackActivity::class.java
                                                )
                                            )
                                        },
                                        containerColor = colorScheme.tertiaryContainer,
                                        contentColor = colorScheme.tertiary
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.GpsFixed,
                                            contentDescription = null
                                        )
                                    }
                                    FloatingActionButton(
                                        modifier = Modifier.padding(start = 8.dp),
                                        onClick = {
                                            chargingStationViewModel.cancelRoutine()
                                        },
                                        containerColor = colorScheme.errorContainer,
                                        contentColor = colorScheme.onErrorContainer
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.FlightLand,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Box {
                        GoogleMap(
                            contentPadding = PaddingValues(
                                bottom = 400.dp
                            ),
                            properties = MapProperties(
                                mapType = MapType.SATELLITE
                            ),
                            cameraPositionState = cameraPositionState,
                            onMapLoaded = {
                                cameraPositionState.move(
                                    CameraUpdateFactory.newLatLngBounds(
                                        bounds, 32
                                    )
                                )
                            }
                            ) {
                            Polygon(
                                points = polygon,
                                fillColor = LightThemeColors.primary.copy(alpha = 0.5f),
                                strokeColor = LightThemeColors.primary
                            )
                            if (currentRoutine?.routine?.id == routineWithWaypoints.routine.id) {
                                droneLocation?.let {
                                    Marker(
                                        state = MarkerState(LatLng(lat.toDouble(), lng.toDouble())),
                                        icon = BitmapDescriptorFactory.fromResource(R.drawable.drone_marker),
                                        rotation = heading,
                                        anchor = Offset(0.5f, 0.5f)
                                    )
                                }
                            }
                            Polyline(
                                points = routineWithWaypoints.waypoints.map {
                                    LatLng(
                                        it.latitude,
                                        it.longitude
                                    )
                                },
                                color = LightThemeColors.primaryContainer
                            )
                        }
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp),
                            shape = androidx.compose.material.MaterialTheme.shapes.small
                        ) {
                            Box(
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                )
                            ) {
                                BatteryComponent(
                                    battery = battery
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RideInfo(
    routineWithWaypoints: RoutineWithWaypoints,
    historyViewModel: HistoryViewModel
) {
    val polygon = PolyUtil.decode(routineWithWaypoints.routine.polygon)
    val waypoints = routineWithWaypoints.waypoints.map {
        LatLng(it.latitude, it.longitude)
    }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val area = SphericalUtil.computeArea(polygon)
            ValueDisplay(
                title = "Area", value = if (area.div(1000000) < 1) {
                    "${area.toInt()} m²"
                } else {
                    "${area.div(1000000).toFixedString(1)} km²"
                }
            )
            ValueDisplay(
                title = "Tiempo",
                value = "${ceil((SphericalUtil.computeLength(waypoints) / 10.0) / 60).toInt()} min"
            )
            ValueDisplay(
                title = "Hora",
                value = "${routineWithWaypoints.routine.start.toHour()} hrs"
            )
        }
        Divider()
        LazyColumn {
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                    text = "Historial",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (historyViewModel.history.isNotEmpty()) {
                items(historyViewModel.history) {
                    Card {
                        HistoryItem(it)
                    }
                }
            } else {
                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 0.dp),
                        text = "Sin ejecuciones recientes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}