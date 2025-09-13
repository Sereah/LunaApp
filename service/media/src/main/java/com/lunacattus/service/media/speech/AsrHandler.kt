package com.lunacattus.service.media.speech

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import com.lunacattus.speech.asr.ActionType
import com.lunacattus.speech.asr.AppName
import com.lunacattus.speech.asr.AsrResult
import com.lunacattus.speech.asr.ControlCommand
import com.lunacattus.speech.asr.Domain
import com.lunacattus.speech.asr.PhoneCommand
import com.lunacattus.speech.asr.SettingItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AsrHandler @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter
) {

    fun handler(result: AsrResult) {
        when (result) {
            is AsrResult.Final -> {
                result.nluRec?.let {
                    showNlu(it)
                }
                val command = result.command
                command?.let {
                    when (it.domain) {
                        Domain.PHONE -> {
                            it as PhoneCommand
                            if (it.action == ActionType.CALL) {
                                handleCall(it.person, it.number)
                            }
                            if (it.action == ActionType.SMS) {
                                handleSms(it.person, it.number)
                            }
                        }

                        Domain.CONTROL -> {
                            it as ControlCommand
                            if (it.settingItem != null) {
                                handleSettingItem(it.action == ActionType.OPEN, it.settingItem!!)
                            }
                            if (it.app != null) {
                                handleApp(it.action == ActionType.OPEN, it.app!!)
                            }
                        }
                    }
                }
            }

            is AsrResult.Partial -> {
                showNlu(result.rec)
            }
        }
    }

    private fun showNlu(string: String) {

    }

    private fun handleCall(person: String?, number: String?) {

    }

    private fun handleSms(person: String?, number: String?) {

    }


    private fun handleApp(open: Boolean, appName: AppName) {
        when(appName) {
            AppName.PLAYER -> {}
            AppName.CAMERA -> {}
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleSettingItem(open: Boolean, settingItem: SettingItem) {
        when (settingItem) {
            SettingItem.BT -> {
                if (open) {
                    bluetoothAdapter.enable()
                } else {
                    bluetoothAdapter.disable()
                }
            }

            SettingItem.WIFI -> {

            }

            SettingItem.AIRPLANE -> {

            }

            SettingItem.SILENT -> {

            }
        }
    }

}