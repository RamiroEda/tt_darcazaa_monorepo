package mx.ipn.upiiz.darcazaa.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
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
fun rememberExoPlayer(): ExoPlayer {
    val context = LocalContext.current
    return remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context).setLiveMaxOffsetMs(50))
            .build()
    }
}