package mx.ipn.upiiz.darcazaa.data.contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import mx.ipn.upiiz.darcazaa.data.models.BarcodeTypes

class QRCodeContract: ActivityResultContract<Collection<BarcodeTypes>, String?>() {
    override fun createIntent(context: Context, input: Collection<BarcodeTypes>): Intent = IntentIntegrator(context as Activity)
        .setDesiredBarcodeFormats(input.map { it.codeName })
        .setOrientationLocked(false)
        .setPrompt("Escanea un código")
        .createScanIntent()

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode != Activity.RESULT_OK) return null

        val result: IntentResult? = IntentIntegrator.parseActivityResult(resultCode, intent)

        return result?.contents
    }
}