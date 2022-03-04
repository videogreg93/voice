package managers.speech

/**
 * Empty version for when we dont actually need to do speech recognition
 */
class SpeechManagerAbstract: SpeechManager {
    override fun addRecognizingListener(listener: (String) -> Unit) {

    }

    override fun addRecognizedListener(listener: (String) -> Unit) {

    }

    override fun startContinuousRecognitionAsync() {

    }

    override fun stopContinuousRecognitionAsync() {

    }
}