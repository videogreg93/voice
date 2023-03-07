package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import api.UpdateClient
import com.hera.voice.BuildConfig
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import i18n.Messages
import i18n.text
import kotlinx.coroutines.runBlocking
import managers.AudioManager
import managers.Prefs
import managers.UserManager
import managers.text.TextBoy
import models.Doctor
import java.awt.Cursor

@ExperimentalComposeUiApi
class SignInScreen(
    private val onSignInSuccessful: (Doctor) -> Unit,
    private val userManager: UserManager,
) {

    init {
        AudioManager()

        println(AudioConfig.fromMicrophoneInput(""))
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun setup() {
        var showUpdateDialog by remember { mutableStateOf(true) }
        val version = runBlocking {
            UpdateClient.getLatestReleaseVersion()
        }
        if (version != BuildConfig.APP_VERSION && showUpdateDialog) {
            AlertDialog(
                title = {
                    Text("New Version Available")
                },
                text = {
                    Column(modifier = Modifier.padding(16.dp).width(254.dp)) {
                        Text("Your Version  : ${BuildConfig.APP_VERSION}")
                        Text("Latest Version: ${version}")
                    }
                },
                buttons = {
                    Row(modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)) {
                        Button(
                            modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary
                            ),
                            onClick = {
                                showUpdateDialog = false
                            }
                        ) {
                            Text("Update")
                        }
                        Spacer(Modifier.width(32.dp))
                        Button(
                            modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.onPrimary
                            ),
                            onClick = {
                                showUpdateDialog = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                },
                onDismissRequest = {},
            )
        }
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
                    modifier = Modifier.fillMaxWidth()
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
    }

    @Composable
    private fun PracticeNumberInput(input: String, onValueChange: (String) -> Unit) {
        OutlinedTextField(
            value = input,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(top = 16.dp),
            onValueChange = onValueChange,
            label = { Text(TextBoy.getMessage(Messages.practiceNumberLabel)) },
        )
    }

    @ExperimentalComposeUiApi
    @Composable
    private fun SignInButton(validation: () -> Boolean, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            enabled = validation(),
            modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
        ) {
            Text(Messages.signIn.text)
        }
    }
}
