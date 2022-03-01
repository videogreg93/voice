package managers.user

import models.Doctor

interface UserManager {
    fun getUser(permit: String): Doctor?
}