package com.propellerads.sdk.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

internal sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Fail(val exception: Throwable?) : Resource<Nothing>() {
        val message: String
            get() = exception?.message ?: "Unknown exception"
    }

    fun dataOrNull() = when (this) {
        is Success -> data
        else -> null
    }
}

internal interface Mappable<D> {
    fun map(): D
}

/**
 * Used to execute network request and map result to Resource
 */
internal fun <T : Mappable<D>, D> execute(
    errorParser: IErrorParser,
    request: suspend () -> T
): Flow<Resource<D>> = flow {
    emit(Resource.Loading)
    emit(
        Resource.Success(
            request().map()
        )
    )
}
    .catch { e ->
        emit(
            Resource.Fail(
                when (e) {
                    is HttpException -> errorParser.parse(e)
                    else -> e
                }
            )
        )
    }

internal fun <T, D> executeRaw(
    mapper: (T) -> D,
    request: suspend () -> T
): Flow<Resource<D>> = flow {
    emit(Resource.Loading)
    emit(
        Resource.Success(
            mapper(
                request()
            )
        )
    )
}
    .catch { e ->
        emit(Resource.Fail(e))
    }