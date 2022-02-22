package ui.main

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
import managers.TemplateManager
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
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.io.path.absolutePathString


@OptIn(ExperimentalMaterialApi::class)
class MainScreen(val user: Doctor, val speechManager: SpeechManager) {

    @ExperimentalMaterialApi
    @Composable
    @Preview
    fun App() {
        val viewModel by remember { mutableStateOf(MainScreenViewModel(user, TemplateManager())) }
        val stateVertical = rememberScrollState(0)
        var exportedFilename by remember { mutableStateOf(FileManager.generatedDocument.absolutePathString()) }

        speechManager.recognizer.recognizing.addEventListener { _, e ->
            viewModel.state.onSpeechRecognizing(" " + e.result.text)
        }
        speechManager.recognizer.recognized.addEventListener { s, e ->
            viewModel.state.onSpeechRecognized(" " + e.result.text)
        }

        var showExportDialog by remember { mutableStateOf(false) }
        var showAboutDialog by remember { mutableStateOf(false) }

        MaterialTheme(
            colors = MaterialTheme.colors.copy(
                primary = Color.Blue,
                onPrimary = Color.White,
            )
        ) {
            var isRecording by remember { mutableStateOf(false) }
            var isTemplatesDropdownExpanded by remember { mutableStateOf(false) }
            val templatesItems = listOf("Protocole Opératoire", "BEM")
            var templatesSelectedIndex by remember { mutableStateOf(0) }
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
                            val fileName = viewModel.state.inputs.firstOrNull { it.isFileName }?.let {
                                it.text + ".docx"
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
                                    inputs = viewModel.state.inputs.map {
                                        it.id to it.text
                                    },
                                    input = File(FileManager.mainFolder.toFile(), viewModel.state.templateFile),
                                    output = FileOutputStream(output)
                                )
                                exportedFilename = output.absolutePath
                                showExportDialog = true
                            }
                        }
                        Spacer(Modifier.width(32.dp))
                        Box {
                            Button(
                                onClick = { isTemplatesDropdownExpanded = true }
                            ) {
                                Text(
                                    templatesItems[templatesSelectedIndex],
                                    color = MaterialTheme.colors.onPrimary
                                )
                            }
                            TemplatesDropDown(
                                expanded = isTemplatesDropdownExpanded,
                                onDismissRequest = { isTemplatesDropdownExpanded = false },
                                templatesItems,
                                onItemClick = {
                                    templatesSelectedIndex = it
                                    isTemplatesDropdownExpanded = false
                                }
                            )
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
                            AboutDialog({ showAboutDialog = false },
                                { url ->
                                    if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                                            .isSupported(Desktop.Action.BROWSE)
                                    ) {
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
                    viewModel.state.inputs.mapIndexed { index, voiceField ->
                        VoiceTextField(
                            voiceField,
                            voiceField.text,
                            onChange = {
                                if (!isRecording) {
                                    viewModel.state.onTextChange(index, it)
                                }
                            },
                            onFocusChange = {
                                if (it.hasFocus) {
                                    viewModel.state.onInputFocusChanged(index)
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
                                    urlText.getStringAnnotations(
                                        tag = "URL", start = offset,
                                        end = offset
                                    )
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

    @Composable
    fun TemplatesDropDown(
        expanded: Boolean,
        onDismissRequest: () -> Unit,
        items: List<String>,
        onItemClick: (Int) -> Unit,
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    onClick = {
                        onItemClick(index)
                    }
                ) {
                    Text(item)
                }
            }
        }
    }
}