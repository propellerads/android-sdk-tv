package com.propellerads.sdk.repository

import android.content.Context
import java.util.*

interface IUsedDataProvider {
    fun getUserId(): String
    fun getUserCreationTime(): Long
}

class UsedDataProvider(
    context: Context,
) : IUsedDataProvider {

    private companion object {
        const val USER_ID_PREF_NAME = "com.propellerads.sdk.prefs"
        const val USER_ID_PREF_KEY = "USER_ID_PREF_KEY"
        const val USER_TIMESTAMP_PREF_KEY = "USER_TIMESTAMP_PREF_KEY"
    }

    @Volatile
    private var userId: String = ""

    @Volatile
    private var userTimestamp: Long = 0L

    private val sharedPref = context.getSharedPreferences(
        USER_ID_PREF_NAME,
        Context.MODE_PRIVATE
    )

    init {
        obtainUserData()
    }

    private fun obtainUserData() {
        val storedUserId = readUserId()
        val storedTimestamp = readUserTimestamp()
        if (storedUserId == null) {
            val generatedUserId = generateUserId()
            val generatedTimestamp = System.currentTimeMillis() / 1000
            writeUserData(generatedUserId, generatedTimestamp)
            this.userId = generatedUserId
            this.userTimestamp = generatedTimestamp
        } else {
            this.userId = storedUserId
            this.userTimestamp = storedTimestamp
        }
    }

    private fun generateUserId() = UUID.randomUUID().toString()
        .replace("-", "")

    private fun readUserId() = sharedPref.getString(USER_ID_PREF_KEY, null)
    private fun readUserTimestamp() = sharedPref.getLong(USER_TIMESTAMP_PREF_KEY, 0L)

    private fun writeUserData(uuid: String, timestamp: Long) {
        sharedPref
            .edit()
            .putString(USER_ID_PREF_KEY, uuid)
            .putLong(USER_TIMESTAMP_PREF_KEY, timestamp)
            .apply()
    }

    override fun getUserId(): String = userId

    override fun getUserCreationTime(): Long = userTimestamp
}