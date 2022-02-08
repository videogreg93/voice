import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.microsoft.cognitiveservices.speech.CancellationReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import models.VoiceField
import java.util.concurrent.Semaphore


@Composable
@Preview
fun App() {

    val inputs = listOf(
        VoiceField("Numéro de patient", VoiceField.Size.SMALL),
        VoiceField("Introduction", VoiceField.Size.MEDIUM),
        VoiceField("Autre", VoiceField.Size.LARGE),
    )

    val allTexts = mutableStateListOf("", "", "")
    // Used during recognition to append current session to previous text
    var startingText = ""

    var selectedInputIndex: Int by remember { mutableStateOf(0) }

    recognizer.recognizing.addEventListener { _, e ->
        allTexts[selectedInputIndex] = startingText + " " + e.result.text
    }
    recognizer.recognized.addEventListener { s, e ->
        allTexts[selectedInputIndex] = startingText + " " + e.result.text
        startingText = allTexts[selectedInputIndex]
    }

    MaterialTheme {
        var isRecording by remember { mutableStateOf(false) }
        Scaffold(
            topBar = {
                TopAppBar {
                    Text("Voice", modifier = Modifier.padding(start = 12.dp, end = 24.dp))
                    var recordButtonText by remember { mutableStateOf("Record") }
                    val onClick = {
                        isRecording = !isRecording
                        recordButtonText = if (isRecording) {
                            recognizer.startContinuousRecognitionAsync()
                            "Recording..."
                        } else {
                            recognizer.stopContinuousRecognitionAsync()
                            "Record"
                        }
                    }
                    recordButton(recordButtonText, onClick)
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                inputs.mapIndexed { index, voiceField ->
                    VoiceTextField(
                        voiceField, allTexts[index],
                        onChange = {
                            allTexts[index] = it
                            if (!isRecording) {
                                startingText = it
                            }
                        },
                        onFocusChange = {
                            if (it.hasFocus) {
                                selectedInputIndex = index
                                startingText = allTexts[index]
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun recordButton(text: String, onTap: () -> Unit) {
    Button(onClick = onTap) {
        Image(
            painterResource("microphone.png"),
            "",
            colorFilter = ColorFilter.tint(Color.White),
        )
        Text(text)
    }
}

@Composable
fun VoiceTextField(
    voiceField: VoiceField,
    input: String,
    onChange: (String) -> Unit,
    onFocusChange: (FocusState) -> Unit
) {
    val height = when (voiceField.size) {
        VoiceField.Size.SMALL -> Dp.Unspecified
        VoiceField.Size.MEDIUM -> 100.dp
        VoiceField.Size.LARGE -> 200.dp
    }
    OutlinedTextField(
        value = input,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(top = 16.dp)
            .defaultMinSize(minHeight = height)
            .onFocusChanged(onFocusChange),
        onValueChange = onChange,
        label = { Text(voiceField.label) },
    )
}

private val stopTranslationWithFileSemaphore: Semaphore = Semaphore(0)
lateinit var recognizer: SpeechRecognizer

fun main() = application {
    recognizer = setupSpeech()
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

private fun setupSpeech(): SpeechRecognizer {
    val speechConfig = SpeechConfig.fromSubscription(
        "efcf3d32d3ca4d9fa2d18c8874bab6b7",
        "eastus",
    )
    speechConfig.enableDictation()
    speechConfig.speechRecognitionLanguage = "fr-CA"
    val audioConfig = AudioConfig.fromDefaultMicrophoneInput()
    val recognizer = SpeechRecognizer(speechConfig, audioConfig)
    recognizer.canceled.addEventListener { s, e ->
        System.out.println("CANCELED: Reason=" + e.getReason());

        if (e.getReason() == CancellationReason.Error) {
            System.out.println("CANCELED: ErrorCode=" + e.getErrorCode());
            System.out.println("CANCELED: ErrorDetails=" + e.getErrorDetails());
            System.out.println("CANCELED: Did you update the subscription info?");
        }

        stopTranslationWithFileSemaphore.release();
    }
    recognizer.sessionStopped.addEventListener { s, e ->
        System.out.println("\n    Session stopped event.");
        stopTranslationWithFileSemaphore.release();
    }
    recognizer.sessionStarted.addEventListener { s, e ->
        println("Session started")
    }
    recognizer.sessionStopped.addEventListener { s, e ->
        println("Session stopped")
    }
//    stopTranslationWithFileSemaphore.acquire()
    return recognizer
}
