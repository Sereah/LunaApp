package com.lunacattus.conflux.domain.bluetooth

import android.content.Context
import com.lunacattus.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) : IBluetoothRepository {
    init {
        Logger.d(TAG, "init")
    }

    companion object {
        const val TAG = "BluetoothRepository"
    }
}