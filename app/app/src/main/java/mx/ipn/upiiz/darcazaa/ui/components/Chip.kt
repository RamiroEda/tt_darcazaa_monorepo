package mx.ipn.upiiz.darcazaa.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun Chip(
    text: String = "",
    isChecked: Boolean = false,
    isCheckable: Boolean = true,
    @DrawableRes chipIcon: Int? = null,
    @DrawableRes checkedIcon: Int? = null,
    @DrawableRes closeIcon: Int? = null,
    onClick: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    AndroidView(
        factory = {
            com.google.android.material.chip.Chip(it).also { chip ->
                chip.isCheckable = isCheckable
                chip.isCheckedIconVisible = true
                chip.isChipIconVisible = true
                chip.isCloseIconVisible = true
            }
        }
    ){
        it.isChecked = isChecked

        it.text = text
        it.chipIcon = chipIcon?.let { icon ->
            ContextCompat.getDrawable(context, icon)
        }
        it.checkedIcon = checkedIcon?.let { icon ->
            ContextCompat.getDrawable(context, icon)
        }
        it.closeIcon = closeIcon?.let { icon ->
            ContextCompat.getDrawable(context, icon)
        }
        it.setOnClickListener{ _ ->
            onClick()
            it.isChecked = isChecked
        }
        it.setOnCloseIconClickListener{ _ ->
            onClose()
            it.isChecked = isChecked
        }
    }
}