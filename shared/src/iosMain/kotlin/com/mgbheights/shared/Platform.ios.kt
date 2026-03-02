package com.mgbheights.shared

import platform.UIKit.UIDevice

actual class Platform actual constructor() {
    actual val name: String = "iOS"
    actual val osVersion: String = UIDevice.currentDevice.systemVersion
    actual val deviceModel: String = UIDevice.currentDevice.model
}

actual fun getPlatformName(): String = "iOS"

