package com.propellerads.sdk.repository

import android.content.Context
import java.util.*

interface IUsedIdProvider {
    fun getUserId(): String
}

class UsedIdProvider(
    context: Context,
) : IUsedIdProvider {

    private companion object {
        const val USER_ID_PREF_NAME = "com.propellerads.sdk.prefs"
        const val USER_ID_PREF_KEY = "USER_ID_PREF_KEY"
    }

    @Volatile
    private var userId: String = ""

    private val sharedPref = context.getSharedPreferences(
        USER_ID_PREF_NAME,
        Context.MODE_PRIVATE
    )

    init {
        this.userId = readUserId()
            ?: generateUserId().also { generatedUUID ->
                writeUserId(generatedUUID)
            }
    }

    private fun generateUserId() = UUID.randomUUID().toString()
        .replace("-", "")

    private fun readUserId() = sharedPref.getString(USER_ID_PREF_KEY, null)

    private fun writeUserId(uuid: String) {
        sharedPref
            .edit()
            .putString(USER_ID_PREF_KEY, uuid)
            .apply()
    }

    override fun getUserId(): String = userId
}