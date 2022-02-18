package models

class VoiceField(val label: String, val size: Size = Size.SMALL, val id: String, val isFileName: Boolean = false) {
    enum class Size {
        SMALL, MEDIUM, LARGE
    }
}