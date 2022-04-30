package mx.ipn.upiiz.darcazaa.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView

@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    exoPlayer: ExoPlayer
) {
    val androidView = AndroidView(
        modifier = modifier,
        factory = {
            PlayerView(it).apply {
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                useController = false
                player = exoPlayer
                layoutParams =
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams
                            .MATCH_PARENT,
                        ViewGroup.LayoutParams
                            .MATCH_PARENT
                    )
            }
        }
    )

    DisposableEffect(key1 = androidView) {
        onDispose { exoPlayer.release() }
    }
}

@Composable
fun rememberExoPlayer(): ExoPlayer {
    val context = LocalContext.current

    val player = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(context).setLiveMaxOffsetMs(50).setLiveTargetOffsetMs(0)
            )
            .build()
    }

    return player
}