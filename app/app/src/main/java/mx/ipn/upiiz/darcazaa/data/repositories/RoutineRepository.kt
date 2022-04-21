package mx.ipn.upiiz.darcazaa.data.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import mx.ipn.upiiz.darcazaa.data.models.Routine
import mx.ipn.upiiz.darcazaa.data.models.RoutineWithWaypoints
import mx.ipn.upiiz.darcazaa.data.models.Waypoint

@Dao
interface RoutineRepository {
    @Transaction
    @Query("SELECT * FROM routines")
    fun getAllFlow(): Flow<List<RoutineWithWaypoints>>

    @Transaction
    @Query("SELECT * FROM routines")
    fun getAll(): List<RoutineWithWaypoints>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    fun get(id: Int): RoutineWithWaypoints

    @Query("UPDATE routines SET is_synced = 1")
    fun markAllAsSynced()

    @Query("UPDATE routines SET is_synced = 0")
    fun markAllAsUnSynced()

    @Query("DELETE FROM routines WHERE id = :id")
    fun delete(id: Int)

    @Insert
    suspend fun addRoutine(routine: Routine): Long

    @Insert
    suspend fun addWaypoints(waypoints: List<Waypoint>): List<Long>
}