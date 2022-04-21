package mx.ipn.upiiz.darcazaa.utils

import mx.ipn.upiiz.darcazaa.data.models.Waypoint
import java.time.DayOfWeek

fun Int.toDays(): List<DayOfWeek> = List(7){
    val mask = (1).shl(it)
    if(this.and(mask) != 0) {
        DayOfWeek.of(it+1)
    }else{
        null
    }
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