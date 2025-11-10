package com.xperm.service

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.xperm.service.utils.ShizukuCompatUtils

/**
 * Shizuku兼容的ContentProvider
 * 提供与Shizuku Provider相似的接口
 */
class ShizukuProvider : ContentProvider() {
    companion object {
        private const val TAG = "ShizukuProvider"
        private const val AUTHORITY_SUFFIX = ".shizuku"
        private const val METHOD_SEND_BINDER = "sendBinder"
        
        // 用于存储binder的全局变量
        var binder: IBinder? = null
            private set
    }
    
    override fun onCreate(): Boolean {
        Log.d(TAG, "ShizukuProvider created")
        return true
    }
    
    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        return when (method) {
            METHOD_SEND_BINDER -> {
                handleSendBinder(extras)
            }
            else -> {
                Log.w(TAG, "Unknown method: $method")
                null
            }
        }
    }
    
    private fun handleSendBinder(extras: Bundle?): Bundle? {
        if (extras == null) {
            Log.e(TAG, "Extras is null in handleSendBinder")
            return null
        }
        
        try {
            val binderContainer = extras.getParcelable("moe.shizuku.privileged.api.intent.extra.BINDER")
            if (binderContainer is android.os.Parcelable) {
                // 在XPerm中，我们使用自己的Binder
                val xpermBinder = XPermService.getInstance()?.getServiceBinder()
                if (xpermBinder != null) {
                    binder = xpermBinder
                    Log.d(TAG, "Binder sent successfully")
                    
                    val result = Bundle()
                    result.putBoolean("result", true)
                    return result
                } else {
                    Log.e(TAG, "XPerm service binder is null")
                }
            } else {
                Log.e(TAG, "Binder container is not a valid Parcelable")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling sendBinder", e)
        }
        
        return null
    }
    
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }
    
    override fun getType(uri: Uri): String? {
        return null
    }
    
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }
    
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }
    
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }
}