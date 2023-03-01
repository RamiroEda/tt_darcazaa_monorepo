package mx.ipn.upiiz.darcazaa.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send
import com.google.accompanist.flowlayout.FlowRow
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import dagger.hilt.android.AndroidEntryPoint
import mx.ipn.upiiz.darcazaa.R
import mx.ipn.upiiz.darcazaa.ui.components.Chip
import mx.ipn.upiiz.darcazaa.ui.components.TimePickerDialog
import mx.ipn.upiiz.darcazaa.ui.theme.DARCAZAATheme
import mx.ipn.upiiz.darcazaa.ui.theme.LightThemeColors
import mx.ipn.upiiz.darcazaa.utils.toHour
import mx.ipn.upiiz.darcazaa.view_models.AddRoutineViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

@AndroidEntryPoint
class AddRoutineActivity : AppCompatActivity() {
    private val addRoutineViewModel: AddRoutineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DARCAZAATheme {
                val insertSuccess = addRoutineViewModel.insertSuccess.value

                Crossfade(targetState = addRoutineViewModel.section.value) {
                    if (!it) {
                        RoutineBasicInfo()
                    } else {
                        SelectArea()
                    }
                }

                SideEffect {
                    if (insertSuccess) {
                        finish()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineBasicInfo(
    addRoutineViewModel: AddRoutineViewModel = viewModel()
) {
    var showTimePicker by remember {
        mutableStateOf(false)
    }
    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 32.dp)
        ) {
            Text(
                modifier = Modifier.padding(top = 32.dp),
                text = "Nueva rutina",
                style = MaterialTheme.typography.headlineLarge
            )
            OutlinedTextField(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .fillMaxWidth(),
                value = addRoutineViewModel.title.value,
                onValueChange = { addRoutineViewModel.title.value = it },
                label = {
                    Text(text = "Titulo")
                },
                singleLine = true
            )
            Text(
                modifier = Modifier.padding(top = 32.dp),
                text = "Frecuencia de ejecucion",
                style = MaterialTheme.typography.labelLarge
            )
            FlowRow(
                mainAxisSpacing = 8.dp
            ) {
                Chip(
                    text = "Una sola vez",
                    isChecked = addRoutineViewModel.isSingleUse.value,
                    checkedIcon = R.drawable.ic_round_check_24,
                    onClick = {
                        addRoutineViewModel.isSingleUse.value = true
                        addRoutineViewModel.repeat.value = ""
                    }
                )
                Chip(
                    text = "Repetir",
                    isChecked = !addRoutineViewModel.isSingleUse.value,
                    checkedIcon = R.drawable.ic_round_check_24,
                    onClick = {
                        addRoutineViewModel.isSingleUse.value = false
                    }
                )
            }
            AnimatedVisibility(visible = !addRoutineViewModel.isSingleUse.value) {
                Column {
                    Text(
                        modifier = Modifier.padding(top = 16.dp),
                        text = "Días",
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(
                        crossAxisSpacing = 0.dp,
                        mainAxisSpacing = 8.dp,
                    ) {
                        DayOfWeek.values().forEachIndexed { index, dayOfWeek ->
                            Chip(
                                text = dayOfWeek.getDisplayName(
                                    TextStyle.FULL_STANDALONE,
                                    Locale.getDefault()
                                ),
                                isChecked = addRoutineViewModel.repeat.value.contains("$index"),
                                onClick = {
                                    if (addRoutineViewModel.repeat.value.contains("$index")) {
                                        addRoutineViewModel.repeat.value =
                                            addRoutineViewModel.repeat.value.filterNot { char -> "$char" == "$index" }
                                    } else {
                                        addRoutineViewModel.repeat.value += "$index"
                                    }
                                },
                                isCheckable = true,
                                checkedIcon = R.drawable.ic_round_check_24,
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = addRoutineViewModel.hour.value.toHour(),
                    onValueChange = {},
                    label = {
                        Text(text = "Hora")
                    },
                    readOnly = true
                )
                Box(
                    modifier = Modifier
                        .clickable {
                            showTimePicker = true
                        }
                        .fillMaxWidth()
                        .height(64.dp)
                ) {}
            }

            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 48.dp),
                onClick = {
                    if (
                        addRoutineViewModel.title.value.isNotBlank()
                    ) {
                        addRoutineViewModel.section.value = true
                    }
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Map,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = "Seleccionar area",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = {
                showTimePicker = false
            },
            onSet = {
                showTimePicker = false
                addRoutineViewModel.hour.value = it
            },
            initialTime = addRoutineViewModel.hour.value
        )
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectArea(
    addRoutineViewModel: AddRoutineViewModel = viewModel()
) {
    val context = LocalContext.current as FragmentActivity
    var locationEnabled by remember {
        mutableStateOf(false)
    }
    val cameraPositionState = rememberCameraPositionState()

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (addRoutineViewModel.selectedPolygon.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            addRoutineViewModel.undoPoint()
                        },
                        icon = {
                            Icon(imageVector = Icons.Rounded.Undo, contentDescription = null)
                        },
                        text = {
                            Text(text = "Deshacer")
                        },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                }
                ExtendedFloatingActionButton(
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = {
                        addRoutineViewModel.addPoint(cameraPositionState.position.target)
                    },
                    icon = {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                    },
                    text = {
                        Text(text = "Agregar perimetro")
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.tertiary
                )

                ExtendedFloatingActionButton(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = {
                        if (addRoutineViewModel.selectedPolygon.size >= 3) {
                            addRoutineViewModel.save()
                        }
                    },
                    icon = {
                        Icon(imageVector = Icons.Rounded.Check, contentDescription = null)
                    },
                    text = {
                        Text(text = "Finalizar rutina")
                    }
                )
            }
        }
    ) {
        Box(
            modifier = Modifier.padding(it)
        ) {
            GoogleMap(
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = MapType.SATELLITE,
                    isMyLocationEnabled = locationEnabled
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false
                ),
                onMapLoaded = {
                    context.permissionsBuilder(Manifest.permission.ACCESS_FINE_LOCATION).build()
                        .send { res ->
                            if (res.allGranted()) {
                                locationEnabled = true
                                val fusedLocationClient =
                                    LocationServices.getFusedLocationProviderClient(context)
                                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                                    if(loc != null){
                                        cameraPositionState.move(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(
                                                    loc.latitude,
                                                    loc.longitude
                                                ), 17f
                                            )
                                        )
                                    }
                                }
                            }
                        }
                }
            ) {
                Marker(
                    state = MarkerState(cameraPositionState.position.target)
                )
                if (addRoutineViewModel.selectedPolygon.isNotEmpty()) {
                    Polygon(
                        points = addRoutineViewModel.selectedPolygon.toList(),
                        fillColor = LightThemeColors.primaryContainer.copy(alpha = 0.5f),
                        strokeColor = LightThemeColors.primaryContainer
                    )
                }
                addRoutineViewModel.selectedPolygon.map { latLng ->
                    Circle(
                        center = latLng,
                        radius = 4.0,
                        strokeColor = LightThemeColors.secondaryContainer,
                        fillColor = LightThemeColors.secondaryContainer
                    )
                }
            }
            IconButton(
                modifier = Modifier.padding(
                    horizontal = 8.dp,
                    vertical = 4.dp
                ),
                onClick = {
                    addRoutineViewModel.section.value = false
                }
            ) {
                Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
        }
    }
}