package com.lunacattus.common.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

class ReceiverSafeContext(
    base: Context
) : ContextWrapper(base) {

    override fun registerReceiver(
        receiver: BroadcastReceiver?,
        filter: IntentFilter?
    ): Intent? {
        return if (Build.VERSION.SDK_INT >= 33) {
            super.registerReceiver(
                receiver,
                filter,
                RECEIVER_EXPORTED
            )
        } else {
            super.registerReceiver(receiver, filter)
        }
    }

    override fun registerReceiver(
        receiver: BroadcastReceiver?,
        filter: IntentFilter?,
        flags: Int
    ): Intent? {
        return super.registerReceiver(receiver, filter, flags)
    }
}
