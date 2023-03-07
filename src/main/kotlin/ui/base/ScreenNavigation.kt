package ui.base

import Navigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import i18n.Messages
import i18n.text
import managers.FileManager
import managers.TemplateManager
import managers.speech.SpeechManager
import models.Doctor
import ui.main.MainScreen
import ui.main.MainScreenViewModel
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

/**
 * Sealed class containing all possible screens. Used for navigation between screens
 */
sealed class ScreenNavigation {
    @Composable
    open fun render() {}
    object Empty: ScreenNavigation()
    object SignIn: ScreenNavigation()
    data class Main(val viewModel: MainScreenViewModel, val onExit: () -> Unit): ScreenNavigation() {
        @Composable
        override fun render() {
            MainWindow(viewModel, onExit)
        }

        @Composable
        private fun MainWindow(mainScreenViewModel: MainScreenViewModel, onExit: () -> Unit) {
            Window(
                onCloseRequest = onExit,
                title = i18n.Messages.appName.text,
                state = rememberWindowState(
                    width = 960.dp,
                    height = Dp.Unspecified,
                )
            ) {
                val viewModel by remember {
                    mutableStateOf(mainScreenViewModel)
                }
                MenuBar {
                    Menu(Messages.Menu_File.text) {
                        Item(Messages.Menu_Load.text, onClick = viewModel.state.onLoadNewTemplate)
                        Item(Messages.Menu_Exit.text, onClick = Navigator::returnToSignIn)
                    }
                }
                MainScreen(viewModel)
            }
        }
    }

    data class FilePicker(private val onFileChosen: (File?) -> Unit) : ScreenNavigation() {
        @Composable
        override fun render() {
            FileDialog(
                onCloseRequest = onFileChosen
            )
        }

        @Composable
        private fun FileDialog(
            parent: Frame? = null,
            onCloseRequest: (result: File?) -> Unit
        ) = AwtWindow(
            create = {
                object : FileDialog(parent, "Choose a file", LOAD) {
                    init {
                        directory = FileManager.mainFolder.absolutePathString()
                        setFilenameFilter { dir, name ->
                            name.contains(FileManager.echoFileExtension)
                        }
                    }

                    override fun setVisible(value: Boolean) {
                        super.setVisible(value)
                        if (value) {
                            val completeFile = file?.let {
                                Paths.get(directory, "/$it").toFile()
                            }
                            onCloseRequest(file?.let { completeFile })
                        }
                    }
                }
            },
            dispose = FileDialog::dispose
        )
    }
}
