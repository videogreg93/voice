package ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import managers.Prefs
import managers.user.UserManager
import models.Doctor
import ui.base.ViewModel

class SignInViewModel(
    private val userManager: UserManager,
) : ViewModel<SignInViewModel.SignInState>() {
    override var state: SignInState by
    mutableStateOf(
        SignInState(
            "",
            null,
            Prefs.getLastUser().isNotEmpty(),
            ::onTapRememberMe,
            ::onPermitInputChange,
            ::signInEnabled,
        )
    )

    init {
        onPermitInputChange(Prefs.getLastUser())
    }

    private fun onTapRememberMe(value: Boolean) {
        state = state.copy(isRememberMeChecked = value)
    }

    fun onPermitInputChange(input: String) {
        val user = userManager.getUser(input)
        state = state.copy(
            permitNumber = input,
            user = user
        )
    }

    private fun signInEnabled(): Boolean {
        return state.user != null
    }

    data class SignInState(
        val permitNumber: String,
        val user: Doctor?,
        val isRememberMeChecked: Boolean,
        val onTapRememberMe: (Boolean) -> Unit,
        val onPermitInputChange: (String) -> Unit,
        val signInEnabled: () -> Boolean,
    )
}

