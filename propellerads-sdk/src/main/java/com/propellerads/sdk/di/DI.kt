package com.propellerads.sdk.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.propellerads.sdk.BuildConfig
import com.propellerads.sdk.api.ApiConfig
import com.propellerads.sdk.api.ApiErrorParser
import com.propellerads.sdk.api.IApi
import com.propellerads.sdk.api.interceptor.CookieHeaderInterceptor
import com.propellerads.sdk.api.interceptor.DeviceDataInterceptor
import com.propellerads.sdk.bannerAd.bannerManager.BannerManager
import com.propellerads.sdk.bannerAd.bannerManager.IBannerManager
import com.propellerads.sdk.configuration.ConfigLoader
import com.propellerads.sdk.configuration.IConfigLoader
import com.propellerads.sdk.provider.adId.AdIdProvider
import com.propellerads.sdk.provider.adId.IAdIdProvider
import com.propellerads.sdk.provider.deviceType.DeviceTypeProvider
import com.propellerads.sdk.provider.deviceType.IDeviceTypeProvider
import com.propellerads.sdk.provider.publisherId.IPublisherIdProvider
import com.propellerads.sdk.provider.publisherId.PublisherIdProvider
import com.propellerads.sdk.repository.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal object DI {

    private var _configLoader: ConfigLoader? = null

    val configLoader: IConfigLoader
        get() = _configLoader ?: IConfigLoader.STUB

    val bannerManager: IBannerManager = BannerManager()

    fun init(context: Context) {
        val api = buildApi(context)
        val repo = buildRepo(api)
        _configLoader = buildConfigLoader(context, repo)
    }

    private fun buildApi(context: Context): IApi {
        val userIdProvider: IUsedIdProvider = UsedIdProvider(context)
        val cookieInterceptor = CookieHeaderInterceptor(userIdProvider)
        val deviceInterceptor = DeviceDataInterceptor()

        val httpClient = OkHttpClient.Builder()
            .apply {
                addInterceptor(cookieInterceptor)
                addInterceptor(deviceInterceptor)
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.BODY)
                    )
                }
            }
            .build()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(IApi::class.java)
//        return MockApi()
    }

    private fun buildRepo(api: IApi): IPropellerRepository {
        val errorParser: IErrorParser = ApiErrorParser()
        return PropellerRepository(api, errorParser)
    }

    private fun buildConfigLoader(context: Context, repo: IPropellerRepository): ConfigLoader {
        val adIdProvider: IAdIdProvider = AdIdProvider(context)
        val publisherIdProvider: IPublisherIdProvider = PublisherIdProvider(context)
        val deviceTypeProvider: IDeviceTypeProvider = DeviceTypeProvider(context)

        return ConfigLoader(
            repo,
            adIdProvider,
            publisherIdProvider,
            deviceTypeProvider,
        )
    }
}