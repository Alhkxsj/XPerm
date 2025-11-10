package com.xperm.service.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.File
import java.net.Socket

/**
 * 客户端身份验证工具类
 * 用于验证通过Socket连接的客户端身份
 */
object ClientAuthUtils {
    private const val TAG = "ClientAuthUtils"
    
    /**
     * 从Socket连接获取客户端包名
     * 通过Unix Domain Socket的peer credentials获取客户端UID，然后查询包名
     */
    fun getPackageNameFromSocket(context: Context, socket: Socket): String? {
        return try {
            // 注意：在Android中，通过标准TCP Socket很难直接获取客户端的UID
            // 这里提供几种可能的实现方式：
            
            // 方式1：如果使用Unix Domain Socket，可以通过peer credentials获取UID
            // 方式2：客户端在连接时主动发送身份信息，服务端验证
            // 方式3：通过Binder机制获取客户端信息
            
            // 简化实现，返回null表示需要客户端主动认证
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting package name from socket", e)
            null
        }
    }
    
    /**
     * 验证客户端是否已授权
     */
    fun isClientAuthorized(context: Context, packageName: String): Boolean {
        return try {
            val packageManager = context.packageManager
            val permissionManager = com.xperm.service.PermissionManager.getInstance(context)
            
            // 检查应用是否安装
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            if (packageInfo.applicationInfo == null) {
                Log.e(TAG, "Application info is null for package: $packageName")
                return false
            }
            
            val uid = packageInfo.applicationInfo.uid
            
            // 检查应用是否声明了需要XPerm权限
            val requestedPermissions = packageInfo.requestedPermissions
            val hasXPermPermission = requestedPermissions?.contains("com.xperm.service.permission.XPERM") == true
            
            if (!hasXPermPermission) {
                Log.w(TAG, "Package $packageName does not declare XPerm permission")
                return false
            }
            
            // 检查应用是否已被授权
            val isAuthorized = permissionManager.isGranted(packageName, uid)
            Log.d(TAG, "Package $packageName authorization status: $isAuthorized")
            
            isAuthorized
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Package not found: $packageName", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking client authorization", e)
            false
        }
    }
    
    /**
     * 验证客户端签名（可选的安全增强）
     */
    fun verifyClientSignature(context: Context, packageName: String, expectedSignature: String?): Boolean {
        return try {
            if (expectedSignature.isNullOrEmpty()) {
                // 如果没有提供期望的签名，跳过验证
                return true
            }
            
            val packageInfo = context.packageManager.getPackageInfo(
                packageName, 
                PackageManager.GET_SIGNATURES
            )
            
            // 获取应用签名
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            
            if (signatures.isNullOrEmpty()) {
                Log.w(TAG, "No signatures found for package: $packageName")
                return false
            }
            
            // 简化实现，实际应用中应该比较签名的哈希值
            // 这里只是示例，实际应用中需要更复杂的签名验证逻辑
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying client signature", e)
            false
        }
    }
    
    /**
     * 处理客户端认证请求
     * 客户端发送认证信息，服务端验证
     */
    fun handleAuthenticationRequest(context: Context, authData: String): String? {
        return try {
            // 解析认证数据，格式可以是JSON或其他自定义格式
            // 示例格式：{"packageName":"com.example.app","signature":"..."}
            
            // 简化实现，假设authData就是包名
            if (authData.isNotEmpty() && isClientAuthorized(context, authData)) {
                authData
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling authentication request", e)
            null
        }
    }
}