package com.lunacattus.network.http

interface IHttpClient {

    suspend fun post(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
    ): Result<String>

    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
    ): Result<String>
}
