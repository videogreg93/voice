package managers.speech

interface SpeechManager {
    fun addRecognizingListener(listener: (String) -> Unit)
    fun addRecognizedListener(listener: (String) -> Unit)
    fun startContinuousRecognitionAsync()
    fun stopContinuousRecognitionAsync()

}