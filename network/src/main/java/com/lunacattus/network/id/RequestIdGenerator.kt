package com.lunacattus.network.id

import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestIdGenerator @Inject constructor(
    @param:DeviceCode private val deviceCode: String,
) {
    private val counter = AtomicLong(0)

    fun generate(): String {
        val timestamp = System.currentTimeMillis()
        val seq = counter.incrementAndGet()
        return "${timestamp}${seq}${deviceCode}"
    }

    companion object {
        private const val TAG = "RequestIdGenerator"
    }
}
