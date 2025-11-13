package com.xperm.service

import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import rikka.shizuku.Shizuku
import rikka.shizuku.Sui
import com.xperm.service.utils.ShizukuCompatUtils

/**
 * 权限管理API接口
 * 提供系统级权限管理功能
 */
object ShizukuCompatAPI {
    private const val TAG = "ShizukuCompatAPI"
    
    // 初始化系统权限支持
    fun initializeSuiSupport(packageName: String): Boolean {
        return try {
            Sui.init(packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing system permission support", e)
            false
        }
    }
    
    // 检查服务是否可用
    fun pingBinder(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            Log.e(TAG, "Error pinging binder", e)
            false
        }
    }
    
    // 获取服务的Binder
    fun getBinder(): IBinder? {
        return try {
            if (Shizuku.isPreV11()) {
                Log.e(TAG, "Pre-v11 is not supported")
                return null
            }
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                return Shizuku.getBinder()
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting binder", e)
            null
        }
    }
    
    // 获取服务器UID
    fun getUid(): Int {
        return try {
            Shizuku.getUid()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting UID", e)
            -1
        }
    }
    
    // 获取服务器版本
    fun getVersion(): Int {
        return try {
            Shizuku.getVersion()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting version", e)
            -1
        }
    }
    
    // 获取服务器补丁版本
    fun getServerPatchVersion(): Int {
        return try {
            Shizuku.getServerPatchVersion()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting server patch version", e)
            -1
        }
    }
    
    // 检查远程权限
    fun checkRemotePermission(permission: String): Int {
        return try {
            Shizuku.checkPermission(permission)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking remote permission", e)
            PackageManager.PERMISSION_DENIED
        }
    }
    
    // 获取最新服务版本
    fun getLatestServiceVersion(): Int {
        return try {
            Shizuku.getLatestServiceVersion()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting latest service version", e)
            -1
        }
    }
    
    // 退出服务
    fun exit() {
        try {
            // 服务由系统控制，不能直接退出
        } catch (e: Exception) {
            Log.e(TAG, "Error in exit", e)
        }
    }
    
    // 请求权限
    fun requestPermission(requestCode: Int) {
        try {
            if (Shizuku.isPreV11()) {
                Log.e(TAG, "Pre-v11 is not supported")
                return
            }
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                if (!Shizuku.shouldShowRequestPermissionRationale()) {
                    Shizuku.requestPermission(requestCode)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting permission", e)
        }
    }
    
    // 检查权限
    fun checkSelfPermission(): Int {
        return try {
            Shizuku.checkSelfPermission()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking self permission", e)
            PackageManager.PERMISSION_DENIED
        }
    }
    
    // 是否应该显示权限请求理由
    fun shouldShowRequestPermissionRationale(): Boolean {
        return try {
            Shizuku.shouldShowRequestPermissionRationale()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permission rationale", e)
            false
        }
    }
    
    // 添加服务状态监听器
    fun addBinderReceivedListener(listener: () -> Unit) {
        try {
            val shizukuListener = rikka.shizuku.Shizuku.OnBinderReceivedListener {
                listener()
            }
            Shizuku.addBinderReceivedListener(shizukuListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding binder received listener", e)
        }
    }
    
    fun removeBinderReceivedListener(listener: () -> Unit) {
        try {
            val shizukuListener = rikka.shizuku.Shizuku.OnBinderReceivedListener {
                listener()
            }
            Shizuku.removeBinderReceivedListener(shizukuListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing binder received listener", e)
        }
    }
    
    fun addBinderDeadListener(listener: () -> Unit) {
        try {
            val shizukuListener = rikka.shizuku.Shizuku.OnBinderDeadListener {
                listener()
            }
            Shizuku.addBinderDeadListener(shizukuListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding binder dead listener", e)
        }
    }
    
    fun removeBinderDeadListener(listener: () -> Unit) {
        try {
            val shizukuListener = rikka.shizuku.Shizuku.OnBinderDeadListener {
                listener()
            }
            Shizuku.removeBinderDeadListener(shizukuListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing binder dead listener", e)
        }
    }
}