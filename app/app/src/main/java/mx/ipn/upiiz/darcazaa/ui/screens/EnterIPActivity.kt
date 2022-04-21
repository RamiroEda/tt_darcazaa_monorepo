package mx.ipn.upiiz.darcazaa.ui.screens

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.IO
import mx.ipn.upiiz.darcazaa.data.data_providers.ioOptions
import mx.ipn.upiiz.darcazaa.data.models.SocketProvider
import mx.ipn.upiiz.darcazaa.ui.theme.DARCAZAATheme
import javax.inject.Inject

@AndroidEntryPoint
class EnterIPActivity : AppCompatActivity() {
    @Inject
    lateinit var preferences: SharedPreferences

    @Inject
    lateinit var socketProvider: SocketProvider

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DARCAZAATheme {
                var ipText by remember {
                    mutableStateOf(preferences.getString("url", "ws://192.168.1.1") ?: "")
                }
                Scaffold {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            OutlinedTextField(
                                value = ipText,
                                onValueChange = {
                                    ipText = it
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Uri,
                                    autoCorrect = false,
                                ),
                                singleLine = true
                            )
                            Button(
                                modifier = Modifier.padding(top = 16.dp),
                                onClick = {
                                    if(ipText.matches(Regex("^([a-z0-9+.-]+):(?://(?:((?:[a-z0-9-._~!\$&'()*+,;=:]|%[0-9A-F]{2})*)@)?((?:[a-z0-9-._~!\$&'()*+,;=]|%[0-9A-F]{2})*)(?::(\\d*))?(/(?:[a-z0-9-._~!\$&'()*+,;=:@/]|%[0-9A-F]{2})*)?|(/?(?:[a-z0-9-._~!\$&'()*+,;=:@]|%[0-9A-F]{2})+(?:[a-z0-9-._~!\$&'()*+,;=:@/]|%[0-9A-F]{2})*)?)(?:\\?((?:[a-z0-9-._~!\$&'()*+,;=:/?@]|%[0-9A-F]{2})*))?(?:#((?:[a-z0-9-._~!\$&'()*+,;=:/?@]|%[0-9A-F]{2})*))?\$", RegexOption.IGNORE_CASE))){
                                        preferences.edit().putString("url", ipText).apply()
                                        socketProvider.socket = IO.socket("$ipText/routines", ioOptions)
                                        finish()
                                    }
                                }
                            ) {
                                Text(text = "Seleccionar IP")
                            }
                        }
                    }
                }
            }
        }
    }
}