package com.example.settings

import kotlin.random.Random

class UserData (userName: String?, personalCode: Int?) {
    val userName: String?
    val userCode: Int?

    init {
        this.userName = userName
        this.userCode = personalCode?.let {personalCode} ?: designateUserCode()
    }

    private fun designateUserCode(): Int{
        return Random.nextInt(1000, 9999)
    }
}