import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import i18n.Messages
import i18n.text
import managers.FileManager
import managers.TemplateManager
import managers.UserManager
import managers.speech.SpeechManagerImpl
import models.Doctor
import org.apache.commons.io.FileUtils
import ui.SignInScreen
import ui.base.ScreenNavigation
import ui.main.MainScreen
import ui.main.MainScreenViewModel
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists


@ExperimentalComposeUiApi
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
    var currentScreen: ScreenNavigation by remember { mutableStateOf(ScreenNavigation.SignIn) }
    val userManager = UserManager()
    when (val screen = currentScreen) {
        ScreenNavigation.SignIn -> {
            Window(
                onCloseRequest = ::exitApplication,
                state = rememberWindowState(
                    width = 600.dp,
                    height = 350.dp,
                ),
                title = Messages.signIn.text
            ) {
                SignInScreen(
                    onSignInSuccessful = {
                        currentScreen = ScreenNavigation.Main(it)
                    },
                    userManager = userManager,
                ).setup()
            }
        }
        is ScreenNavigation.Main -> {
            MainWindow(screen.user) { exitApplication() }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MainWindow(user: Doctor, onExit: () -> Unit) {
    Window(
        onCloseRequest = onExit,
        title = Messages.appName.text,
        state = rememberWindowState(
            width = 960.dp,
            height = 800.dp,
        )
    ) {
        val viewModel by remember {
            mutableStateOf(
                MainScreenViewModel.create(
                    user,
                    TemplateManager(),
                    SpeechManagerImpl.instance
                )
            )
        }
        MainScreen(viewModel)
    }
}

