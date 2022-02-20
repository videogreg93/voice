package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hera.voice.BuildConfig
import i18n.Messages
import managers.FileManager
import managers.SpeechManager
import managers.TextBoy
import models.Doctor
import models.VoiceField
import org.apache.commons.io.FilenameUtils
import replaceIdsInDocument
import java.awt.Cursor
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.util.*
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.io.path.absolutePathString


@OptIn(ExperimentalMaterialApi::class)
class MainScreen(val user: Doctor, val speechManager: SpeechManager) {
    @ExperimentalMaterialApi
    @Composable
    @Preview
    fun App() {
        val inputs = listOf(
            VoiceField("Date de l'Opération", VoiceField.Size.SMALL, "{id_date}"),
            VoiceField("Nom du patient", VoiceField.Size.SMALL, "{id_nom_patient}", true),
            VoiceField("Numéro de dossier", VoiceField.Size.SMALL, "{id_number}"),
            VoiceField("NAM", VoiceField.Size.SMALL, "{id_nam}"),
            VoiceField("DDN", VoiceField.Size.SMALL, "{id_ddn}"),
            VoiceField("Âge", VoiceField.Size.SMALL, "{id_age}"),
            VoiceField("No Visite", VoiceField.Size.SMALL, "{id_visite}"),
            VoiceField("Diagnostic Préopératoire", VoiceField.Size.MEDIUM, "{id_diagnostic_preoperatoire}"),
            VoiceField("Diagnostic Postopératoire", VoiceField.Size.MEDIUM, "{id_diagnostic_postoperatoire}"),
            VoiceField("Protocole Opératoire", VoiceField.Size.LARGE, "{id_protocole_operatoire}"),
            VoiceField("Nom du médecin", VoiceField.Size.SMALL, "{id_nom_medecin}", isUsername = true),
            VoiceField("Département", VoiceField.Size.SMALL, "{id_nom_departement}"),
        )

        val allTexts = mutableStateListOf(
            *inputs.map { "" }.toTypedArray()
        )
        val stateVertical = rememberScrollState(0)
        // Used during recognition to append current session to previous text
        var startingText = ""

        var selectedInputIndex: Int by remember { mutableStateOf(0) }
        var exportedFilename by remember { mutableStateOf(FileManager.generatedDocument.absolutePathString()) }

        speechManager.recognizer.recognizing.addEventListener { _, e ->
            allTexts[selectedInputIndex] = startingText + " " + e.result.text
        }
        speechManager.recognizer.recognized.addEventListener { s, e ->
            allTexts[selectedInputIndex] = startingText + " " + e.result.text
            startingText = allTexts[selectedInputIndex]
        }

        var showExportDialog by remember { mutableStateOf(false) }
        var showAboutDialog by remember { mutableStateOf(false) }

        // Prefill username field
        inputs.indexOfFirst { it.isUsername }.takeUnless { it == -1 }?.let { index ->
            allTexts[index] = user.givenName
        }

        MaterialTheme(
            colors = MaterialTheme.colors.copy(
                primary = Color.Blue,
                onPrimary = Color.White,
            )
        ) {
            var isRecording by remember { mutableStateOf(false) }
            Scaffold(
                topBar = {
                    TopAppBar(
                        contentColor = MaterialTheme.colors.onPrimary
                    ) {
                        TextButton(onClick = {
                            showAboutDialog = true
                        }) {
                            Text(
                                text = TextBoy.getMessage(Messages.appName),
                                modifier = Modifier.padding(start = 12.dp, end = 24.dp),
                                color = MaterialTheme.colors.onPrimary
                            )
                        }
                        var recordButtonText by remember { mutableStateOf(TextBoy.getMessage(Messages.record)) }
                        val onClick = {
                            isRecording = !isRecording
                            recordButtonText = if (isRecording) {
                                speechManager.recognizer.startContinuousRecognitionAsync()
                                TextBoy.getMessage(Messages.recording)
                            } else {
                                speechManager.recognizer.stopContinuousRecognitionAsync()
                                TextBoy.getMessage(Messages.record)
                            }
                        }
                        recordButton(recordButtonText, onClick)
                        Spacer(Modifier.width(32.dp))
                        exportButton {
                            val fileName = inputs.indexOfFirst { it.isFileName }.takeUnless { it == -1 }?.let {
                                allTexts[it] + ".docx"
                            } ?: "generated.docx"
                            val fileChooser = JFileChooser(FileManager.myDocuments).apply {
                                selectedFile = File(fileName)
                                isAcceptAllFileFilterUsed = false
                                val fileFilter = FileNameExtensionFilter("Only .docx files", "docx")
                                addChoosableFileFilter(fileFilter)
                            }
                            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                                var output = fileChooser.selectedFile
                                if (!FilenameUtils.getExtension(fileChooser.name).equals("docx", ignoreCase = true)) {
                                    output = File(
                                        output.parentFile,
                                        FilenameUtils.getBaseName(output.name) + ".docx"
                                    )
                                }
                                replaceIdsInDocument(
                                    inputs = allTexts.mapIndexed { index, s ->
                                        inputs[index].id to s
                                    },
                                    input = FileManager.template,
                                    output = FileOutputStream(output)
                                )
                                exportedFilename = output.absolutePath
                                showExportDialog = true
                            }
                        }
                        if (showExportDialog) {
                            AlertDialog(
                                title = {
                                    Text(TextBoy.getMessage(Messages.success))
                                },
                                text = {
                                    SelectionContainer {
                                        Text("${TextBoy.getMessage(Messages.success)} $exportedFilename")
                                    }
                                },
                                buttons = {
                                    Row(modifier = Modifier.padding(32.dp)) {
                                        Button(
                                            onClick = {
                                                Desktop.getDesktop().open(File(exportedFilename))
                                                showExportDialog = false
                                            }
                                        ) {
                                            Text(TextBoy.getMessage(Messages.openCta))
                                        }
                                        Spacer(modifier = Modifier.size(16.dp))
                                        Button(
                                            onClick = {
                                                showExportDialog = false

                                            }
                                        ) {
                                            Text(TextBoy.getMessage(Messages.ok))
                                        }
                                    }

                                },
                                onDismissRequest = {},
                            )
                        }
                        if (showAboutDialog) {
                            AboutDialog( { showAboutDialog = false },
                                { url ->
                                    println(url)
                                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                        println("Open Url")
                                        Desktop.getDesktop().browse(URI(url))
                                    }
                                })
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
                                if (!isRecording) {
                                    allTexts[index] = it
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
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onPrimary),
            )
            Spacer(Modifier.padding(4.dp))
            Text(text)
        }
    }

    @Composable
    fun exportButton(onTap: () -> Unit) {
        Button(onClick = onTap) {
            Image(
                painterResource("export.png"),
                "",
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onPrimary),
            )
            Spacer(Modifier.padding(4.dp))
            Text(TextBoy.getMessage(Messages.export))
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

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun AboutDialog(onDismiss: () -> Unit, onClickUrl: (String) -> Unit) {
        AlertDialog(
            title = {
                Text("About")
            },
            text = {
                SelectionContainer {
                    Column(
                        modifier = Modifier.defaultMinSize(minWidth = 600.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Version: ${BuildConfig.APP_VERSION}")
                        Text(TextBoy.getMessage(Messages.foundABug))
                        val urlText = buildAnnotatedString {
                            append(TextBoy.getMessage(Messages.latestRelease))
                            pushStringAnnotation(
                                tag = "URL",
                                annotation = TextBoy.getMessage(Messages.latestReleaseUrl)
                            )
                            withStyle(
                                style = SpanStyle(
                                    color = Color.Blue,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(TextBoy.getMessage(Messages.latestReleaseUrl))
                            }

                            pop()
                        }
                        DisableSelection {
                            ClickableText(
                                text = urlText,
                                modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
                                onClick = { offset ->
                                    urlText.getStringAnnotations(tag = "URL", start = offset,
                                        end = offset)
                                        .firstOrNull()?.let { annotation ->
                                            onClickUrl(annotation.item)
                                        }
                                }
                            )
                        }
                    }
                }
            },
            buttons = {
                Row(modifier = Modifier.padding(32.dp)) {
                    Button(
                        onClick = onDismiss
                    ) {
                        Text(TextBoy.getMessage(Messages.ok))
                    }
                }

            },
            onDismissRequest = {},
        )
    }
}