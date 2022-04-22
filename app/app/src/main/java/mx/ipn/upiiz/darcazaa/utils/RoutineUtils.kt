package mx.ipn.upiiz.darcazaa.utils

import mx.ipn.upiiz.darcazaa.data.models.Waypoint
import java.time.DayOfWeek

fun String.toDays(): List<DayOfWeek> = map {
    DayOfWeek.of(it.digitToInt() + 1)
}.filterNotNull()

fun Double.toHour(): String {
    return "${this.toInt().toString().padStart(2, '0')}:${this.mod(1.0).times(60).toInt().toString().padStart(2, '0')}"
}

fun List<List<Waypoint>>.mergeAll(): List<Waypoint>{
    val res = arrayListOf<Waypoint>()
    this.forEach {
        res.addAll(it)
    }
    return res
}

fun Double.toFixedString(decimalPlaces : Int) : String = String.format("%.${decimalPlaces}f", this)