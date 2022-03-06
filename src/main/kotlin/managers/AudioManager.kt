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
        return "${FileManager.mainFolder.toFile()}/getDeviceIds.exe".runCommand(FileManager.mainFolder.toFile())?.let {
            Json.decodeFromString(it)
        } ?: emptyList()
    }

    fun getDefaultInputDevice(): InputDevice? {
        return getInputDevices().firstOrNull()
    }

    @Serializable
    data class InputDevice(val name: String, val id: String)
}