package com.propellerads.sdk.repository

import com.propellerads.sdk.provider.deviceType.DeviceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

internal interface IPropellerRepository {

    fun getSettings(
        publisherId: String,
        userId: String,
        deviceType: DeviceType
    ): Flow<Resource<List<WidgetConfig>>>

    fun impressionCallback(url: String): Flow<Resource<OK>>
}

internal sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Fail(val exception: Exception?) : Resource<Nothing>()
}

/**
 * Used to execute network request and map result to Resource
 */
internal fun <T : Mappable<D>, D> execute(
    errorParser: IErrorParser,
    request: suspend () -> T
): Flow<Resource<D>> = flow {
    emit(Resource.Loading)
    try {
        emit(
            Resource.Success(
                request().map()
            )
        )
    } catch (e: Exception) {
        emit(
            Resource.Fail(
                when (e) {
                    is HttpException -> errorParser.parse(e)
                    else -> e
                }
            )
        )
    }
}

internal interface Mappable<D> {
    fun map(): D
}