package mx.ipn.upiiz.darcazaa.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import mx.ipn.upiiz.darcazaa.data.data_providers.LocalDatabase
import mx.ipn.upiiz.darcazaa.data.models.PreferenceKeys
import mx.ipn.upiiz.darcazaa.data.models.SyncingStatus
import mx.ipn.upiiz.darcazaa.data.models.UserPreferences
import mx.ipn.upiiz.darcazaa.data.repositories.ConnectionRepository
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val preferences: UserPreferences,
    localDatabase: LocalDatabase
): ViewModel() {
    val isConnected = mutableStateOf(false)
    val loading = mutableStateOf(true)
    val url = mutableStateOf<String?>(null)
    val syncingStatus = mutableStateOf(SyncingStatus.UNKNOWN)
    private val routineRepository = localDatabase.routineRepository()

    init {
        waitForConnection()
        listenUrlChanges()
    }

    private fun listenUrlChanges() = viewModelScope.launch {
        kotlin.runCatching {
            connectionRepository.urlChanges().collect {
                url.value = it
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun waitForConnection() = viewModelScope.launch {
        while (isActive){
            while (isActive){
                println("Waiting for connection in ${preferences.get(PreferenceKeys.Url, "192.168.1.1")} ...")
                if(connectionRepository.tryConnect()) break
                else delay(1000)
                loading.value = false
            }
            println("Connected in ${preferences.get(PreferenceKeys.Url, "192.168.1.1")}")
            isConnected.value = true
            val syncingStatus = listenSyncingStatus()
            val hashes = listenToHashes()
            listenConnectionStatus().join()
            syncingStatus.cancel()
            hashes.cancel()
        }
    }

    private fun listenToHashes() = viewModelScope.launch {
        kotlin.runCatching {
            connectionRepository.routinesChanges().collect {
                withContext(Dispatchers.Default){
                    val localRoutines = routineRepository.getAll()
                    val incomingHashes = it.map { it.hash }

                    for (localRoutine in localRoutines) {
                        val remoteRoutine = it.find { it.hash == localRoutine.routine.hash }

                        if (remoteRoutine != null) {
                            if (remoteRoutine.repeat == "" && remoteRoutine.executedAt != null){
                                routineRepository.delete(remoteRoutine.id)
                            }
                        }
                    }

                    val localHashes = routineRepository.getAll().map { it.routine.hash }

                    println(localHashes)
                    println(incomingHashes)

                    if (!(incomingHashes.containsAll(localHashes) && localHashes.containsAll(incomingHashes))){
                        routineRepository.markAllAsUnSynced()
                        syncRoutines()
                    }
                }
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun syncRoutines() = viewModelScope.launch {
        withContext(Dispatchers.Default){
            val routines = routineRepository.getAll()
            if(routines.any { !it.routine.isSynced } || routines.isEmpty()){
                connectionRepository.syncRoutines(routines)
            }
        }
    }

    private fun listenSyncingStatus() = viewModelScope.launch {
        kotlin.runCatching {
            connectionRepository.syncingStatus().collect {
                println(it)
                syncingStatus.value = it
                if (it == SyncingStatus.ERROR){
                    delay(5000)
                    syncRoutines()
                }else if (it == SyncingStatus.SYNCED){
                    withContext(Dispatchers.Default){
                        routineRepository.markAllAsSynced()
                    }
                }
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun listenConnectionStatus() = viewModelScope.launch {
        kotlin.runCatching {
            connectionRepository.connectionStatus().collect {
                println("Connection status: $it")
                isConnected.value = it
                if(!it) cancel()
            }
        }.onFailure {
            it.printStackTrace()
        }
    }
}