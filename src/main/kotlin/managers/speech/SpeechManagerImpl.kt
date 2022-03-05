package managers.speech

import com.hera.voice.BuildConfig
import com.hera.voice.BuildConfig.SPEECH_API_KEY
import com.microsoft.cognitiveservices.speech.CancellationReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import managers.AudioManager

class SpeechManagerImpl(private val audioManager: AudioManager) : SpeechManager {

    private val subscriptionKey = SPEECH_API_KEY
    private val regionCode = "eastus"

    private val recognizer: SpeechRecognizer
    private var currentInputDevice = audioManager.getDefaultInputDevice()

    init {
        println(currentInputDevice.name)
        println("Init SpeechManager")
        recognizer = setupSpeech()
    }

    private fun setupSpeech(): SpeechRecognizer {
        val speechConfig = SpeechConfig.fromSubscription(
            subscriptionKey,
            regionCode,
        )
        speechConfig.enableDictation()
        speechConfig.speechRecognitionLanguage = "fr-CA"
        val audioConfig = AudioConfig.fromMicrophoneInput(currentInputDevice.id)
        val recognizer = SpeechRecognizer(speechConfig, audioConfig)
        recognizer.canceled.addEventListener { s, e ->
            println("CANCELED: Reason=" + e.getReason());

            if (e.getReason() == CancellationReason.Error) {
                println("CANCELED: ErrorCode=" + e.getErrorCode());
                println("CANCELED: ErrorDetails=" + e.getErrorDetails());
                println("CANCELED: Did you update the subscription info?");
            }
        }
        recognizer.sessionStopped.addEventListener { s, e ->
            println("\n    Session stopped event.")
        }
        recognizer.sessionStarted.addEventListener { s, e ->
            println("Session started")
        }
        recognizer.sessionStopped.addEventListener { s, e ->
            println("Session stopped")
        }
        return recognizer
    }

    override fun addRecognizingListener(listener: (String) -> Unit) {
        recognizer.recognizing.addEventListener { any, speechRecognitionEventArgs ->
            listener(speechRecognitionEventArgs.result.text)
        }
    }

    override fun addRecognizedListener(listener: (String) -> Unit) {
        recognizer.recognized.addEventListener { any, speechRecognitionEventArgs ->
            listener(speechRecognitionEventArgs.result.text)
        }
    }

    override fun startContinuousRecognitionAsync() {
        recognizer.startContinuousRecognitionAsync()
    }

    override fun stopContinuousRecognitionAsync() {
        recognizer.stopContinuousRecognitionAsync()
    }

    override fun getSupportedInputDevices(): List<AudioManager.InputDevice> {
        return audioManager.getInputDevices()
    }

    override fun setInputDevice(device: AudioManager.InputDevice) {
        currentInputDevice = device
        setupSpeech()
    }

    companion object {
        val instance: SpeechManager by lazy {
            if (BuildConfig.SPEECH_ENABLED) {
                SpeechManagerImpl(AudioManager())
            } else {
                SpeechManagerAbstract()
            }
        }
    }
}