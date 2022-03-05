package managers

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import runCommand
import java.io.File

/**
 * Uses an external executable to fetch device ids and names
 */
class AudioManager {

    fun getInputDevices(): List<InputDevice> {
        return "./getDeviceIds.exe".runCommand(File("./"))?.let {
            Json.decodeFromString(it)
        } ?: emptyList()
    }

    fun getDefaultInputDevice(): InputDevice {
        return getInputDevices().first()
    }

    @Serializable
    data class InputDevice(val name: String, val id: String)
}