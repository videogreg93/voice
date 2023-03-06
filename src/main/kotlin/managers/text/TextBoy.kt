package managers.text

import i18n.Messages
import java.text.MessageFormat
import java.util.*


object TextBoy {
    private const val MESSAGES_KEY = "i18n/messages"
    private val bundle: ResourceBundle by lazy { ResourceBundle.getBundle(MESSAGES_KEY) }
    var locale: Locale
        get() = Locale.getDefault()
        set(l) {
            Locale.setDefault(l)
        }

    fun isSupported(l: Locale): Boolean {
        return Locale.getAvailableLocales().contains(l)
    }

    fun getMessage(key: Messages): String {
        return bundle.getString(key.value)
    }

    fun getMessage(key: Messages, vararg arguments: Any): String {
        return MessageFormat.format(getMessage(key), *arguments)
    }
}
