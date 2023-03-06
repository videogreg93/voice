package i18n

import managers.text.TextBoy

val Messages.text: String
    get() = TextBoy.getMessage(this)
