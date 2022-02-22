package ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import i18n.Messages
import managers.SpeechManager
import managers.TemplateManager
import managers.TextBoy
import models.Doctor
import models.VoiceField
import ui.base.ViewModel

class MainScreenViewModel(
    user: Doctor,
    val templateManager: TemplateManager,
    val speechManager: SpeechManager,
) : ViewModel<MainScreenViewModel.MainScreenState>() {

    private val template = templateManager.loadDefaultTemplate()

    private val selectedVoiceField: VoiceField
        get() = state.inputs[state.selectedInputIndex]

    // TODO avoid loading the template twice
    override var state: MainScreenState by mutableStateOf(
        MainScreenState(
            template.templateFile,
            user,
            ::onTextChange,
            0,
            ::onInputFocusChanged,
            ::onSpeechRecognizing,
            ::onSpeechRecognized,
            ::onDropdownItemClicked,
            ::onRecordButtonClicked,
        ).apply {
            inputs = templateManager.loadDefaultTemplate().inputs
            templateNames = templateManager.getTemplateNames()
        }
    )

    private var startingText = ""

    init {
        // Prefill username is correct input
        state.inputs.indexOfFirst { it.isUsername }.takeUnless { it == -1 }?.let {
            state.inputs[it].text = user.givenName
        }

        speechManager.recognizer.recognizing.addEventListener { _, e ->
            state.onSpeechRecognizing(" " + e.result.text)
        }
        speechManager.recognizer.recognized.addEventListener { s, e ->
            state.onSpeechRecognized(" " + e.result.text)
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

    private fun onDropdownItemClicked(index: Int) {
        state.selectedDropdownIndex = index
        state.isDropdownExpanded = false
        state.inputs = templateManager.loadTemplate(state.templateNames[index]).inputs
    }

    private fun onRecordButtonClicked() {
        state.isRecording = !state.isRecording
        state.recordButtonText = if (state.isRecording) {
            speechManager.recognizer.startContinuousRecognitionAsync()
            TextBoy.getMessage(Messages.recording)
        } else {
            speechManager.recognizer.stopContinuousRecognitionAsync()
            TextBoy.getMessage(Messages.record)
        }
    }

    data class MainScreenState(
        val templateFile: String,
        val currentUser: Doctor,
        val onTextChange: (Int, String) -> Unit,
        var selectedInputIndex: Int,
        val onInputFocusChanged: (Int) -> Unit,
        val onSpeechRecognizing: (String) -> Unit,
        val onSpeechRecognized: (String) -> Unit,
        val onDropdownItemClicked: (Int) -> Unit,
        val onRecordButtonClicked: () -> Unit,
    ) {
        var templateNames: List<String> = mutableStateListOf()
        var inputs: List<VoiceField> = mutableStateListOf()
        var isRecording by mutableStateOf(false)
        var selectedDropdownIndex by mutableStateOf(0)
        var isDropdownExpanded by mutableStateOf(false)
        var recordButtonText by mutableStateOf(TextBoy.getMessage(Messages.record))
    }
}