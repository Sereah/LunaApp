package com.lunacattus.connection.model.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice

enum class BluetoothDeviceType {
    PHONE,      // 手机
    COMPUTER,   // 电脑/笔记本
    HEADSET,    // 耳机/音箱
    CAR,        // 车机
    INPUT,      // 键盘/鼠标/手柄
    WEARABLE,   // 手表/手环 (BLE)
    UNKNOWN     // 未知
}

@SuppressLint("MissingPermission")
fun BluetoothDevice.getPreciseType(): BluetoothDeviceType {
    val uuids = this.uuids?.map { it.toString().lowercase() }
    val deviceClass = this.bluetoothClass

    // 1. 优先判断是否为输入设备 (HID)，这类设备身份最明确
    if (uuids?.any { it.startsWith("00001124") } == true ||
        deviceClass?.majorDeviceClass == BluetoothClass.Device.Major.PERIPHERAL) {
        return BluetoothDeviceType.INPUT
    }

    // 2. 如果支持 HFP Client (111e) 或 A2DP Sink (110b)，说明是音频接收端（耳机或车机）
    val isAudioSink = uuids?.any { it.startsWith("0000111e") || it.startsWith("0000110b") } == true

    if (isAudioSink) {
        // 通过 BluetoothClass 的子类 (Device Class) 进一步细化
        return when (deviceClass?.deviceClass) {
            // 车机类标识
            BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO -> BluetoothDeviceType.CAR

            // 耳机、音箱类标识
            BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET,
            BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES,
            BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER,
            BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE -> BluetoothDeviceType.HEADSET

            // 如果子类不明确，再根据名字包含 "Car", "BT_KIT" 等关键字（兜底方案）
            else -> {
                val name = this.name?.lowercase() ?: ""
                if (name.contains("car") || name.contains("auto") || name.contains("kit")) {
                    BluetoothDeviceType.CAR
                } else {
                    BluetoothDeviceType.HEADSET
                }
            }
        }
    }

    // 3. 判断是否为手机 (HFP AG 111f)
    if (uuids?.any { it.startsWith("0000111f") } == true ||
        deviceClass?.majorDeviceClass == BluetoothClass.Device.Major.PHONE) {
        return BluetoothDeviceType.PHONE
    }

    return BluetoothDeviceType.UNKNOWN
}