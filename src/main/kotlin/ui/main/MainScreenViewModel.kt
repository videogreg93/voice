package ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import i18n.Messages
import managers.AudioManager
import managers.TemplateManager
import managers.text.TextBoy
import managers.speech.SpeechManager
import models.Doctor
import models.Template
import models.VoiceField
import ui.base.ViewModel
import java.lang.Integer.min
import java.util.*
import kotlin.collections.ArrayList

class MainScreenViewModel(
    user: Doctor,
    private val templateManager: TemplateManager,
    private val speechManager: SpeechManager,
    initialTemplate: Template,
) : ViewModel<MainScreenViewModel.MainScreenState>() {

    private val template = initialTemplate

    private val selectedVoiceField: VoiceField
        get() = state.inputs[state.selectedInputIndex]

    // TODO avoid loading the template twice
    override var state: MainScreenState by mutableStateOf(
        MainScreenState(
            template.templateFile,
            user,
            "",
            initialTemplate.inputs,
            ::onTextChange,
            false,
            TextBoy.getMessage(Messages.record),
            0,
            0, // TODO doesn't reflect actual loaded template
            false,
            templateManager.getTemplateNames(),
            "",
            inputDevices = speechManager.getSupportedInputDevices(),
            false,
            0,
            ::onInputFocusChanged,
            ::onSpeechRecognizing,
            ::onSpeechRecognized,
            ::onDropdownItemClicked,
            ::onDropdownButtonClicked,
            ::onDropdownDismissRequest,
            ::onInputDeviceDropdownItemClicked,
            ::onInputDeviceButtonClicked,
            ::onInputDeviceDismissRequest,
            ::onRecordButtonClicked,
            ::onAddTextFieldClicked,
            ::onAddTextFieldChanged,
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

        setupCallbacks()
    }

    private fun setupCallbacks() {
        speechManager.addRecognizingListener {
            state.onSpeechRecognizing(" $it")
        }
        speechManager.addRecognizedListener {
            state.onSpeechRecognized(" $it")
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

    // Input device dropdown

    private fun onInputDeviceDropdownItemClicked(index: Int) {
        speechManager.setInputDevice(state.inputDevices[index])
        setupCallbacks()
        state = state.copy(
            selectedDeviceInputDropdownIndex = index,
            isInputDevicesDropdownExpanded = false,
        )
    }

    private fun onInputDeviceButtonClicked() {
        state = state.copy(
            isInputDevicesDropdownExpanded = true
        )
    }

    private fun onInputDeviceDismissRequest() {
        state = state.copy(
            isInputDevicesDropdownExpanded = false,
        )
    }

    private fun onRecordButtonClicked() {
        val isRecording = !state.isRecording
        val recordButtonText = if (isRecording) {
            speechManager.startContinuousRecognitionAsync()
            TextBoy.getMessage(Messages.recording)
        } else {
            speechManager.stopContinuousRecognitionAsync()
            TextBoy.getMessage(Messages.record)
        }
        state = state.copy(
            isRecording = isRecording,
            recordButtonText = recordButtonText,
        )
    }

    private fun onAddTextFieldChanged(value: String) {
        state = state.copy(
            addTextFieldInput = value
        )
    }

    private fun onAddTextFieldClicked() {
        val newList = ArrayList(state.inputs) + VoiceField(
            label = state.addTextFieldInput,
            size = VoiceField.Size.SMALL,
            id = "{${state.addTextFieldInput.lowercase(Locale.CANADA_FRENCH)}}"
        )

        state = state.copy(
            inputs = newList
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
        val addTextFieldInput: String,
        val inputDevices: List<AudioManager.InputDevice>,
        val isInputDevicesDropdownExpanded: Boolean,
        val selectedDeviceInputDropdownIndex: Int,
        val onInputFocusChanged: (Int) -> Unit,
        val onSpeechRecognizing: (String) -> Unit,
        val onSpeechRecognized: (String) -> Unit,
        val onDropdownItemClicked: (Int) -> Unit,
        val onDropdownButtonClicked: () -> Unit,
        val onDropdownDismissRequest: () -> Unit,
        val onInputDeviceItemClicked: (Int) -> Unit,
        val onInputDeviceButtonClicked: () -> Unit,
        val onInputDeviceDismissRequest: () -> Unit,
        val onRecordButtonClicked: () -> Unit,
        val onAddTextFieldClicked: () -> Unit,
        val onAddTextFieldChanged: (String) -> Unit,
    )

    companion object {

        fun create(
            user: Doctor,
            templateManager: TemplateManager,
            speechManager: SpeechManager,
        ): MainScreenViewModel {
            if (instance == null) {
                instance = MainScreenViewModel(user, templateManager, speechManager, templateManager.loadDefaultTemplate())
            }
            return instance!!
        }

        var instance: MainScreenViewModel? = null
    }
}
