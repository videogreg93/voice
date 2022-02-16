package models

class VoiceField(val label: String, val size: Size = Size.SMALL, val id: String) {
    enum class Size {
        SMALL, MEDIUM, LARGE
    }
}