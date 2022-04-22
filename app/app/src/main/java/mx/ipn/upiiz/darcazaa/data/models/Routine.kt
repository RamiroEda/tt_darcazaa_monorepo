package mx.ipn.upiiz.darcazaa.data.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*

@Entity(tableName = "routines", indices = [Index(value = ["hash"], unique = true)])
data class Routine(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "start")
    val start: Double,
    @ColumnInfo(name = "repeat")
    val repeat: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean,
    @ColumnInfo(name = "polygon")
    val polygon: String,
    @ColumnInfo(name = "hash")
    val hash: String = this.hashCode().toUInt().toString(16),
) : Parcelable {


    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!
    ) {
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + start.hashCode()
        result = 31 * result + repeat.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + polygon.hashCode()
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeDouble(start)
        parcel.writeString(repeat)
        parcel.writeString(title)
        parcel.writeByte(if (isSynced) 1 else 0)
        parcel.writeString(polygon)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Routine> {
        override fun createFromParcel(parcel: Parcel): Routine {
            return Routine(parcel)
        }

        override fun newArray(size: Int): Array<Routine?> {
            return arrayOfNulls(size)
        }
    }
}

data class RoutineWithWaypoints(
    @Embedded
    val routine: Routine,
    @Relation(
        entity = Waypoint::class,
        parentColumn = "hash",
        entityColumn = "routine_hash"
    )
    val waypoints: List<Waypoint>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Routine::class.java.classLoader)!!,
        parcel.createTypedArrayList(Waypoint)!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(routine, flags)
        parcel.writeTypedList(waypoints)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RoutineWithWaypoints> {
        override fun createFromParcel(parcel: Parcel): RoutineWithWaypoints {
            return RoutineWithWaypoints(parcel)
        }

        override fun newArray(size: Int): Array<RoutineWithWaypoints?> {
            return arrayOfNulls(size)
        }
    }
}