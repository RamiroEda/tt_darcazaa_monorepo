package mx.ipn.upiiz.darcazaa.ui.components

import android.view.ContextThemeWrapper
import android.widget.TimePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import mx.ipn.upiiz.darcazaa.R

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit = {},
    is24Hour: Boolean = false,
    initialTime: Double? = null,
    onSet: (Double) -> Unit = {}
){
    var timePicker: TimePicker? = null

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card {
            Column {
                AndroidView(
                    factory = {
                        TimePicker(ContextThemeWrapper(it, R.style.TimePickerTheme)).also { picker ->
                            picker.setIs24HourView(is24Hour)
                            timePicker = picker
                            if(initialTime != null){
                                picker.hour = initialTime.toInt()
                                picker.minute = initialTime.mod(1.0).times(60).toInt()
                            }
                        }
                    }
                )
                Row(
                    modifier = Modifier
                        .padding(
                            vertical = 8.dp,
                            horizontal = 16.dp
                        )
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        onClick = {
                            onDismissRequest()
                        }
                    ){
                        Text(text = "Cancelar")
                    }
                    TextButton(
                        onClick = {
                            timePicker?.let {
                                onSet(it.hour + (it.minute/60.0))
                            }
                        }
                    ){
                        Text(text = "Aceptar")
                    }
                }
            }
        }
    }
}