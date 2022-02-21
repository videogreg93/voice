package ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import models.Doctor
import models.VoiceField
import ui.base.ViewModel

class MainScreenViewModel(user: Doctor) : ViewModel<MainScreenViewModel.MainScreenState>() {

    private val defaultInputs = listOf(
        VoiceField("Date de l'Opération", VoiceField.Size.SMALL, "{id_date}"),
        VoiceField("Nom du patient", VoiceField.Size.SMALL, "{id_nom_patient}", true),
        VoiceField("Numéro de dossier", VoiceField.Size.SMALL, "{id_number}"),
        VoiceField("NAM", VoiceField.Size.SMALL, "{id_nam}"),
        VoiceField("DDN", VoiceField.Size.SMALL, "{id_ddn}"),
        VoiceField("Âge", VoiceField.Size.SMALL, "{id_age}"),
        VoiceField("No Visite", VoiceField.Size.SMALL, "{id_visite}"),
        VoiceField("Diagnostic Préopératoire", VoiceField.Size.MEDIUM, "{id_diagnostic_preoperatoire}"),
        VoiceField("Diagnostic Postopératoire", VoiceField.Size.MEDIUM, "{id_diagnostic_postoperatoire}"),
        VoiceField("Protocole Opératoire", VoiceField.Size.LARGE, "{id_protocole_operatoire}"),
        VoiceField("Nom du médecin", VoiceField.Size.SMALL, "{id_nom_medecin}", isUsername = true),
        VoiceField("Département", VoiceField.Size.SMALL, "{id_nom_departement}"),
    )

    override var state: MainScreenState by mutableStateOf(
        MainScreenState(
            user,
            ArrayList(
                defaultInputs
            ),
            ::onTextChange,
            0,
            ::onInputFocusChanged,
            ::onSpeechRecognizing,
            ::onSpeechRecognized
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
        val currentUser: Doctor,
        val inputs: ArrayList<VoiceField>,
        val onTextChange: (Int, String) -> Unit,
        val selectedInputIndex: Int,
        val onInputFocusChanged: (Int) -> Unit,
        val onSpeechRecognizing: (String) -> Unit,
        val onSpeechRecognized: (String) -> Unit
    )
}