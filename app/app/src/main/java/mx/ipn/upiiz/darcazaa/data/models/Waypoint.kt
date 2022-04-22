package mx.ipn.upiiz.darcazaa.data.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import androidx.room.ForeignKey.CASCADE

@Entity(
    tableName = "waypoints",
    foreignKeys = [
        ForeignKey(
            entity = Routine::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("routine_hash"),
            onDelete = CASCADE
        )
    ],
    indices = [
        Index(value = arrayOf("routine_hash"))
    ]
)
data class Waypoint(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "index")
    val index: Int,
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    @ColumnInfo(name = "longitude")
    val longitude: Double,
    @ColumnInfo(name = "routine_hash")
    val routine_hash: String
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(index)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(routine_hash)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Waypoint> {
        override fun createFromParcel(parcel: Parcel): Waypoint {
            return Waypoint(parcel)
        }

        override fun newArray(size: Int): Array<Waypoint?> {
            return arrayOfNulls(size)
        }
    }
}