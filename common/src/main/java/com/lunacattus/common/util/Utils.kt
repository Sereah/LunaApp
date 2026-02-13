package com.lunacattus.common.util

import android.content.Context
import android.content.pm.PackageManager


object Utils {
    fun isSystemSignature(context: Context): Boolean {
        val pm = context.packageManager
        return pm.checkSignatures(context.packageName, "android") ==
                PackageManager.SIGNATURE_MATCH
    }
}