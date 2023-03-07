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
import ui.main.MainScreenViewModel
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

@OptIn(ExperimentalComposeUiApi::class)
object Navigator {
    var screen: ScreenNavigation by mutableStateOf(ScreenNavigation.SignIn)
    lateinit var onExit: () -> Unit

    fun loadTemplate(doctor: Doctor, onExit: () -> Unit) {
        screen = ScreenNavigation.FilePicker { file ->
            if (file != null) {
                screen = ScreenNavigation.Main(
                    MainScreenViewModel.create(
                        doctor,
                        TemplateManager(),
                        SpeechManagerImpl.instance,
                        file
                    ),
                    onExit,
                )
            } else {
                ScreenNavigation.SignIn
            }
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
fun main() = application {
    Navigator.onExit = ::exitApplication
    if (!FileManager.mainFolder.exists()) Files.createDirectories(FileManager.mainFolder)
    // Copy all files from resource folder to the AppRoaming folder
    val filenames = listOf("BEM.json", "protocolOperatoire.json", "getDeviceIds.exe")
    filenames.forEach { filename ->
        getResource(filename)?.let { inputStream ->
            val newFile = Paths.get(FileManager.mainFolder.absolutePathString(), "/$filename").toFile()
            FileUtils.copyInputStreamToFile(inputStream, newFile)
        }
    }
    val userManager = UserManager()
    when (val screen = Navigator.screen) {
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
                    onSignInSuccessful = { doctor ->
                        Navigator.screen = ScreenNavigation.FilePicker { file ->
                            Navigator.screen = if (file != null) {
                                ScreenNavigation.Main(
                                    MainScreenViewModel.create(
                                        doctor,
                                        TemplateManager(),
                                        SpeechManagerImpl.instance,
                                        file
                                    ),
                                    ::exitApplication,
                                )
                            } else {
                                ScreenNavigation.SignIn
                            }
                        }
                    },
                    userManager = userManager,
                ).setup()
            }
        }

        else -> screen.render()
    }
}

