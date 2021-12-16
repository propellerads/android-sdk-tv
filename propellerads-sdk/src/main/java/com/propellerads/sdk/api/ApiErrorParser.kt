package com.propellerads.sdk.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.propellerads.sdk.repository.IErrorParser
import com.propellerads.sdk.repository.ParsedHttpException
import retrofit2.HttpException

internal class ApiErrorParser : IErrorParser {

    private companion object {
        const val ERROR_MSG_FIELD = "error_message"
    }

    private val jsonParser = JsonParser()

    override fun parse(httpException: HttpException): ParsedHttpException {
        val errMessage = try {
            val errorBody = httpException.response()?.errorBody()?.string()
            val errJson = jsonParser.parse(errorBody) as JsonObject
            errJson.get(ERROR_MSG_FIELD).asString
        } catch (e: Exception) {
            httpException.message
                ?: httpException.message()
                ?: "Unknown API exception"
        }
        return ParsedHttpException(errMessage)
    }
}