import java.io.InputStream
import java.net.URI

fun getResourceAsText(path: String): String? =
    object {}.javaClass.getResource(path)?.readText()

fun getResource(path: String): InputStream? =
    object {}.javaClass.getResourceAsStream(path)