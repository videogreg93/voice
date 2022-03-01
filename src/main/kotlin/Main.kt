import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import managers.FileManager
import managers.SpeechManager
import managers.TemplateManager
import managers.user.UserManager
import managers.user.UserManagerImpl
import models.Doctor
import ui.main.MainScreen
import ui.SignInScreen
import ui.main.MainScreenViewModel
import java.nio.file.Files
import kotlin.io.path.exists


@ExperimentalMaterialApi
fun main() = application {
    mainSetup(::exitApplication)
}

@Composable
fun mainSetup(onExit: () -> Unit) {
    if (!FileManager.mainFolder.exists()) Files.createDirectories(FileManager.mainFolder)
    var showMainWindow by remember { mutableStateOf(false) }
    var showSignInWindow by remember { mutableStateOf(true) }
    var currentUser: Doctor? by remember { mutableStateOf(null) } // TODO make non optional
    val userManager = UserManagerImpl()
    if (showSignInWindow) {
        Window(
            onCloseRequest = onExit,
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
        MainWindow(currentUser!!, onExit)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MainWindow(user: Doctor, onExit: () -> Unit) {
    Window(
        onCloseRequest = onExit,
        title = "Voice"
    ) {
        val viewModel by remember { mutableStateOf(MainScreenViewModel.create(user, TemplateManager(), SpeechManager.instance)) }
        MainScreen(viewModel)
    }
}

