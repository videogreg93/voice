package ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import managers.TemplateManager
import models.Doctor
import models.VoiceField
import ui.base.ViewModel

class MainScreenViewModel(
    user: Doctor,
    val templateManager: TemplateManager,
) : ViewModel<MainScreenViewModel.MainScreenState>() {

    private val template = templateManager.loadDefaultTemplate()

    private val selectedVoiceField: VoiceField
        get() = state.inputs[state.selectedInputIndex]

    // TODO avoid loading the template twice
    override var state: MainScreenState by mutableStateOf(
        MainScreenState(
            template.templateFile,
            user,
            mutableStateListOf(*templateManager.loadDefaultTemplate().inputs.toTypedArray()),
            ::onTextChange,
            0,
            false,
            ::onInputFocusChanged,
            ::onSpeechRecognizing,
            ::onSpeechRecognized,
        )
    )

    private var startingText = ""

    init {
        // Prefill username is correct input
        state.inputs.indexOfFirst { it.isUsername }.takeUnless { it == -1 }?.let {
            state.inputs[it].text = user.givenName
        }
    }

    private fun onTextChange(index: Int, newValue: String) {
        state.inputs[index].text = newValue
        startingText = selectedVoiceField.text
    }

    private fun onInputFocusChanged(index: Int) {
        state.selectedInputIndex = index
        startingText = selectedVoiceField.text
    }

    private fun onSpeechRecognizing(value: String) {
        selectedVoiceField.text = startingText + value
    }

    private fun onSpeechRecognized(value: String) {
        selectedVoiceField.text = startingText + value
        startingText = selectedVoiceField.text
    }

    data class MainScreenState(
        val templateFile: String,
        val currentUser: Doctor,
        val inputs: SnapshotStateList<VoiceField>,
        val onTextChange: (Int, String) -> Unit,
        var selectedInputIndex: Int,
        var isRecording: Boolean,
        val onInputFocusChanged: (Int) -> Unit,
        val onSpeechRecognizing: (String) -> Unit,
        val onSpeechRecognized: (String) -> Unit
    )
}