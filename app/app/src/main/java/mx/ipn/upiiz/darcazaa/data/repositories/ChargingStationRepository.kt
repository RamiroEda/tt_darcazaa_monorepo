package mx.ipn.upiiz.darcazaa.data.repositories

import io.socket.client.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import mx.ipn.upiiz.darcazaa.data.data_providers.LocalDatabase
import mx.ipn.upiiz.darcazaa.data.data_providers.ioOptions
import mx.ipn.upiiz.darcazaa.data.models.*
import org.json.JSONObject

interface ChargingStationRepository {
    fun systemStatus(): Flow<SystemStatus>
    fun batteryStatus(): Flow<Battery>
    fun positionStatus(): Flow<LatLngAlt>
    fun currentRoutine(): Flow<RoutineWithWaypoints?>
    fun videoData(): Flow<ByteArray>
    fun cancelRoutine()
    fun runRoutine(hash: String)
}

class DroneSocketIORepository(
    private val socketProvider: SocketProvider,
    localDatabase: LocalDatabase,
    private val preferences: UserPreferences
) : ChargingStationRepository {
    private val routineRepository = localDatabase.routineRepository()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun systemStatus(): Flow<SystemStatus> = callbackFlow {
        socketProvider.socket.on("status") {
            val status = it.firstOrNull()
            if (status is String) {
                trySend(SystemStatus.safeByName(status))
            }
        }
        awaitClose { }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun batteryStatus(): Flow<Battery> = callbackFlow {
        socketProvider.socket.on("battery") {
            val battery = it.firstOrNull()
            if (battery is JSONObject) {
                trySend(
                    Battery(
                        level = battery.getInt("level"),
                        current = battery.getDouble("current"),
                        voltage = battery.getDouble("voltage")
                    )
                )
            }
        }
        awaitClose { }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun positionStatus(): Flow<LatLngAlt> = callbackFlow {
        socketProvider.socket.on("location") {
            val position = it.firstOrNull()
            if (position is JSONObject) {
                trySend(
                    LatLngAlt(
                        latitude = position.getDouble("latitude"),
                        longitude = position.getDouble("longitude"),
                        altitude = position.getDouble("altitude"),
                        speed = position.getDouble("speed"),
                        heading = position.getDouble("heading")
                    )
                )
            }
        }
        awaitClose { }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun currentRoutine(): Flow<RoutineWithWaypoints?> = callbackFlow {
        socketProvider.socket.on("current_mission") {
            val routine = it.firstOrNull()

            if (routine is JSONObject) {
                trySend(routineRepository.get(routine.getInt("id")))
            } else {
                trySend(null)
            }
        }
        awaitClose { }
    }

    override fun videoData(): Flow<ByteArray> = callbackFlow {
        IO.socket("ws://${preferences.get(PreferenceKeys.Url, "192.168.1.1")}/camera", ioOptions)
            .connect()
            .on("message") {
                val uri = it.firstOrNull()
                if (uri is ByteArray) {
                    trySend(uri)
                }
            }
        awaitClose { }
    }

    override fun cancelRoutine() {
        socketProvider.socket.emit("cancel")
    }

    override fun runRoutine(hash: String) {
        socketProvider.socket.emit("run_mission", hash)
    }
}