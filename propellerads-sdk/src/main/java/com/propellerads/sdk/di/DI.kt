package com.propellerads.sdk.di

import android.content.Context
import androidx.startup.Initializer
import com.propellerads.sdk.BuildConfig
import com.propellerads.sdk.api.ApiConfig
import com.propellerads.sdk.api.ApiErrorParser
import com.propellerads.sdk.api.IApi
import com.propellerads.sdk.api.MockApi
import com.propellerads.sdk.configuration.ConfigLoader
import com.propellerads.sdk.configuration.IConfigLoader
import com.propellerads.sdk.bannerAd.bannerManager.BannerManager
import com.propellerads.sdk.bannerAd.bannerManager.IBannerManager
import com.propellerads.sdk.provider.adId.AdIdProvider
import com.propellerads.sdk.provider.adId.IAdIdProvider
import com.propellerads.sdk.provider.deviceType.DeviceTypeProvider
import com.propellerads.sdk.provider.deviceType.IDeviceTypeProvider
import com.propellerads.sdk.provider.publisherId.IPublisherIdProvider
import com.propellerads.sdk.provider.publisherId.PublisherIdProvider
import com.propellerads.sdk.repository.IErrorParser
import com.propellerads.sdk.repository.IPropellerRepository
import com.propellerads.sdk.repository.PropellerRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object DI {

    private val httpClient = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG)
                addInterceptor(
                    HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY)
                )
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

//    private val api: IApi = retrofit.create(IApi::class.java)
    private val api: IApi = MockApi()

    private val errorParser: IErrorParser = ApiErrorParser()

    val repo: IPropellerRepository = PropellerRepository(api, errorParser)

    private lateinit var _configLoader: ConfigLoader

    val configLoader: IConfigLoader
        get() = _configLoader

    val bannerManager: IBannerManager = BannerManager()

    fun init(context: Context) {
        val adIdProvider: IAdIdProvider = AdIdProvider(context)
        val publisherIdProvider: IPublisherIdProvider = PublisherIdProvider(context)
        val deviceTypeProvider: IDeviceTypeProvider = DeviceTypeProvider(context)

        _configLoader = ConfigLoader(
            repo,
            adIdProvider,
            publisherIdProvider,
            deviceTypeProvider,
        )
    }
}

/**
 * Initializer for startup-runtime
 */
internal class DIInitializer : Initializer<DI> {
    override fun create(context: Context) = DI.apply { init(context) }
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}