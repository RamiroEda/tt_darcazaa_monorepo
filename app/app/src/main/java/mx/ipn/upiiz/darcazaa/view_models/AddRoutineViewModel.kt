package mx.ipn.upiiz.darcazaa.view_models

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.ipn.upiiz.darcazaa.data.data_providers.LocalDatabase
import mx.ipn.upiiz.darcazaa.data.models.Routine
import mx.ipn.upiiz.darcazaa.data.models.Waypoint
import mx.ipn.upiiz.darcazaa.data.models.areaTsp
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class AddRoutineViewModel @Inject constructor(
    localDatabase: LocalDatabase
): ViewModel() {
    private val routineRepository = localDatabase.routineRepository()
    val title = mutableStateOf("")
    val hour = mutableStateOf(12.0)
    val isSingleUse = mutableStateOf(true)
    val repeat  = mutableStateOf("")
    var section = mutableStateOf(false)
    val selectedPolygon = mutableStateListOf<LatLng>()
    val insertSuccess = mutableStateOf(false)

    fun addPoint(latLng: LatLng){
        selectedPolygon.add(latLng)
    }

    fun undoPoint(){
        selectedPolygon.removeLastOrNull()
    }

    fun save() = viewModelScope.launch {
        kotlin.runCatching {
            withContext(Dispatchers.Default){
                val routine = Routine(
                    0,
                    hour.value,
                    repeat.value,
                    title.value,
                    false,
                    PolyUtil.encode(selectedPolygon),
                    Random.nextInt().toUInt().toString(16)
                )
                routineRepository.addRoutine(routine)
                routineRepository.addWaypoints(areaTsp(selectedPolygon).mapIndexed { index, latLng ->
                    Waypoint(
                        0,
                        index,
                        latLng.latitude,
                        latLng.longitude,
                        routine.hash
                    )
                })
            }
        }.onSuccess {
            insertSuccess.value = true
        }.onFailure {
            it.printStackTrace()
        }
    }
}