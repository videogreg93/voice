package ui.base

import models.Doctor

/**
 * Sealed class containing all possible screens. Used for navigation between screens
 */
sealed class ScreenNavigation {
    object SignIn: ScreenNavigation()
    data class Main(val user: Doctor): ScreenNavigation()
}
