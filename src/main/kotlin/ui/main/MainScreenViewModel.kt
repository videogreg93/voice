package ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import i18n.Messages
import managers.SpeechManager
import managers.TemplateManager
import managers.TextBoy
import models.Doctor
import models.VoiceField
import ui.base.ViewModel
import java.lang.Integer.min

class MainScreenViewModel(
    user: Doctor,
    private val templateManager: TemplateManager,
    private val speechManager: SpeechManager,
) : ViewModel<MainScreenViewModel.MainScreenState>() {

    private val template = templateManager.loadDefaultTemplate()

    private val selectedVoiceField: VoiceField
        get() = state.inputs[state.selectedInputIndex]

    // TODO avoid loading the template twice
    override var state: MainScreenState by mutableStateOf(
        MainScreenState(
            template.templateFile,
            user,
            "",
            templateManager.loadDefaultTemplate().inputs,
            ::onTextChange,
            false,
            TextBoy.getMessage(Messages.record),
            0,
            0,
            false,
            templateManager.getTemplateNames(),
            ::onInputFocusChanged,
            ::onSpeechRecognizing,
            ::onSpeechRecognized,
            ::onDropdownItemClicked,
            ::onDropdownButtonClicked,
            ::onDropdownDismissRequest,
            ::onRecordButtonClicked,
        )
    )

    init {
        // Prefill username with correct input
        // TODO make this work when changing template.
        state.inputs.filter { it.isUsername }.map { state.inputs.indexOf(it) }.filter { it != -1 }.forEach {
            val newList = ArrayList(state.inputs)
            newList[it] = newList[it].copy(text = user.givenName)
            state = state.copy(
                inputs = newList
            )
        }
        speechManager.recognizer.recognizing.removeEventListener { any, speechRecognitionEventArgs -> }

        speechManager.recognizer.recognizing.addEventListener { _, e ->
            state.onSpeechRecognizing(" " + e.result.text)
        }
        speechManager.recognizer.recognized.addEventListener { s, e ->
            state.onSpeechRecognized(" " + e.result.text)
        }
    }

    private fun onTextChange(index: Int, newValue: String) {
        val newList = ArrayList(state.inputs)
        newList[index] = state.inputs[index].copy(text = newValue)
        state = state.copy(
            inputs = newList,
            startingText = selectedVoiceField.text
        )
    }

    private fun onInputFocusChanged(index: Int) {
        state = state.copy(
            selectedInputIndex = index,
            startingText = state.inputs[index].text
        )
    }

    private fun onSpeechRecognizing(value: String) {
        val newList = ArrayList(state.inputs)
        newList[state.selectedInputIndex] = newList[state.selectedInputIndex].copy(text = state.startingText + value)
        state = state.copy(
            inputs = newList
        )
    }

    private fun onSpeechRecognized(value: String) {
        val newList = ArrayList(state.inputs)
        newList[state.selectedInputIndex] = newList[state.selectedInputIndex].copy(text = state.startingText + value)
        state = state.copy(
            inputs = newList,
            startingText = state.inputs[state.selectedInputIndex].text
        )
    }

    private fun onDropdownItemClicked(index: Int) {
        val newTemplate = templateManager.loadTemplate(state.templateNames[index])
        val newInputs = newTemplate.inputs
        state = state.copy(
            selectedDropdownIndex = index,
            inputs = templateManager.loadTemplate(state.templateNames[index]).inputs,
            isDropdownExpanded = false,
            startingText = "",
            selectedInputIndex = min(state.selectedInputIndex, newInputs.size),
            templateFile = newTemplate.templateFile
        )
    }

    private fun onDropdownButtonClicked() {
        state = state.copy(
            isDropdownExpanded = true
        )
    }

    private fun onDropdownDismissRequest() {
        state = state.copy(
            isDropdownExpanded = false,
        )
    }

    private fun onRecordButtonClicked() {
        val isRecording = !state.isRecording
        val recordButtonText = if (isRecording) {
            speechManager.recognizer.startContinuousRecognitionAsync()
            TextBoy.getMessage(Messages.recording)
        } else {
            speechManager.recognizer.stopContinuousRecognitionAsync()
            TextBoy.getMessage(Messages.record)
        }
        state = state.copy(
            isRecording = isRecording,
            recordButtonText = recordButtonText,
        )
    }

    data class MainScreenState(
        val templateFile: String,
        val currentUser: Doctor,
        val startingText: String,
        val inputs: List<VoiceField>,
        val onTextChange: (Int, String) -> Unit,
        val isRecording: Boolean,
        val recordButtonText: String,
        val selectedInputIndex: Int,
        val selectedDropdownIndex: Int,
        val isDropdownExpanded: Boolean,
        val templateNames: List<String>,
        val onInputFocusChanged: (Int) -> Unit,
        val onSpeechRecognizing: (String) -> Unit,
        val onSpeechRecognized: (String) -> Unit,
        val onDropdownItemClicked: (Int) -> Unit,
        val onDropdownButtonClicked: () -> Unit,
        val onDropdownDismissRequest: () -> Unit,
        val onRecordButtonClicked: () -> Unit,
    )

    companion object {

        fun create(
            user: Doctor,
            templateManager: TemplateManager,
            speechManager: SpeechManager,
        ): MainScreenViewModel {
            if (instance == null) {
                instance = MainScreenViewModel(user, templateManager, speechManager)
            }
            return instance!!
        }

        var instance: MainScreenViewModel? = null
    }
}