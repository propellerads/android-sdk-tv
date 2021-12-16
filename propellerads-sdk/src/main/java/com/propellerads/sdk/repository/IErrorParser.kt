package com.propellerads.sdk.repository

import retrofit2.HttpException

interface IErrorParser {
    fun parse(httpException: HttpException): ParsedHttpException
}

class ParsedHttpException(
    message: String
) : Exception(message)