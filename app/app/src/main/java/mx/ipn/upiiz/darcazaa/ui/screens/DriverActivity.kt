package mx.ipn.upiiz.darcazaa.ui.screens

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.PressGestureScope
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import dagger.hilt.android.AndroidEntryPoint
import mx.ipn.upiiz.darcazaa.R
import mx.ipn.upiiz.darcazaa.data.models.PreferenceKeys
import mx.ipn.upiiz.darcazaa.data.models.SystemStatus
import mx.ipn.upiiz.darcazaa.data.models.UserPreferences
import mx.ipn.upiiz.darcazaa.data.models.WebSocketDataSource
import mx.ipn.upiiz.darcazaa.ui.components.VideoPlayer
import mx.ipn.upiiz.darcazaa.ui.components.rememberExoPlayer
import mx.ipn.upiiz.darcazaa.ui.theme.DARCAZAATheme
import mx.ipn.upiiz.darcazaa.view_models.ChargingStationViewModel
import mx.ipn.upiiz.darcazaa.view_models.DriverViewModel
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

val JOYSTICK_SIZE = 168.dp
val JOYSTICK_BALL_SIZE = 56.dp
val BUTTON_SIZE = 64.dp

@AndroidEntryPoint
class DriverActivity : AppCompatActivity() {
    private val driverViewModel: DriverViewModel by viewModels()
    private val chargingStationViewModel: ChargingStationViewModel by viewModels()

    @Inject
    lateinit var preferences: UserPreferences

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            DARCAZAATheme {
                Scaffold(
                    containerColor = Color.White
                ) {
                    GoogleMap(
                        properties = MapProperties(
                            mapType = MapType.SATELLITE,
                        ),
                        uiSettings = MapUiSettings(
                            tiltGesturesEnabled = false,
                            rotationGesturesEnabled = false,
                            zoomGesturesEnabled = false,
                            scrollGesturesEnabled = false
                        ),
                        cameraPositionState = CameraPositionState(
                            position = chargingStationViewModel.position.value.let {
                                CameraPosition(
                                    LatLng(
                                        it?.latitude ?: 0.0,
                                        it?.longitude ?: 0.0,
                                    ),
                                    18f,
                                    0f,
                                    it?.heading?.toFloat() ?: 0f
                                )
                            }
                        )
                    ){
                        chargingStationViewModel.position.value?.let {
                            Marker(
                                state = MarkerState(LatLng(it.latitude, it.longitude)),
                                icon = BitmapDescriptorFactory.fromResource(R.drawable.drone_marker),
                                rotation = it.heading.toFloat(),
                                anchor = Offset(0.5f, 0.5f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        var isDrivingEnabled by remember {
                            mutableStateOf(false)
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                                .size(168.dp)
                        ) {
                            val density = LocalDensity.current
                            val joystickBallCenter = with(density) {
                                JOYSTICK_SIZE.div(2).minus(
                                    JOYSTICK_BALL_SIZE.div(2)
                                ).toPx()
                            }
                            val joystickCenter = with(density) {
                                JOYSTICK_SIZE.div(2).toPx()
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(
                                            alpha = 0.3f
                                        )
                                    )
                            ) {}
                            var offsetAbsoluteX by remember {
                                mutableStateOf(joystickBallCenter)
                            }
                            var offsetAbsoluteY by remember {
                                mutableStateOf(joystickBallCenter)
                            }
                            var offsetX by remember {
                                mutableStateOf(offsetAbsoluteX)
                            }
                            var offsetY by remember {
                                mutableStateOf(offsetAbsoluteY)
                            }
                            Box(
                                Modifier
                                    .offset {
                                        IntOffset(
                                            offsetX.roundToInt(),
                                            offsetY.roundToInt()
                                        )
                                    }
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .size(56.dp)
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragEnd = {
                                                offsetAbsoluteX = joystickBallCenter
                                                offsetAbsoluteY = joystickBallCenter
                                                offsetX = joystickBallCenter
                                                offsetY = joystickBallCenter
                                                driverViewModel.setVelocity(
                                                    x = 0.0,
                                                    y = 0.0
                                                )
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                offsetAbsoluteX += dragAmount.x
                                                offsetAbsoluteY += dragAmount.y

                                                val magX = offsetX - joystickBallCenter
                                                val magY = offsetY - joystickBallCenter

                                                val magnitude = magnitude(
                                                    offsetAbsoluteX,
                                                    offsetAbsoluteY,
                                                    joystickBallCenter,
                                                    joystickBallCenter
                                                )
                                                if (magnitude <= joystickCenter) {
                                                    offsetX = offsetAbsoluteX
                                                    offsetY = offsetAbsoluteY
                                                } else {
                                                    val ratio = joystickCenter / magnitude
                                                    val x = offsetAbsoluteX - joystickBallCenter
                                                    val y = offsetAbsoluteY - joystickBallCenter
                                                    offsetX = joystickBallCenter + (x * ratio)
                                                    offsetY = joystickBallCenter + (y * ratio)
                                                }

                                                driverViewModel.setVelocity(
                                                    y = magX
                                                        .div(joystickCenter)
                                                        .times(10)
                                                        .toDouble(),
                                                    x = magY
                                                        .times(-1)
                                                        .div(joystickCenter)
                                                        .times(10)
                                                        .toDouble()
                                                )
                                            }
                                        )
                                    }
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .size(BUTTON_SIZE * 3)
                        ) {
                            CustomFab(
                                modifier = Modifier.align(Alignment.TopCenter),
                                icon = Icons.Rounded.ArrowUpward,
                                onPress = {
                                    driverViewModel.setVelocity(z = -1.0)
                                    awaitRelease()
                                    driverViewModel.setVelocity(z = 0.0)
                                }
                            )
                            CustomFab(
                                modifier = Modifier.align(Alignment.BottomCenter),
                                icon = Icons.Rounded.ArrowDownward,
                                onPress = {
                                    driverViewModel.setVelocity(z = 1.0)
                                    awaitRelease()
                                    driverViewModel.setVelocity(z = 0.0)
                                }
                            )
                            CustomFab(
                                modifier = Modifier.align(Alignment.CenterStart),
                                icon = Icons.Rounded.Undo,
                                onPress = {
                                    driverViewModel.rotate(-1)
                                    awaitRelease()
                                    driverViewModel.rotate(0)
                                }
                            )
                            CustomFab(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                icon = Icons.Rounded.Redo,
                                onPress = {
                                    driverViewModel.rotate(1)
                                    awaitRelease()
                                    driverViewModel.rotate(0)
                                }
                            )
                        }
                        Row(
                            modifier = Modifier.align(Alignment.TopEnd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedVisibility(visible = isDrivingEnabled) {
                                if (chargingStationViewModel.systemStatus.value == SystemStatus.FLYING) {
                                    CustomFab(
                                        icon = Icons.Rounded.FlightLand,
                                        onPress = {
                                            awaitRelease()
                                            driverViewModel.land()
                                        }
                                    )
                                } else if (chargingStationViewModel.systemStatus.value == SystemStatus.IDLE
                                    || chargingStationViewModel.systemStatus.value == SystemStatus.WAITING_FOR_WEATHER
                                ) {
                                    CustomFab(
                                        icon = Icons.Rounded.FlightTakeoff,
                                        onPress = {
                                            awaitRelease()
                                            driverViewModel.takeoff()
                                        }
                                    )
                                }
                            }
                            Card(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        vertical = 4.dp,
                                        horizontal = 16.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Habiltar conducción",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Switch(
                                        modifier = Modifier.padding(start = 4.dp),
                                        checked = isDrivingEnabled,
                                        onCheckedChange = {
                                            isDrivingEnabled = it
                                            if (it) {
                                                driverViewModel.setMode("GUIDED")
                                            } else {
                                                driverViewModel.setMode("AUTO")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun magnitude(x: Float, y: Float, pivotX: Float = 0f, pivotY: Float = 0f) =
        sqrt(((x - pivotX).pow(2) + (y - pivotY).pow(2)).toDouble()).toFloat()
}

@Composable
fun CustomFab(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onPress: suspend PressGestureScope.(Offset) -> Unit
) {
    Box(
        modifier = modifier
            .size(BUTTON_SIZE)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = onPress
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}