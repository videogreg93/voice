package managers

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Template
import java.io.File

/**
 * Responsible for fetching different templates. Templates are a list of inputs and parameters
 */
class TemplateManager {

    fun loadDefaultTemplate() = loadTemplate(DEFAULT_FILENAME)

    fun loadTemplate(name: String): Template {
        val file = File(FileManager.mainFolder.toFile(), "$name$FILE_EXTENSION")
        val fileText = file.readText()
        return Json.decodeFromString(fileText)
    }

    fun saveTemplate(name: String, template: Template) {
        val templateJson = Json.encodeToString(template)
        val file = File(FileManager.mainFolder.toFile(), "$name$FILE_EXTENSION")
        file.writeText(templateJson)
        println(file.absolutePath)
    }

    companion object {
        private const val FILE_EXTENSION = ".json"
        private const val DEFAULT_FILENAME = "protocolOperatoire"
    }
}