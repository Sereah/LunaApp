package com.lunacattus.network.id

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestIdTracker @Inject constructor() {

    private val activeIds = ConcurrentHashMap.newKeySet<String>()

    fun register(id: String): Boolean = activeIds.add(id)

    fun expire(id: String) {
        activeIds.remove(id)
    }

    fun expireAll() {
        activeIds.clear()
    }

    fun isActive(id: String): Boolean = activeIds.contains(id)

    fun getActiveCount(): Int = activeIds.size
}
