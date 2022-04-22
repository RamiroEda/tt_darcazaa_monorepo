package mx.ipn.upiiz.darcazaa.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.PlayerControlView

@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    exoPlayer: ExoPlayer
) {
    AndroidView(
        modifier = modifier,
        factory = {
            PlayerControlView(it).apply {
                player = exoPlayer
                hide()
            }
        }
    )
}

@Composable
fun rememberExoPlayerLifecycleObserver(exoPlayer: ExoPlayer) = remember {
    LifecycleEventObserver { _, event ->
        exoPlayer.prepare()
        when (event) {
            Lifecycle.Event.ON_CREATE -> exoPlayer.play()
            Lifecycle.Event.ON_START -> exoPlayer.play()
            Lifecycle.Event.ON_RESUME -> exoPlayer.play()
            Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
            Lifecycle.Event.ON_STOP -> exoPlayer.stop()
            Lifecycle.Event.ON_DESTROY -> exoPlayer.release()
            else -> throw IllegalStateException()
        }
    }
}


@Composable
fun rememberExoPlayer(): ExoPlayer {
    val context = LocalContext.current

    val player = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context).setLiveMaxOffsetMs(50))
            .build()
    }

    val lifecycleObserver = rememberExoPlayerLifecycleObserver(player)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return player
}