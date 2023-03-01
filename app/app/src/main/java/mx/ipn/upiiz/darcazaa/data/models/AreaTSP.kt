package mx.ipn.upiiz.darcazaa.data.models

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import org.jgrapht.alg.tour.GreedyHeuristicTSP
import org.jgrapht.alg.tour.NearestInsertionHeuristicTSP
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleWeightedGraph
import kotlin.math.absoluteValue

private const val GRID_SEPARATION_METERS = 50.0
private const val GRID_SEPARATION_METERS_SMALL = 5.0
private const val LATITUDE_DEGREE_METERS = 111319.488

fun areaTsp(area: List<LatLng>): List<LatLng> {
    val areaGrid = generateGrid(area)

    println(areaGrid)

    if (areaGrid.isEmpty()) return emptyList()

    val res = mutableListOf<LatLng>()

    areaGrid.forEachIndexed { index, latLngs ->
        if(index % 2 == 0){
            latLngs.forEach { res += it }
        }else{
            latLngs.reversed().forEach { res += it}
        }
    }

    res.add(areaGrid.first().first())

    return res
}

fun generateGrid(area: List<LatLng>): List<List<LatLng>> {
    val (minLat, maxLat) = area.minMaxOf { it.latitude }
    val (minLng, maxLng) = area.minMaxOf { it.longitude }
    val polyArea = SphericalUtil.computeArea(area)

    val grid = mutableListOf<MutableList<LatLng>>()

    val modLat = ((if(polyArea >= 5000) GRID_SEPARATION_METERS else GRID_SEPARATION_METERS_SMALL) / LATITUDE_DEGREE_METERS).absoluteValue
    var currentLat = minLat.minusMod(modLat)

    while (currentLat < maxLat){
        var currentLng = minLng.minusMod(modLat)
        grid.add(mutableListOf())

        while (currentLng < maxLng){
            LatLng(currentLat, currentLng).also {
                if(PolyUtil.containsLocation(it, area, true)){
                    grid.last() += it
                }
            }
            currentLng += modLat
        }

        currentLat += modLat
    }

    return grid.filter { it.isNotEmpty() }
}

private fun Double.minusMod(div: Double) = this - this.mod(div)

fun <T>List<T>.minMaxOf(lambda: (T) -> Double): Pair<Double, Double> = Pair(
    minOf(lambda),
    maxOf(lambda)
)