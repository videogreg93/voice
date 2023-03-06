import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import i18n.Messages
import managers.FileManager
import managers.speech.SpeechManagerImpl
import managers.TemplateManager
import managers.TextBoy
import managers.UserManager
import models.Doctor
import org.apache.commons.io.FileUtils
import ui.main.MainScreen
import ui.SignInScreen
import ui.main.MainScreenViewModel
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists


@ExperimentalMaterialApi
fun main() = application {
    if (!FileManager.mainFolder.exists()) Files.createDirectories(FileManager.mainFolder)
    // Copy all files from resource folder to the AppRoaming folder
    val filenames = listOf("BEM.json", "protocolOperatoire.json", "getDeviceIds.exe")
    filenames.forEach { filename ->
        getResource(filename)?.let { inputStream ->
            val newFile = Paths.get(FileManager.mainFolder.absolutePathString(), "/$filename").toFile()
            FileUtils.copyInputStreamToFile(inputStream, newFile)
        }
    }
    var showMainWindow by remember { mutableStateOf(false) }
    var showSignInWindow by remember { mutableStateOf(true) }
    var currentUser: Doctor? by remember { mutableStateOf(null) } // TODO make non optional
    val userManager = UserManager()
    if (showSignInWindow) {
        Window(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(
                width = 600.dp,
                height = 350.dp,
            ),
            title = "Sign in"
        ) {
            SignInScreen(
                onSignInSuccessful = {
                    showMainWindow = true
                    showSignInWindow = false
                    currentUser = it
                },
                userManager = userManager,
            ).setup()
        }
    }

    if (showMainWindow) {
        MainWindow(currentUser!!) { exitApplication() }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MainWindow(user: Doctor, onExit: () -> Unit) {
    Window(
        onCloseRequest = onExit,
        title = TextBoy.getMessage(Messages.appName),
        state = rememberWindowState(
            width = 960.dp,
            height = 800.dp,
        )
    ) {
        val viewModel by remember { mutableStateOf(MainScreenViewModel.create(user, TemplateManager(), SpeechManagerImpl.instance)) }
        MainScreen(viewModel)
    }
}

