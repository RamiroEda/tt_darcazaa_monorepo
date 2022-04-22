package mx.ipn.upiiz.darcazaa.view_models

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.ipn.upiiz.darcazaa.data.data_providers.LocalDatabase
import mx.ipn.upiiz.darcazaa.data.models.*
import mx.ipn.upiiz.darcazaa.data.repositories.ChargingStationRepository
import mx.ipn.upiiz.darcazaa.data.repositories.ConnectionRepository
import javax.inject.Inject

@HiltViewModel
class ChargingStationViewModel @Inject constructor(
    private val chargingStationRepository: ChargingStationRepository,
    private val connectionRepository: ConnectionRepository,
    localDatabase: LocalDatabase
): ViewModel() {
    val systemStatus = mutableStateOf(SystemStatus.UNKNOWN)
    val battery = mutableStateOf(Battery(0, 0.0, 0.0))
    val position = mutableStateOf<LatLngAlt?>(null)
    val routines = mutableStateListOf<RoutineWithWaypoints>()
    val currentRoutine = mutableStateOf<RoutineWithWaypoints?>(null)
    val videoStreamUri = mutableStateOf<String?>(null)

    private val routineRepository = localDatabase.routineRepository()

    init {
        listenSystemStatus()
        listenBattery()
        listenPosition()
        listenRoutines()
        listenCurrentRoutine()
        listenVideoStreamUri()
        emitData()
    }

    private fun listenRoutines() = viewModelScope.launch {
        while (true){
            try {
                routineRepository.getAllFlow().collect {
                    routines.clear()
                    routines.addAll(it)
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    private fun listenVideoStreamUri() = viewModelScope.launch {
        chargingStationRepository.videoUri().collect {
            videoStreamUri.value = it
        }
    }

    fun removeRoutine(routine: Routine) = viewModelScope.launch {
        kotlin.runCatching {
            withContext(Dispatchers.Default){
                routineRepository.delete(routine.id)
                emitData()
            }
        }
    }

    fun runRoutine(routine: Routine) = viewModelScope.launch {
        kotlin.runCatching {
            chargingStationRepository.runRoutine(routine.hash)
        }
    }

    private fun emitData() = viewModelScope.launch {
        kotlin.runCatching {
            connectionRepository.emitData()
        }
    }

    private fun listenSystemStatus() = viewModelScope.launch {
        kotlin.runCatching {
            chargingStationRepository.systemStatus().collect {
                systemStatus.value = it
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun listenBattery() = viewModelScope.launch {
        kotlin.runCatching {
            chargingStationRepository.batteryStatus().collect {
                battery.value = it
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun listenCurrentRoutine() = viewModelScope.launch {
        kotlin.runCatching {
            chargingStationRepository.currentRoutine().collect {
                currentRoutine.value = it
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun cancelRoutine() = viewModelScope.launch {
        kotlin.runCatching {
            chargingStationRepository.cancelRoutine()
        }
    }

    private fun listenPosition() = viewModelScope.launch {
        kotlin.runCatching {
            chargingStationRepository.positionStatus().collect {
                position.value = it
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

}