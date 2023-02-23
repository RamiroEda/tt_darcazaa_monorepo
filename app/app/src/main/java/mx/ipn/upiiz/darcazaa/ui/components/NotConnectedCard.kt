package mx.ipn.upiiz.darcazaa.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.socket.client.IO
import mx.ipn.upiiz.darcazaa.R
import mx.ipn.upiiz.darcazaa.data.contracts.QRCodeContract
import mx.ipn.upiiz.darcazaa.data.data_providers.ioOptions
import mx.ipn.upiiz.darcazaa.data.models.BarcodeTypes
import mx.ipn.upiiz.darcazaa.data.models.PreferenceKeys
import mx.ipn.upiiz.darcazaa.data.models.SocketProvider
import mx.ipn.upiiz.darcazaa.data.models.UserPreferences
import mx.ipn.upiiz.darcazaa.utils.connectToWifiNetwork
import mx.ipn.upiiz.darcazaa.utils.isValidIP
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotConnectedCard(
    preferences: UserPreferences,
    socketProvider: SocketProvider
) {
    val context = LocalContext.current
    val scannerContract = rememberLauncherForActivityResult(contract = QRCodeContract()){
        if (it != null) {
            runCatching {
                JSONObject(it).also { json ->
                    assert(json.has("ip")){"No IP"}
                }
            }.onSuccess { json ->
                val ip = json.getString("ip")
                if (ip.isValidIP()){
                    preferences.set(PreferenceKeys.Url, ip)
                    socketProvider.socket = IO.socket("ws://$ip/routines", ioOptions)

                    if(json.has("ssid")){
                        context.connectToWifiNetwork(
                            json.getString("ssid"),
                            if(json.has("pass")) json.getString("pass") else null
                        )
                    }
                }
            }
        }
    }
    Scaffold {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(32.dp),
                elevation = 0.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                backgroundColor = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        modifier = Modifier.height(100.dp),
                        painter = painterResource(id = R.drawable.drone),
                        contentDescription = "Drone"
                    )
                    Text(
                        text = "Vincular un dispositivo",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "Conectate con tu estación de carga para empezar",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        modifier = Modifier.padding(top = 32.dp),
                        onClick = {
                            scannerContract.launch(listOf(BarcodeTypes.QRCode))
                        },
                    ) {
                        Text(text = "Vincular nuevo dispositivo")
                    }
                }
            }
        }
    }
}