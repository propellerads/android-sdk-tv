package com.propellerads.sdk.api.interceptor

import com.propellerads.sdk.repository.IUsedDataProvider
import okhttp3.Interceptor
import okhttp3.Response

internal class CookieHeaderInterceptor(
    userDataProvider: IUsedDataProvider,
) : Interceptor {

    @Volatile
    private var cookie: String = ""

    init {
        buildCookie(userDataProvider)
    }

    private fun buildCookie(userDataProvider: IUsedDataProvider) {
        val userId = userDataProvider.getUserId()
        val timestamp = userDataProvider.getUserCreationTime()
        cookie = "OAID=$userId; oaidts=$timestamp"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("cookie", cookie)
            .build()
        return chain.proceed(request)
    }
}