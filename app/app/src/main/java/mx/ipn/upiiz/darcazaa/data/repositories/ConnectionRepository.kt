package mx.ipn.upiiz.darcazaa.data.repositories

import io.socket.client.Socket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import mx.ipn.upiiz.darcazaa.data.models.*
import mx.ipn.upiiz.darcazaa.utils.mergeAll
import org.json.JSONArray
import org.json.JSONObject

interface ConnectionRepository {
    suspend fun tryConnect(): Boolean
    fun connectionStatus(): Flow<Boolean>
    fun urlChanges(): Flow<String?>
    fun syncingStatus(): Flow<SyncingStatus>
    fun routinesChanges(): Flow<List<Routine>>
    suspend fun syncRoutines(routines: List<RoutineWithWaypoints>)
    fun emitData()
}

class ConnectionSocketIORepository(
    private val socketProvider: SocketProvider,
    private val preferences: UserPreferences
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
    override fun routinesChanges(): Flow<List<Routine>> = callbackFlow {
        socketProvider.socket.on("routines"){
            val hashes = it.firstOrNull()
            if(hashes is JSONArray){
                val hashesList = mutableListOf<Routine>()

                for (i in 0 until hashes.length()){
                    val routine = hashes.getJSONObject(i)
                    hashesList.add(
                        Routine(
                            id = routine.getInt("id"),
                            start = routine.getDouble("start"),
                            repeat = routine.getString("repeat"),
                            title = routine.getString("title"),
                            isSynced = true,
                            hash = routine.getString("hash"),
                            executedAt = if(!routine.isNull("executedAt")){
                                routine.getInt("executedAt")
                            }else null
                        )
                    )
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
                        "routine_hash" to it.routine_hash
                    ))
                }
            })
        )))
    }

    override fun emitData() {
        socketProvider.socket.emit("emit_data")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun urlChanges(): Flow<String?>
        = preferences.preferenceChanges
            .filter { it.first === PreferenceKeys.Url }
            .map { it.second as? String? }
}