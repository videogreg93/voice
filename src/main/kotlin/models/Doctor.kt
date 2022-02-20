package models

data class Doctor(
    val permit: String,
    val firstName: String,
    val lastName: String,
) {
    val givenName: String
        get() = "$firstName $lastName"
}
