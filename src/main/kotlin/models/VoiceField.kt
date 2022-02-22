package models

import kotlinx.serialization.Serializable

@Serializable
data class VoiceField(
    val label: String,
    val size: Size = Size.SMALL,
    val id: String,
    val isFileName: Boolean = false,
    val isUsername: Boolean = false,
    val isPermitNumber: Boolean = false,
    val text: String = "",
) {
    enum class Size {
        SMALL, MEDIUM, LARGE
    }
}