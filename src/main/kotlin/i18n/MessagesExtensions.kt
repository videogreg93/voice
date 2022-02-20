package i18n

import managers.TextBoy

val Messages.text: String
    get() = TextBoy.getMessage(this)