package com.propellerads.sdk.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

internal fun <T> Flow<T>.doOnLoading(
    predicate: suspend () -> Unit
): Flow<T> = flow {
    collect { res ->
        if (res is Resource.Loading) {
            predicate()
        }
        emit(res)
    }
}

internal fun <T> Flow<T>.retryIfFailed(
    predicate: suspend FlowCollector<T>.(resource: Resource.Fail, attempt: Long) -> Boolean
): Flow<T> = flow {
    var attempt = 0L
    var shallRetry: Boolean
    do {
        shallRetry = false
        collect { res ->
            if (res is Resource.Fail && !res.isParserException()) {
                if (this.predicate(res, attempt)) {
                    shallRetry = true
                    attempt++
                } else {
                    emit(res)
                }
            } else {
                emit(res)
            }
        }
    } while (shallRetry)
}

internal fun <T> Flow<T>.retryUntilFail(
    interval: Long,
): Flow<T> = flow {
    var shallRetry: Boolean
    do {
        shallRetry = false
        collect { res ->
            if (res is Resource.Success<*>) {
                shallRetry = true
                delay(interval)
            } else {
                emit(res)
            }
        }
    } while (shallRetry)
}