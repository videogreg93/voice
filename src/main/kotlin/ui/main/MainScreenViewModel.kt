package ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import managers.TemplateManager
import models.Doctor
import models.VoiceField
import ui.base.ViewModel

class MainScreenViewModel(
    user: Doctor,
    val templateManager: TemplateManager,
) : ViewModel<MainScreenViewModel.MainScreenState>() {

    private val template = templateManager.loadDefaultTemplate()

    // TODO avoid loading the template twice
    override var state: MainScreenState by mutableStateOf(
        MainScreenState(
            template.templateFile,
            user,
            ArrayList(templateManager.loadDefaultTemplate().inputs),
            ::onTextChange,
            0,
            ::onInputFocusChanged,
            ::onSpeechRecognizing,
            ::onSpeechRecognized,
        )
    )

    private var startingText = ""

    init {
        // Prefill username is correct input
        state.inputs.indexOfFirst { it.isUsername }.takeUnless { it == -1 }?.let {
            state.inputs[it] = state.inputs[it].copy(text = user.givenName)
        }
    }

    private fun onTextChange(index: Int, newValue: String) {
        val newList = ArrayList(state.inputs)
        newList[index] = state.inputs[index].copy(text = newValue)
        state = state.copy(inputs = newList)
    }

    private fun onInputFocusChanged(index: Int) {
        state = state.copy(selectedInputIndex = index)
    }

    private fun onSpeechRecognizing(value: String) {
        val newField = state.inputs[state.selectedInputIndex].copy(text = startingText + value)
        val newList = ArrayList(state.inputs)
        newList[state.selectedInputIndex] = newField
        state = state.copy(inputs = newList)
    }

    private fun onSpeechRecognized(value: String) {
        val newField = state.inputs[state.selectedInputIndex].copy(text = startingText + value)
        val newList = ArrayList(state.inputs)
        newList[state.selectedInputIndex] = newField
        startingText = newField.text
        state = state.copy(inputs = newList)
    }

    data class MainScreenState(
        val templateFile: String,
        val currentUser: Doctor,
        val inputs: ArrayList<VoiceField>,
        val onTextChange: (Int, String) -> Unit,
        val selectedInputIndex: Int,
        val onInputFocusChanged: (Int) -> Unit,
        val onSpeechRecognizing: (String) -> Unit,
        val onSpeechRecognized: (String) -> Unit
    )
}