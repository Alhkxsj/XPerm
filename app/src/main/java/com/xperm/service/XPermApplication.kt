package com.xperm.service

import android.app.Application
import android.util.Log
import rikka.shizuku.ShizukuProvider
import rikka.shizuku.provider.SingleProcessProvider

class XPermApplication : Application() {
    companion object {
        private const val TAG = "XPermApplication"
    }

    override fun onCreate() {
        super.onCreate()
        
        // 初始化系统权限提供者
        try {
            Log.d(TAG, "XPermApplication initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing system permissions", e)
        }
    }
}