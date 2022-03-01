package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import i18n.Messages
import i18n.text
import managers.Prefs
import managers.TextBoy
import managers.user.UserManager
import models.Doctor

class SignInScreen(
    private val onSignInSuccessful: (Doctor) -> Unit,
    private val userManager: UserManager,
) {


    @Composable
    fun setup(): SignInViewModel {

        val viewModel by remember { mutableStateOf(SignInViewModel(userManager)) }
        MaterialTheme(
            colors = MaterialTheme.colors.copy(
                primary = Color.Blue,
                onPrimary = Color.White,
            )
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        contentColor = MaterialTheme.colors.onPrimary
                    ) {
                        Text(
                            text = TextBoy.getMessage(Messages.appName),
                            modifier = Modifier.padding(start = 12.dp, end = 24.dp),
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                },
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().testTag("SignInColumn")
                ) {
                    val user = viewModel.state.user
                    if (user != null) {
                        Spacer(Modifier.padding(top = 12.dp))
                        Text(user.givenName)
                        Text("#${user.permit}")
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(viewModel.state.isRememberMeChecked, viewModel.state.onTapRememberMe)
                            Text(Messages.rememberMe.text)
                        }
                    }
                    PracticeNumberInput(
                        input = viewModel.state.permitNumber,
                        onValueChange = viewModel.state.onPermitInputChange
                    )
                    SignInButton(
                        validation = viewModel.state.signInEnabled,
                        onClick = {
                            if (viewModel.state.isRememberMeChecked) {
                                Prefs.setLastUser(viewModel.state.permitNumber)
                            } else {
                                Prefs.setLastUser("")
                            }
                            viewModel.state.user?.let(onSignInSuccessful)
                                ?: println("Could not find user ${viewModel.state.permitNumber}")
                        }
                    )
                }
            }
        }

        return viewModel
    }

    @Composable
    private fun PracticeNumberInput(input: String, onValueChange: (String) -> Unit) {
        OutlinedTextField(
            value = input,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(top = 16.dp)
                .testTag("SignInField"),
            onValueChange = onValueChange,
            label = { Text(TextBoy.getMessage(Messages.practiceNumberLabel)) },
        )
    }

    @Composable
    private fun SignInButton(validation: () -> Boolean, onClick: () -> Unit) {
        Button(
            modifier = Modifier.testTag("SignInButton"),
            onClick = onClick,
            enabled = validation(),
        ) {
            Text(Messages.signIn.text)
        }
    }
}