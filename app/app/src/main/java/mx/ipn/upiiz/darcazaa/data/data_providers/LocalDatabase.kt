package mx.ipn.upiiz.darcazaa.data.data_providers

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mx.ipn.upiiz.darcazaa.data.models.Routine
import mx.ipn.upiiz.darcazaa.data.models.Waypoint
import mx.ipn.upiiz.darcazaa.data.repositories.RoutineRepository

@Database(
    entities = [
        Routine::class,
        Waypoint::class
    ],
    version = 5,
)
abstract class LocalDatabase: RoomDatabase() {
    companion object{
        fun getInstance(context: Context) = Room.databaseBuilder(
            context,
            LocalDatabase::class.java, "local-database"
        ).fallbackToDestructiveMigration().build()
    }

    abstract fun routineRepository(): RoutineRepository
}