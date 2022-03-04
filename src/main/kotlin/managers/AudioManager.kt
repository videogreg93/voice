package managers

import runCommand
import java.io.File

/**
 * Uses an external executable to fetch device ids and names
 */
class AudioManager {

    fun getInputDevices(): List<InputDevice> {
        return "./getDeviceIds.exe".runCommand(File("./"))?.let {
            it.lines().filter { it.isNotBlank() }.map {
                // TODO modify the c++ code so that the seperator isn't just a comma
                val split = it.split(",")
                InputDevice(split.first(), split.last())
            }
        } ?: emptyList()
    }

    fun getDefaultInputDevice(): InputDevice {
        return getInputDevices().first()
    }

    data class InputDevice(val name: String, val id: String)
}