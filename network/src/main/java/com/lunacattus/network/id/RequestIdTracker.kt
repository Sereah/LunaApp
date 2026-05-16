package com.lunacattus.network.id

import java.util.concurrent.ConcurrentHashMap

class RequestIdTracker {

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
