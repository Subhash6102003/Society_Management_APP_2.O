package com.mgbheights.shared

actual class Platform actual constructor() {
    actual val name: String = "Android"
    actual val osVersion: String = android.os.Build.VERSION.RELEASE
    actual val deviceModel: String = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
}

actual fun getPlatformName(): String = "Android"

