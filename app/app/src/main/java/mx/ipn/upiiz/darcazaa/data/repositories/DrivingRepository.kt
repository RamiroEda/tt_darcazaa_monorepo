package mx.ipn.upiiz.darcazaa.data.repositories

import io.socket.client.Socket
import org.json.JSONObject

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
        socket.emit("translate", JSONObject(mapOf(
            "x" to x,
            "y" to y,
            "z" to z
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