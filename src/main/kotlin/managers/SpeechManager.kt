package managers

import com.hera.voice.BuildConfig.SPEECH_API_KEY
import com.microsoft.cognitiveservices.speech.CancellationReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig

class SpeechManager {

    private val subscriptionKey = SPEECH_API_KEY
    private val regionCode = "eastus"

    val recognizer: SpeechRecognizer

    init {
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
        val audioConfig = AudioConfig.fromDefaultMicrophoneInput()
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
}