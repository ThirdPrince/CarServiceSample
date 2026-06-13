package com.sample.carservicesample.data.source

import kotlinx.coroutines.flow.Flow

interface CarPropertySource<T> {
    suspend fun get(): T?
    fun observe(): Flow<T?>
}
