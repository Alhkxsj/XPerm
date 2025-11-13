package com.xperm.service.ui

data class ServiceStatus(
    val isRunning: Boolean = false,
    val uid: Int = -1,
    val apiVersion: Int = -1,
    val patchVersion: Int = -1,
    val permission: Boolean = false,
    val serviceAvailable: Boolean = false,
    val serviceUid: Int = -1
) {
    companion object {
        val NOT_RUNNING = ServiceStatus()
    }
}