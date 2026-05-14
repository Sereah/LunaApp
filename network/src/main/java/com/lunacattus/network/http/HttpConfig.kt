package com.lunacattus.network.http

data class HttpConfig(
    val connectTimeoutMs: Long = 10_000L,
    val readTimeoutMs: Long = 30_000L,
    val writeTimeoutMs: Long = 15_000L,
)
