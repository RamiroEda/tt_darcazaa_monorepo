package mx.ipn.upiiz.darcazaa.data.models

data class LatLngAlt(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Double = 0.0,
    val heading: Double = 0.0
)