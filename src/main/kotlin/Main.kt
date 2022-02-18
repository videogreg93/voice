import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
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
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Semaphore
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

val mainFolder by lazy { Paths.get(System.getenv("APPDATA"), "/Voice") }
val generatedDocument by lazy { Paths.get(mainFolder.absolutePathString(), "/generated.docx") }
val template by lazy { Paths.get(mainFolder.absolutePathString(), "/template.docx").toFile() }

@ExperimentalMaterialApi
@Composable
@Preview
fun App() {
    val inputs = listOf(
        VoiceField("Date de l'Opération", VoiceField.Size.SMALL, "{id_date}"),
        VoiceField("Nom du patient", VoiceField.Size.SMALL, "{id_nom_patient}"),
        VoiceField("Numéro de dossier", VoiceField.Size.SMALL, "{id_number}"),
        VoiceField("NAM", VoiceField.Size.SMALL, "{id_nam}"),
        VoiceField("DDN", VoiceField.Size.SMALL, "{id_ddn}"),
        VoiceField("Aĝe", VoiceField.Size.SMALL, "{id_age}"),
        VoiceField("No Visite", VoiceField.Size.SMALL, "{id_visite}"),
        VoiceField("Diagnostic Préopératoire", VoiceField.Size.MEDIUM, "{id_diagnostic_preoperatoire}"),
        VoiceField("Diagnostic Postopératoire", VoiceField.Size.MEDIUM, "{id_diagnostic_postoperatoire}"),
        VoiceField("Protocole Opératoire", VoiceField.Size.LARGE, "{id_protocole_operatoire}"),
        VoiceField("Nom du médecin", VoiceField.Size.SMALL, "{id_nom_medecin}"),
        VoiceField("Département", VoiceField.Size.SMALL, "{id_nom_departement}"),
    )

    val allTexts = mutableStateListOf(
        *inputs.map { "" }.toTypedArray()
    )
    val stateVertical = rememberScrollState(0)
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

    var openDialog by remember { mutableStateOf(false) }

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
                    Spacer(Modifier.width(100.dp))
                    exportButton {
                        replaceIdsInDocument(
                            inputs = allTexts.mapIndexed { index, s ->
                                inputs[index].id to s
                            },
                            input = template,
                            output = FileOutputStream(generatedDocument.toFile())
                        )
                        openDialog = true
                    }
                    if (openDialog) {
                        AlertDialog(
                            title = {
                                Text("Success!")
                            },
                            text = {
                                SelectionContainer {
                                    Text("Successfully generated at ${generatedDocument.absolutePathString()}")
                                }
                            },
                            buttons = {
                                Button(
                                    onClick = {
                                        openDialog = false

                                    }
                                ) {
                                    Text("Ok")
                                }

                            },
                            onDismissRequest = {},
                        )
                    }
                }
            }
        ) {
            VerticalScrollbar(
                modifier = Modifier.fillMaxHeight(),
                adapter = rememberScrollbarAdapter(stateVertical),
            )
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(stateVertical),
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
fun exportButton(onTap: () -> Unit) {
    Button(onClick = onTap) {
        Text("Export")
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

@ExperimentalMaterialApi
fun main() = application {
    recognizer = setupSpeech()
    if (!mainFolder.exists()) Files.createDirectories(mainFolder)
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
