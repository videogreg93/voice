package managers

import java.util.prefs.Preferences

object Prefs {

    private val prefs by lazy { Preferences.userRoot().node(this::class.java.name) }

    fun setLastUser(user: String) {
        prefs.put(LAST_USER_KEY, user)
    }

    fun getLastUser(): String {
        return prefs.get(LAST_USER_KEY, "")
    }

    private const val LAST_USER_KEY = "LAST_USER"
}