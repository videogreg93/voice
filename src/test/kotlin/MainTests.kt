@file:OptIn(ExperimentalMaterialApi::class)

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import kotlinx.coroutines.runBlocking
import managers.user.UserManagerImpl
import org.junit.Rule
import org.junit.Test
import ui.SignInScreen
import ui.SignInViewModel

@OptIn(ExperimentalMaterialApi::class)
class MainTests {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `sign in enabled on valid user`() {
        val screen = SignInScreen({}, UserManagerImpl())
        var viewmodel: SignInViewModel? = null
        rule.setContent {
            viewmodel = screen.setup()
        }
        viewmodel?.onPermitInputChange("112233")
        rule.onNodeWithTag("SignInButton").assertIsEnabled()
    }

    @Test
    fun `sign in disabled on invalid user`() {
        runBlocking {
            val screen = SignInScreen({}, UserManagerImpl())
            var viewmodel: SignInViewModel? = null
            rule.setContent {
                viewmodel = screen.setup()
            }
            viewmodel?.onPermitInputChange("")
            rule.awaitIdle()
            rule.onNodeWithTag("SignInButton").assertIsNotEnabled()
            viewmodel?.onPermitInputChange("random")
            rule.onNodeWithTag("SignInButton").assertIsNotEnabled()
        }
    }
}