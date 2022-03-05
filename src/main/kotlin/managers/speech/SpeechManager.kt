package managers.speech

import managers.AudioManager

interface SpeechManager {
    fun addRecognizingListener(listener: (String) -> Unit)
    fun addRecognizedListener(listener: (String) -> Unit)
    fun startContinuousRecognitionAsync()
    fun stopContinuousRecognitionAsync()
    fun getSupportedInputDevices(): List<AudioManager.InputDevice>
    fun setInputDevice(device: AudioManager.InputDevice)
}