package mx.ipn.upiiz.darcazaa.data.models

import com.squareup.moshi.Json
import java.util.*

data class History(
    @Json(name = "id")
    val id: Int,
    @Json(name = "routine_hash")
    val routineHash: String,
    @Json(name = "executedAt")
    val executedAt: Date,
    @Json(name = "status")
    val status: String
)
