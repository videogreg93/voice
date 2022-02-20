package managers

import models.Doctor

class UserManager {

    // Dummy users
    private val users = listOf(
        Doctor("112233", "Test", "User"),
        Doctor("111111", "AnotherTest", "User"),
        Doctor("04260", "Karl", "Fournier"))

    fun validateUser(permit: String): Boolean {
        return users.any {
            it.permit == permit
        }
    }

    fun getUser(permit: String): Doctor? {
        return users.firstOrNull { it.permit == permit }
    }
}