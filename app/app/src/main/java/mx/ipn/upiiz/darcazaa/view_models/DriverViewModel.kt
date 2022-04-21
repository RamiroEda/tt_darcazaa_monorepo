package mx.ipn.upiiz.darcazaa.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.socket.client.IO
import kotlinx.coroutines.launch
import mx.ipn.upiiz.darcazaa.data.models.PreferenceKeys
import mx.ipn.upiiz.darcazaa.data.models.UserPreferences
import mx.ipn.upiiz.darcazaa.data.repositories.DrivingSocketIORepository
import javax.inject.Inject

@HiltViewModel
class DriverViewModel @Inject constructor(
    preferences: UserPreferences
) : ViewModel() {
    private var currentX = 0.0
    private var currentY = 0.0
    private var currentZ = 0.0
    private val socket = IO.socket(
        "ws://${preferences.get(PreferenceKeys.Url, "192.168.1.1")}/routines",
        IO.Options.builder().setTransports(arrayOf("websocket"))
            .setAuth(mapOf("authorization" to "driver")).build()
    ).connect()
    private val drivingSocketIORepository = DrivingSocketIORepository(socket)

    fun setMode(mode: String) = viewModelScope.launch {
        drivingSocketIORepository.setMode(mode)
    }

    fun setVelocity(x: Double? = null, y: Double? = null, z: Double? = null) =
        viewModelScope.launch {
            x?.let {
                currentX = it
            }
            y?.let {
                currentY = it
            }
            z?.let {
                currentZ = it
            }

            drivingSocketIORepository.setVelocity(currentX, currentY, currentZ)
        }

    fun rotate(direction: Int) = viewModelScope.launch {
        drivingSocketIORepository.rotate(direction)
    }

    fun land() = viewModelScope.launch {
        drivingSocketIORepository.land()
    }

    fun takeoff() = viewModelScope.launch {
        drivingSocketIORepository.takeoff()
    }

    override fun onCleared() {
        super.onCleared()
        socket.disconnect()
    }
}