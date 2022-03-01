package managers.user

import models.Doctor

class UserManagerImpl: UserManager {

    // Dummy users
    private val users = listOf(
        Doctor("112233", "Test", "User"),
        Doctor("111111", "AnotherTest", "User"),
        Doctor("04260", "Karl", "Fournier")
    )

    override fun getUser(permit: String): Doctor? {
        return users.firstOrNull { it.permit == permit }
    }
}