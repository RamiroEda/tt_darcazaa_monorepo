package mx.ipn.upiiz.darcazaa.data.repositories

import android.content.SharedPreferences
import io.socket.client.Socket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import mx.ipn.upiiz.darcazaa.data.models.RoutineWithWaypoints
import mx.ipn.upiiz.darcazaa.data.models.SocketProvider
import mx.ipn.upiiz.darcazaa.data.models.SyncingStatus
import mx.ipn.upiiz.darcazaa.utils.mergeAll
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

interface ConnectionRepository {
    suspend fun tryConnect(): Boolean
    fun connectionStatus(): Flow<Boolean>
    fun urlChanges(): Flow<String?>
    fun syncingStatus(): Flow<SyncingStatus>
    fun hashesChanges(): Flow<List<String>>
    suspend fun syncRoutines(routines: List<RoutineWithWaypoints>)
    fun emitData()
}

class ConnectionSocketIORepository(
    private val socketProvider: SocketProvider,
    private val preferences: SharedPreferences
): ConnectionRepository{
    override suspend fun tryConnect(): Boolean = try {
        if(socketProvider.socket.connected()){
            true
        }else{
            socketProvider.socket.connect().connected()
        }
    }catch (e: Exception){
        e.printStackTrace()
        false
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun connectionStatus(): Flow<Boolean> = callbackFlow {
        socketProvider.socket.on(Socket.EVENT_CONNECT){
            trySend(true)
        }
        socketProvider.socket.on(Socket.EVENT_CONNECT_ERROR){
            trySend(false)
            cancel(Socket.EVENT_CONNECT_ERROR)
        }
        socketProvider.socket.on(Socket.EVENT_DISCONNECT){
            trySend(false)
            cancel(Socket.EVENT_DISCONNECT)
        }
        awaitClose {  }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun hashesChanges(): Flow<List<String>> = callbackFlow {
        socketProvider.socket.on("hashes"){
            val hashes = it.firstOrNull()
            if(hashes is JSONArray){
                val hashesList = mutableListOf<String>()

                for (i in 0 until hashes.length()){
                    hashesList.add(hashes[i] as String)
                }

                trySend(hashesList)
            }
        }
        awaitClose {  }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun syncingStatus(): Flow<SyncingStatus> = callbackFlow {
        socketProvider.socket.on("sync"){
            val status = it.firstOrNull()
            if(status is String){
                trySend(SyncingStatus.safeValueOf(status))
            }
        }
        awaitClose {  }
    }

    override suspend fun syncRoutines(routines: List<RoutineWithWaypoints>) {
        socketProvider.socket.emit("sync_routines", JSONObject(mapOf(
            "routines" to JSONArray(routines.map { data ->
                data.routine.let {
                    JSONObject(mapOf(
                        "id" to it.id,
                        "start" to it.start,
                        "repeat" to it.repeat,
                        "title" to it.title,
                        "hash" to it.hash
                    ))
                }
            }),
            "waypoints" to JSONArray(routines.map {
                it.waypoints
            }.mergeAll().map { data ->
                data.let {
                    JSONObject(mapOf(
                        "id" to it.id,
                        "index" to it.index,
                        "latitude" to it.latitude,
                        "longitude" to it.longitude,
                        "routine_id" to it.routine_id
                    ))
                }
            })
        )))
    }

    override fun emitData() {
        socketProvider.socket.emit("emit_data")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun urlChanges(): Flow<String?> = callbackFlow {
        if(preferences.contains("url")){
            trySend(preferences.getString("url", ""))
        }else{
            trySend(null)
        }
        preferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            if(key == "url"){
                if(sharedPreferences.contains("url")){
                    trySend(sharedPreferences.getString("url", ""))
                }else{
                    trySend(null)
                }
            }
        }

        awaitClose {  }
    }
}