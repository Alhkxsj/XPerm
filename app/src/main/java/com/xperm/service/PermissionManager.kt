package com.xperm.service

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import java.util.*

/**
 * 权限管理器
 * 管理应用的权限授权状态
 */
class PermissionManager private constructor(context: Context) {
    companion object {
        private const val TAG = "PermissionManager"
        private const val PREFS_NAME = "xperm_permissions"
        private const val FLAG_ALLOWED = 1 shl 1
        private const val FLAG_DENIED = 1 shl 2
        private const val FLAG_TEMPORARY = 1 shl 3
        private const val MASK_PERMISSION = FLAG_ALLOWED or FLAG_DENIED
        private const val MASK_TEMPORARY = FLAG_TEMPORARY
        
        private var instance: PermissionManager? = null
        
        @Synchronized
        fun getInstance(context: Context): PermissionManager {
            if (instance == null) {
                instance = PermissionManager(context.applicationContext)
            }
            return instance!!
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * 检查应用是否已被授权
     */
    fun isGranted(packageName: String, uid: Int): Boolean {
        return try {
            val key = "${packageName}_$uid"
            val flags = prefs.getInt(key, 0)
            (flags and FLAG_ALLOWED) == FLAG_ALLOWED
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permission for $packageName", e)
            false
        }
    }
    
    /**
     * 检查应用是否被临时授权
     */
    fun isTemporaryGranted(packageName: String, uid: Int): Boolean {
        return try {
            val key = "${packageName}_$uid"
            val flags = prefs.getInt(key, 0)
            (flags and FLAG_TEMPORARY) == FLAG_TEMPORARY
        } catch (e: Exception) {
            Log.e(TAG, "Error checking temporary permission for $packageName", e)
            false
        }
    }
    
    /**
     * 授权应用
     */
    fun grant(packageName: String, uid: Int, temporary: Boolean = false) {
        try {
            val key = "${packageName}_$uid"
            val flags = if (temporary) {
                FLAG_ALLOWED or FLAG_TEMPORARY
            } else {
                FLAG_ALLOWED
            }
            prefs.edit()
                .putInt(key, flags)
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error granting permission to $packageName", e)
        }
    }
    
    /**
     * 撤销应用授权
     */
    fun revoke(packageName: String, uid: Int) {
        try {
            val key = "${packageName}_$uid"
            prefs.edit()
                .putInt(key, FLAG_DENIED)
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error revoking permission from $packageName", e)
        }
    }
    
    /**
     * 清除临时授权
     */
    fun clearTemporaryGrants() {
        try {
            val editor = prefs.edit()
            val keys = prefs.all.keys
            for (key in keys) {
                val flags = prefs.getInt(key, 0)
                if ((flags and FLAG_TEMPORARY) == FLAG_TEMPORARY) {
                    editor.putInt(key, flags and MASK_TEMPORARY.inv())
                }
            }
            editor.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing temporary grants", e)
        }
    }
    
    /**
     * 获取所有已授权的应用
     */
    fun getAuthorizedApps(packageManager: PackageManager): List<PackageInfo> {
        return try {
            val apps = mutableListOf<PackageInfo>()
            val allPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS)
            
            for (pi in allPackages) {
                // 检查应用是否声明了需要XPerm权限
                if (pi.requestedPermissions?.contains("com.xperm.service.permission.XPERM") == true) {
                    if (pi.applicationInfo != null && isGranted(pi.packageName, pi.applicationInfo.uid)) {
                        apps.add(pi)
                    }
                }
            }
            
            apps
        } catch (e: Exception) {
            Log.e(TAG, "Error getting authorized apps", e)
            emptyList()
        }
    }
    
    /**
     * 获取所有临时授权的应用
     */
    fun getTemporaryAuthorizedApps(packageManager: PackageManager): List<PackageInfo> {
        return try {
            val apps = mutableListOf<PackageInfo>()
            val allPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS)
            
            for (pi in allPackages) {
                // 检查应用是否声明了需要XPerm权限
                if (pi.requestedPermissions?.contains("com.xperm.service.permission.XPERM") == true) {
                    if (pi.applicationInfo != null && isTemporaryGranted(pi.packageName, pi.applicationInfo.uid)) {
                        apps.add(pi)
                    }
                }
            }
            
            apps
        } catch (e: Exception) {
            Log.e(TAG, "Error getting temporary authorized apps", e)
            emptyList()
        }
    }
}