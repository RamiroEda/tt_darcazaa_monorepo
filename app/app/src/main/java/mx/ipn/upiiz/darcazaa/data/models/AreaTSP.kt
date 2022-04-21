package mx.ipn.upiiz.darcazaa.data.models

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import org.jgrapht.alg.tour.GreedyHeuristicTSP
import org.jgrapht.alg.tour.NearestNeighborHeuristicTSP
import org.jgrapht.alg.tour.TwoApproxMetricTSP
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleWeightedGraph
import kotlin.math.absoluteValue
import kotlin.math.cos

const val GRID_SEPARATION_METERS = 50.0
const val LATITUDE_DEGREE_METERS = 111319.488

fun areaTsp(area: List<LatLng>): List<LatLng> {
    val graph = SimpleWeightedGraph<LatLng, DefaultWeightedEdge>(DefaultWeightedEdge::class.java)

    val areaGrid = generateGrid(area)

    if (areaGrid.isEmpty()) return emptyList()

    areaGrid.forEach {
        graph.addVertex(it)
    }

    areaGrid.forEach { a ->
        areaGrid.forEach { b ->
            if(a != b){
                val edge = graph.addEdge(a, b)
                if (edge != null){
                    graph.setEdgeWeight(edge, SphericalUtil.computeDistanceBetween(a, b))
                }
            }
        }
    }

    return GreedyHeuristicTSP<LatLng, DefaultWeightedEdge>()
        .getTour(graph)
        .vertexList
}

fun generateGrid(area: List<LatLng>): List<LatLng> {
    val (minLat, maxLat) = area.minMaxOf { it.latitude }
    val (minLng, maxLng) = area.minMaxOf { it.longitude }

    val grid = mutableListOf<LatLng>()

    val modLat = (GRID_SEPARATION_METERS / LATITUDE_DEGREE_METERS).absoluteValue
    var currentLat = minLat.minusMod(modLat)

    while (currentLat < maxLat){
        var currentLng = minLng.minusMod(modLat)

        while (currentLng < maxLng){
            LatLng(currentLat, currentLng).also {
                if(PolyUtil.containsLocation(it, area, true)){
                    grid += it
                }
            }
            currentLng += modLat
        }

        currentLat += modLat
    }

    return grid
}

private fun Double.minusMod(div: Double) = this - this.mod(div)

fun <T>List<T>.minMaxOf(lambda: (T) -> Double): Pair<Double, Double> = Pair(
    minOf(lambda),
    maxOf(lambda)
)