package managers

import java.nio.file.Paths
import javax.swing.filechooser.FileSystemView
import kotlin.io.path.absolutePathString

object FileManager {
    val mainFolder by lazy { Paths.get(System.getenv("APPDATA"), "/Voice") }
    val generatedDocument by lazy { Paths.get(mainFolder.absolutePathString(), "/generated.docx") }
    val template by lazy { Paths.get(mainFolder.absolutePathString(), "/template.docx").toFile() }
    val myDocuments by lazy { FileSystemView.getFileSystemView().defaultDirectory.path }
}