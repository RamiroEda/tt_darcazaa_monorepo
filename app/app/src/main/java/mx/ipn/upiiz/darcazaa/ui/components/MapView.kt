package mx.ipn.upiiz.darcazaa.ui.components

import android.os.Bundle
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.MapStyleOptions
import mx.ipn.upiiz.darcazaa.R

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    mapPadding: PaddingValues = PaddingValues(),
    mapView: MapView = rememberMapViewWithLifecycle(),
    isCenterPointEnabled: Boolean = false,
    isTrafficEnabled: Boolean = false,
    mapFinish: (GoogleMap) -> Unit = {},
    cameraMove: (GoogleMap) -> Unit = {},
    mapUpdate: (GoogleMap) -> Unit = {}
) {
    val isDarkTheme = isSystemInDarkTheme()
    val density = LocalDensity.current

    Box {
        AndroidView(
            modifier = modifier,
            factory = {
                mapView.also {
                    it.getMapAsync { map ->
                        mapFinish(map)
                        map.setOnCameraIdleListener {
                            cameraMove(map)
                        }
                        map.isTrafficEnabled = isTrafficEnabled
                    }
                }
            }
        ) { map ->
            map.getMapAsync {
                with(density){
                    it.setPadding(
                        mapPadding.calculateLeftPadding(LayoutDirection.Ltr).roundToPx(),
                        mapPadding.calculateTopPadding().roundToPx(),
                        mapPadding.calculateRightPadding(LayoutDirection.Ltr).roundToPx(),
                        mapPadding.calculateBottomPadding().roundToPx(),
                    )
                }
                if(isDarkTheme){
                    it.setMapStyle(MapStyleOptions.loadRawResourceStyle(map.context, R.raw.dark_map_style))
                }else{
                    it.setMapStyle(MapStyleOptions.loadRawResourceStyle(map.context, R.raw.light_map_style))
                }
                it.clear()
                mapUpdate(it)
            }
        }
        if(isCenterPointEnabled){
            Box(
                modifier = Modifier.fillMaxSize().padding(mapPadding).animateContentSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.clip(CircleShape).size(8.dp).background(Color.Red)
                )
            }
        }
    }
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map
        }
    }

    val lifecycleObserver = rememberMapLifecycleObserver(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

@Composable
fun rememberMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    remember(mapView) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle.EMPTY)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> throw IllegalStateException()
            }
        }
    }