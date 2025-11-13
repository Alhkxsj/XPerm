package com.xperm.service.utils

import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import rikka.shizuku.Shizuku
import com.xperm.service.XPermService

/**
 * 系统权限工具类
 * 提供系统级权限管理接口
 */
object ShizukuCompatUtils {
    private const val TAG = "XPermUtils"
    const val BINDER_DESCRIPTOR = "rikka.shizuku.IShizukuService"
    
    /**
     * 检查服务Binder是否可用
     */
    fun pingBinder(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking service status", e)
            false
        }
    }
    
    /**
     * 获取服务Binder
     */
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
    
    /**
     * 获取服务UID
     */
    fun getUid(): Int {
        return try {
            Shizuku.getUid()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting UID", e)
            -1
        }
    }
    
    /**
     * 获取服务版本
     */
    fun getVersion(): Int {
        return try {
            Shizuku.getVersion()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting version", e)
            -1
        }
    }
    
    /**
     * 获取服务器补丁版本
     */
    fun getServerPatchVersion(): Int {
        return try {
            Shizuku.getServerPatchVersion()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting server patch version", e)
            -1
        }
    }
    
    /**
     * 获取最新服务版本
     */
    fun getLatestServiceVersion(): Int {
        return try {
            Shizuku.getLatestServiceVersion()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting latest service version", e)
            -1
        }
    }
    
    /**
     * 检查远程权限
     */
    fun checkRemotePermission(permission: String): Int {
        return try {
            Shizuku.checkPermission(permission)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking remote permission", e)
            PackageManager.PERMISSION_DENIED
        }
    }
    
    /**
     * 退出服务
     */
    fun exit() {
        try {
            // 服务由系统控制，不能直接退出
        } catch (e: Exception) {
            Log.e(TAG, "Error in exit", e)
        }
    }
    
    /**
     * 更新指定UID的权限标志
     */
    fun updateFlagsForUid(uid: Int, mask: Int, value: Int) {
        try {
            // 实现权限更新逻辑
            Log.d(TAG, "Updating flags for UID $uid, mask: $mask, value: $value")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating flags for UID $uid", e)
        }
    }
    
    /**
     * 获取指定UID的权限标志
     */
    fun getFlagsForUid(uid: Int, mask: Int): Int {
        return try {
            // 实现权限获取逻辑
            Log.d(TAG, "Getting flags for UID $uid, mask: $mask")
            0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting flags for UID $uid", e)
            0
        }
    }
    
    /**
     * 请求权限
     */
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
    
    /**
     * 检查自身权限
     */
    fun checkSelfPermission(): Int {
        return try {
            Shizuku.checkSelfPermission()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking self permission", e)
            PackageManager.PERMISSION_DENIED
        }
    }
    
    /**
     * 是否应该显示权限请求理由
     */
    fun shouldShowRequestPermissionRationale(): Boolean {
        return try {
            Shizuku.shouldShowRequestPermissionRationale()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permission rationale", e)
            false
        }
    }
}