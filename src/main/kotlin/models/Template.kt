package models

import kotlinx.serialization.Serializable

@Serializable
data class Template(
    val inputs: List<VoiceField>
)