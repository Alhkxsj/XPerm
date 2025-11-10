package com.xperm.service

import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import com.xperm.service.utils.ShizukuCompatUtils

/**
 * Shizuku兼容Binder接口
 * 提供与Shizuku服务相似的Binder接口
 */
class ShizukuCompatBinder(private val context: Context) : Binder() {
    companion object {
        private const val TAG = "ShizukuCompatBinder"
        const val TRANSACTION_getUid = 1
        const val TRANSACTION_checkPermission = 2
        const val TRANSACTION_getVersion = 3
        const val TRANSACTION_getServerPatchVersion = 4
        const val TRANSACTION_exit = 5
        const val TRANSACTION_updateFlagsForUid = 6
        const val TRANSACTION_getFlagsForUid = 7
    }
    
    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        return try {
            when (code) {
                TRANSACTION_getUid -> {
                    data.enforceInterface(ShizukuCompatUtils.BINDER_DESCRIPTOR)
                    val result = ShizukuCompatUtils.getUid()
                    reply?.writeNoException()
                    reply?.writeInt(result)
                    true
                }
                TRANSACTION_checkPermission -> {
                    data.enforceInterface(ShizukuCompatUtils.BINDER_DESCRIPTOR)
                    val permission = data.readString()
                    val result = if (permission != null) {
                        ShizukuCompatUtils.checkRemotePermission(permission)
                    } else {
                        PackageManager.PERMISSION_DENIED
                    }
                    reply?.writeNoException()
                    reply?.writeInt(result)
                    true
                }
                TRANSACTION_getVersion -> {
                    data.enforceInterface(ShizukuCompatUtils.BINDER_DESCRIPTOR)
                    val result = ShizukuCompatUtils.getVersion()
                    reply?.writeNoException()
                    reply?.writeInt(result)
                    true
                }
                TRANSACTION_getServerPatchVersion -> {
                    data.enforceInterface(ShizukuCompatUtils.BINDER_DESCRIPTOR)
                    val result = ShizukuCompatUtils.getServerPatchVersion()
                    reply?.writeNoException()
                    reply?.writeInt(result)
                    true
                }
                TRANSACTION_exit -> {
                    data.enforceInterface(ShizukuCompatUtils.BINDER_DESCRIPTOR)
                    ShizukuCompatUtils.exit()
                    reply?.writeNoException()
                    true
                }
                TRANSACTION_updateFlagsForUid -> {
                    data.enforceInterface(ShizukuCompatUtils.BINDER_DESCRIPTOR)
                    val uid = data.readInt()
                    val mask = data.readInt()
                    val value = data.readInt()
                    ShizukuCompatUtils.updateFlagsForUid(uid, mask, value)
                    reply?.writeNoException()
                    true
                }
                TRANSACTION_getFlagsForUid -> {
                    data.enforceInterface(ShizukuCompatUtils.BINDER_DESCRIPTOR)
                    val uid = data.readInt()
                    val mask = data.readInt()
                    val result = ShizukuCompatUtils.getFlagsForUid(uid, mask)
                    reply?.writeNoException()
                    reply?.writeInt(result)
                    true
                }
                else -> super.onTransact(code, data, reply, flags)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onTransact", e)
            reply?.writeException(e)
            false
        }
    }
}