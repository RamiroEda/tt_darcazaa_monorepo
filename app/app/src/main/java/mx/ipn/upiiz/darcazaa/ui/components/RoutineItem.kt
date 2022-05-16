package mx.ipn.upiiz.darcazaa.ui.components

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.ipn.upiiz.darcazaa.R
import mx.ipn.upiiz.darcazaa.data.models.RoutineWithWaypoints
import mx.ipn.upiiz.darcazaa.ui.screens.RoutineInfoActivity
import mx.ipn.upiiz.darcazaa.utils.toDays
import mx.ipn.upiiz.darcazaa.utils.toHour
import mx.ipn.upiiz.darcazaa.view_models.ChargingStationViewModel
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun RoutineItem(
    routineWithWaypoints: RoutineWithWaypoints,
    chargingStationViewModel: ChargingStationViewModel = viewModel()
) {
    val context = LocalContext.current
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clip(androidx.compose.material.MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = {
                    context.startActivity(Intent(context, RoutineInfoActivity::class.java).also {
                        it.putExtra("routine", routineWithWaypoints)
                    })
                },
                onLongClick = {
                    showDeleteDialog = true
                }
            )
            .height(92.dp)
            .fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        border = if (chargingStationViewModel.currentRoutine.value?.routine?.hash == routineWithWaypoints.routine.hash){
            BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
        }else null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp, 92.dp)
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.drone),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = routineWithWaypoints.routine.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = routineWithWaypoints.routine.repeat.toDays().let {
                        if(it.isNotEmpty()){
                            it.joinToString(", ") { d ->
                                d.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es_MX")).substring(0,2)
                            }
                        }else{
                            "Una sola vez"
                        }
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${routineWithWaypoints.routine.start.toHour()} Hrs.",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    if(showDeleteDialog){
        AlertDialog(
            title = {
                    Text(text = "Borrar ${routineWithWaypoints.routine.title}")
            },
            text = {
                   Text(text = "¿Deseas borrar de la lista de rutinas?")
            },
            onDismissRequest = {
                showDeleteDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        chargingStationViewModel.removeRoutine(routineWithWaypoints.routine)
                    },
                ) {
                    Text(
                        text = "Borrar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}