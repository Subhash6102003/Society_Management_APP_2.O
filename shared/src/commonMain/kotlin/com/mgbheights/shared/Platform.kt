package com.mgbheights.shared

expect class Platform() {
    val name: String
    val osVersion: String
    val deviceModel: String
}

expect fun getPlatformName(): String

