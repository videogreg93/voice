package models

class VoiceField(val label: String, val size: Size = Size.SMALL) {
    enum class Size {
        SMALL, MEDIUM, LARGE
    }
}