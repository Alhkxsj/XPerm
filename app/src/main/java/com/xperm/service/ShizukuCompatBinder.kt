package com.xperm.service

import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import rikka.shizuku.IShizukuService
import rikka.shizuku.Shizuku

/**
 * 系统服务Binder接口
 * 提供系统级权限管理Binder接口
 */
class ShizukuCompatBinder(private val context: Context) : IShizukuService.Stub() {
    companion object {
        private const val TAG = "XPermBinder"
    }
    
    override fun getUid(): Int {
        return android.os.Process.myUid()
    }
    
    override fun checkPermission(permission: String?): Int {
        return try {
            if (permission == null) return PackageManager.PERMISSION_DENIED
            
            // 检查是否有root权限
            val hasRoot = checkRootAccess()
            if (hasRoot) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permission", e)
            PackageManager.PERMISSION_DENIED
        }
    }
    
    override fun getVersion(): Int {
        return 11 // API版本
    }
    
    override fun getServerPatchVersion(): Int {
        return 0
    }
    
    override fun exit() {
        // 服务由系统控制，不能直接退出
        Log.d(TAG, "Request to exit system service (ignored)")
    }
    
    override fun updateFlagsForUid(uid: Int, mask: Int, value: Int) {
        Log.d(TAG, "Updating flags for UID $uid, mask: $mask, value: $value")
    }
    
    override fun getFlagsForUid(uid: Int, mask: Int): Int {
        Log.d(TAG, "Getting flags for UID $uid, mask: $mask")
        return 0
    }
    
    private fun checkRootAccess(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c id")
            val output = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            output.contains("uid=0")
        } catch (e: Exception) {
            Log.e(TAG, "Root check failed", e)
            false
        }
    }
    
    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        return try {
            super.onTransact(code, data, reply, flags)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onTransact", e)
            reply?.writeException(e)
            false
        }
    }
}