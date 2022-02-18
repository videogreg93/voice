import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import managers.FileManager
import managers.SpeechManager
import ui.MainScreen
import java.nio.file.Files
import kotlin.io.path.exists


@ExperimentalMaterialApi
fun main() = application {
    if (!FileManager.mainFolder.exists()) Files.createDirectories(FileManager.mainFolder)
    Window(onCloseRequest = ::exitApplication) {
        MainScreen(SpeechManager()).App()
    }
}
