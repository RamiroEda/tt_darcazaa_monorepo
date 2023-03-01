package mx.ipn.upiiz.darcazaa.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.material.timepicker.TimeFormat
import mx.ipn.upiiz.darcazaa.data.models.History
import mx.ipn.upiiz.darcazaa.ui.theme.DarkThemeColors
import mx.ipn.upiiz.darcazaa.ui.theme.LightThemeColors
import java.text.DateFormat
import java.util.*

@Composable
fun HistoryItem(
    history: History
) {
    val colors = if (!isSystemInDarkTheme()) {
        LightThemeColors
    } else {
        DarkThemeColors
    }

    val locale = Locale("es", "MX")
    val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, TimeFormat.CLOCK_24H, locale)
    val date = dateFormat.format(history.executedAt)

    return Card(
        modifier = Modifier
            .padding(horizontal = 32.dp, vertical = 8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (history.status == "Completado")
                colors.primaryContainer
            else
                colors.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = history.status,
                style = MaterialTheme.typography.h6
            )
            Text(text = date)
        }
    }
}