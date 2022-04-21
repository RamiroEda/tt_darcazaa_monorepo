package mx.ipn.upiiz.darcazaa.ui.components

import android.content.Intent
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
import mx.ipn.upiiz.darcazaa.R
import mx.ipn.upiiz.darcazaa.ui.screens.EnterIPActivity
import mx.ipn.upiiz.darcazaa.ui.theme.Shapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotConnectedCard() {
    val context = LocalContext.current
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
                            context.startActivity(Intent(context, EnterIPActivity::class.java))
                        },
                    ) {
                        Text(text = "Vincular nuevo dispositivo")
                    }
                }
            }
        }
    }
}