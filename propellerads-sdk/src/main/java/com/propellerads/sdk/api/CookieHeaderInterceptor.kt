package com.propellerads.sdk.api

import com.propellerads.sdk.repository.IUsedIdProvider
import okhttp3.Interceptor
import okhttp3.Response

internal class CookieHeaderInterceptor(
    userIdProvider: IUsedIdProvider,
) : Interceptor {

    @Volatile
    private var cookie: String = ""

    init {
        buildCookie(userIdProvider)
    }

    private fun buildCookie(userIdProvider: IUsedIdProvider) {
        val userId = userIdProvider.getUserId()
        cookie = "OAID=$userId"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("cookie", cookie)
            .build()
        return chain.proceed(request)
    }
}