@file:OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)

package ui.main

import Navigator
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hera.voice.BuildConfig
import i18n.Messages
import i18n.text
import managers.AudioManager
import managers.FileManager
import managers.text.TextBoy
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

@Composable
@Preview
fun MainScreen(viewModel: MainScreenViewModel) {
    println("Call Main Screen")
    val stateVertical = rememberScrollState(0)
    var exportedFilename by remember { mutableStateOf(FileManager.generatedDocument.absolutePathString()) }

    var showExportDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            primary = Color.Blue,
            onPrimary = Color.White,
        )
    ) {
        Scaffold(
            modifier = Modifier.onPreviewKeyEvent {
                if (it.isCtrlPressed && it.key == Key.R && it.type == KeyEventType.KeyDown) {
                    viewModel.state.onRecordButtonClicked()
                    true
                } else {
                    false
                }
            },
            topBar = {
                TopAppBar(
                    contentColor = MaterialTheme.colors.onPrimary,
                ) {
                    TextButton(onClick = {
                        showAboutDialog = true
                    }) {
                        Text(
                            text = TextBoy.getMessage(Messages.appName),
                            modifier = Modifier.padding(start = 12.dp, end = 24.dp)
                                .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                    recordButton(viewModel.state.recordButtonText, viewModel.state.onRecordButtonClicked)
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
                    if (viewModel.state.inputDevices.isNotEmpty()) {
                        Box {
                            Button(
                                modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
                                onClick = viewModel.state.onInputDeviceButtonClicked,
                                enabled = !viewModel.state.isRecording,
                                colors = ButtonDefaults.buttonColors(
                                    disabledBackgroundColor = Color(0xFF00008b) // TODO better themeing
                                )
                            ) {
                                Text(
                                    viewModel.state.inputDevices.getOrNull(viewModel.state.selectedDeviceInputDropdownIndex)?.name.orEmpty(),
                                    color = MaterialTheme.colors.onPrimary
                                )
                            }
                            InputDevicesDropDown(
                                expanded = viewModel.state.isInputDevicesDropdownExpanded,
                                onDismissRequest = viewModel.state.onInputDeviceDismissRequest,
                                items = viewModel.state.inputDevices,
                                onItemClick = viewModel.state.onInputDeviceItemClicked,
                            )
                        }
                    }
                    TextButton(onClick = viewModel.state.onLoadNewTemplate) {
                        Text(
                            text = Messages.Menu_Load.text,
                            modifier = Modifier.padding(start = 12.dp, end = 24.dp)
                                .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
                            color = MaterialTheme.colors.onPrimary
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
                                        modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
                                        onClick = {
                                            Desktop.getDesktop().open(File(exportedFilename))
                                            showExportDialog = false
                                        }
                                    ) {
                                        Text(TextBoy.getMessage(Messages.openCta))
                                    }
                                    Spacer(modifier = Modifier.size(16.dp))
                                    Button(
                                        modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(stateVertical),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextFields(
                    viewModel.state.isRecording,
                    viewModel.state.inputs.filterNot { it.isHidden },
                    viewModel.state.onTextChange,
                    viewModel.state.onInputFocusChanged,
                )
                if (false) { // TODO enable if needed
                    AddInputButton(viewModel)
                }
            }
        }
    }
}

@Composable
private fun AddInputButton(viewModel: MainScreenViewModel) {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.fillMaxWidth(0.8f).padding(top = 32.dp)
    ) {
        OutlinedTextField(
            value = viewModel.state.addTextFieldInput,
            onValueChange = viewModel.state.onAddTextFieldChanged,
            modifier = Modifier.defaultMinSize(minWidth = 100.dp)
        )
        Spacer(Modifier.width(16.dp))
        Button(
            onClick = viewModel.state.onAddTextFieldClicked
        ) {
            Text("Add Input")
        }
    }
}

@Composable
private fun TextFields(
    isRecording: Boolean,
    inputs: List<VoiceField>,
    onTextChange: (Int, String) -> Unit,
    onFocusChange: (Int) -> Unit,
) {
    inputs.mapIndexed { index, voiceField ->
        VoiceTextField(
            voiceField,
            isRecording,
            onChange = {
                if (!isRecording) {
                    onTextChange(index, it)
                }
            },
            onFocusChange = {
                if (it.hasFocus) {
                    onFocusChange(index)
                }
            }
        )
    }
}

@Composable
fun recordButton(text: String, onTap: () -> Unit) {
    Button(
        onClick = onTap,
        modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
    ) {
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
    Button(
        onClick = onTap,
        modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
    ) {
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
    isRecording: Boolean,
    onChange: (String) -> Unit,
    onFocusChange: (FocusState) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val height = when (voiceField.size) {
        VoiceField.Size.SMALL -> Dp.Unspecified
        VoiceField.Size.MEDIUM -> 100.dp
        VoiceField.Size.LARGE -> 200.dp
    }
    OutlinedTextField(
        value = voiceField.text,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(top = 16.dp)
            .defaultMinSize(minHeight = height)
            .onFocusChanged(onFocusChange)
            .onPreviewKeyEvent {
                if (!isRecording && it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                    if (it.isShiftPressed) {
                        focusManager.moveFocus(FocusDirection.Up)
                    } else {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                    true
                } else {
                    false
                }
            },
        onValueChange = onChange,
        label = { Text(voiceField.label) },
    )
}

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
fun InputDevicesDropDown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<AudioManager.InputDevice>,
    onItemClick: (Int) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        items.forEachIndexed { index, item ->
            DropdownMenuItem(
                onClick = {
                    onItemClick(index)
                }
            ) {
                Text(item.name)
            }
        }
    }
}
