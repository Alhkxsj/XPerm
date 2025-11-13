package com.xperm.service

import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import com.xperm.service.utils.ShizukuCompatUtils

/**
 * Shizuku兼容API接口
 * 提供与Shizuku相似的权限管理功能
 */
object ShizukuCompatAPI {
    private const val TAG = "ShizukuCompatAPI"
    
    // 检查Shizuku服务是否可用
    fun pingBinder(): Boolean {
        return ShizukuCompatUtils.pingBinder()
    }
    
    // 获取Shizuku服务的Binder
    fun getBinder(): IBinder? {
        return ShizukuCompatUtils.getBinder()
    }
    
    // 获取Shizuku服务器UID
    fun getUid(): Int {
        return ShizukuCompatUtils.getUid()
    }
    
    // 获取Shizuku服务器版本
    fun getVersion(): Int {
        return ShizukuCompatUtils.getVersion()
    }
    
    // 获取服务器补丁版本
    fun getServerPatchVersion(): Int {
        return ShizukuCompatUtils.getServerPatchVersion()
    }
    
    // 检查远程权限
    fun checkRemotePermission(permission: String): Int {
        return ShizukuCompatUtils.checkRemotePermission(permission)
    }
    
    // 获取最新服务版本
    fun getLatestServiceVersion(): Int {
        return ShizukuCompatUtils.getLatestServiceVersion()
    }
    
    // 更新指定UID的权限标志
    fun updateFlagsForUid(uid: Int, mask: Int, value: Int) {
        ShizukuCompatUtils.updateFlagsForUid(uid, mask, value)
    }
    
    // 获取指定UID的权限标志
    fun getFlagsForUid(uid: Int, mask: Int): Int {
        return ShizukuCompatUtils.getFlagsForUid(uid, mask)
    }
    
    // 退出服务
    fun exit() {
        ShizukuCompatUtils.exit()
    }
    
    // 请求权限
    fun requestPermission(requestCode: Int) {
        ShizukuCompatUtils.requestPermission(requestCode)
    }
    
    // 检查权限
    fun checkSelfPermission(): Int {
        return ShizukuCompatUtils.checkSelfPermission()
    }
    
    // 是否应该显示权限请求理由
    fun shouldShowRequestPermissionRationale(): Boolean {
        return ShizukuCompatUtils.shouldShowRequestPermissionRationale()
    }
    
    // 添加Shizuku API v11+ 的兼容方法
    fun addBinderReceivedListener(listener: () -> Unit) {
        Log.d(TAG, "Adding binder received listener")
        // 在XPerm中，可以实现监听器机制
    }
    
    fun removeBinderReceivedListener(listener: () -> Unit) {
        Log.d(TAG, "Removing binder received listener")
    }
    
    fun addBinderDeadListener(listener: () -> Unit) {
        Log.d(TAG, "Adding binder dead listener")
    }
    
    fun removeBinderDeadListener(listener: () -> Unit) {
        Log.d(TAG, "Removing binder dead listener")
    }
}