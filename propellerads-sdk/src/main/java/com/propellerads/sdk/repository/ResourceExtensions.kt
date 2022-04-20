package com.propellerads.sdk.repository

import com.google.gson.JsonParseException
import java.io.EOFException
import java.net.ConnectException
import java.net.UnknownHostException

internal fun Resource.Fail.isParserException() =
    exception is JsonParseException || exception is EOFException

internal fun Resource.Fail.isNetworkException() =
    exception is UnknownHostException || exception is ConnectException