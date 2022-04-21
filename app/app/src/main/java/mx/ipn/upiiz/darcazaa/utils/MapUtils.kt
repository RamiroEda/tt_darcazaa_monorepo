package mx.ipn.upiiz.darcazaa.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.ktx.addCircle
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.addPolygon
import com.google.maps.android.ktx.addPolyline
import mx.ipn.upiiz.darcazaa.R
import mx.ipn.upiiz.darcazaa.data.models.LatLngAlt
import mx.ipn.upiiz.darcazaa.data.models.Routine
import mx.ipn.upiiz.darcazaa.data.models.Waypoint
import java.util.Collections.addAll

fun GoogleMap.drawDrone(position: LatLngAlt){
    this.addMarker {
        position(LatLng(position.latitude, position.longitude))
        icon(BitmapDescriptorFactory.fromResource(R.drawable.drone_marker))
        rotation(position.heading.toFloat())
        anchor(0.5f, 0.5f)
    }
}

fun GoogleMap.drawRoutineArea(routine: Routine, colorScheme: ColorScheme){
    this.addPolygon {
        addAll(PolyUtil.decode(routine.polygon))
        fillColor(
            (colorScheme.primary.toArgb()
                .toLong() or 0xFF000000 and 0x55FFFFFF).toInt()
        )
        strokeColor(
            (colorScheme.secondary.toArgb()
                .toLong() or 0xFF000000).toInt()
        )
    }
}

fun GoogleMap.drawWaypoints(waypoints: List<Waypoint>, colorScheme: ColorScheme){
    val points = waypoints.map {
        LatLng(it.latitude, it.longitude)
    }

    this.addPolyline {
        addAll(points)
        color(
            (colorScheme.error.toArgb()
                .toLong() or 0xFF000000).toInt()
        )
    }
}