package mx.ipn.upiiz.darcazaa.data.repositories

import io.socket.client.Socket
import org.json.JSONObject

const val HORIZONTAL_VELOCITY_FACTOR = 0.2
const val VERTICAL_VELOCITY_FACTOR = 0.5
const val ALTITUDE_VELOCITY_FACTOR = 1.1

interface DrivingRepository {
    fun setVelocity(x: Double, y: Double, z: Double)
    fun rotate(direction: Int)
    fun setMode(mode: String)
    fun land()
    fun takeoff()
}

class DrivingSocketIORepository(
    private val socket: Socket
) : DrivingRepository {
    override fun setVelocity(x: Double, y: Double, z: Double) {
        println("$x $y $z")
        socket.emit("translate", JSONObject(mapOf(
            "x" to x * VERTICAL_VELOCITY_FACTOR,
            "y" to y * HORIZONTAL_VELOCITY_FACTOR,
            "z" to z * ALTITUDE_VELOCITY_FACTOR
        )))
    }

    override fun rotate(direction: Int) {
        socket.emit("rotate", direction)
    }

    override fun setMode(mode: String) {
        socket.emit("change_mode", mode)
    }

    override fun takeoff() {
        socket.emit("takeoff")
    }

    override fun land() {
        socket.emit("land")
    }
}