package mx.ipn.upiiz.darcazaa.view_models

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import mx.ipn.upiiz.darcazaa.data.data_providers.LocalDatabase
import mx.ipn.upiiz.darcazaa.data.models.SyncingStatus
import mx.ipn.upiiz.darcazaa.data.repositories.ConnectionRepository
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val preferences: SharedPreferences,
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
        while (true){
            while (true){
                println("Waiting for connection in ${preferences.getString("url", "ws://192.168.1.1")} ...")
                if(connectionRepository.tryConnect()) break
                else delay(1000)
                loading.value = false
            }
            println("Connected in ${preferences.getString("url", "ws://192.168.1.1")}")
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
            connectionRepository.hashesChanges().collect {
                withContext(Dispatchers.Default){
                    val localHashes = routineRepository.getAll().map { it.routine.hash }

                    if (!(it.containsAll(localHashes) && localHashes.containsAll(it))){
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