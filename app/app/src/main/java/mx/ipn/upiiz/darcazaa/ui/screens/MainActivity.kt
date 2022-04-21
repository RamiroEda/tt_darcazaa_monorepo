package mx.ipn.upiiz.darcazaa.ui.screens

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import mx.ipn.upiiz.darcazaa.data.models.SocketProvider
import mx.ipn.upiiz.darcazaa.data.models.UserPreferences
import mx.ipn.upiiz.darcazaa.ui.components.MainScreen
import mx.ipn.upiiz.darcazaa.ui.components.NotConnectedCard
import mx.ipn.upiiz.darcazaa.ui.theme.DARCAZAATheme
import mx.ipn.upiiz.darcazaa.view_models.ConnectionViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var preferences: UserPreferences
    @Inject lateinit var socketProvider: SocketProvider
    private val connectionViewModel: ConnectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DARCAZAATheme {
                Crossfade(targetState = connectionViewModel.loading.value) { loading ->
                    if(!loading){
                        Crossfade(targetState = connectionViewModel.url.value) { url ->
                            if(url != null){
                                MainScreen(
                                    preferences,
                                    socketProvider
                                )
                            }else{
                                NotConnectedCard(
                                    preferences,
                                    socketProvider
                                )
                            }
                        }
                    }else{
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        connectionViewModel.syncRoutines()
    }
}