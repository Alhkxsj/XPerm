package com.xperm.service

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class CommandExecutor {
    companion object {
        private const val TAG = "CommandExecutor"
        
        fun executeCommand(command: String): CommandResult {
            return try {
                val process = Runtime.getRuntime().exec(command)
                val output = process.inputStream.bufferedReader().use(BufferedReader::readText)
                val error = process.errorStream.bufferedReader().use(BufferedReader::readText)
                
                process.waitFor()
                
                CommandResult(
                    success = process.exitValue() == 0,
                    output = output,
                    error = error,
                    exitCode = process.exitValue()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Command execution failed: $command", e)
                CommandResult(
                    success = false,
                    output = "",
                    error = e.message ?: "Unknown error",
                    exitCode = -1
                )
            }
        }
        
        fun executeRootCommand(command: String, context: Context? = null, packageName: String? = null): CommandResult {
            // 如果提供了上下文和包名，检查权限
            if (context != null && packageName != null) {
                val permissionManager = PermissionManager.getInstance(context)
                val packageManager = context.packageManager
                val packageInfo = try {
                    packageManager.getPackageInfo(packageName, 0)
                } catch (e: Exception) {
                    Log.e(TAG, "Package not found: $packageName", e)
                    return CommandResult(
                        success = false,
                        output = "",
                        error = "Package not found: $packageName",
                        exitCode = -1
                    )
                }
                
                // 检查应用是否已被授权
                if (packageInfo.applicationInfo != null) {
                    if (!permissionManager.isGranted(packageName, packageInfo.applicationInfo.uid)) {
                        Log.e(TAG, "Permission denied for $packageName")
                        return CommandResult(
                            success = false,
                            output = "",
                            error = "Permission denied for $packageName",
                            exitCode = -1
                        )
                    }
                    
                    // 检查是否是临时授权
                    if (permissionManager.isTemporaryGranted(packageName, packageInfo.applicationInfo.uid)) {
                        Log.d(TAG, "Executing command with temporary permission for $packageName")
                    } else {
                        Log.d(TAG, "Executing command with permanent permission for $packageName")
                    }
                } else {
                    Log.e(TAG, "Application info is null for package: $packageName")
                    return CommandResult(
                        success = false,
                        output = "",
                        error = "Application info is null for package: $packageName",
                        exitCode = -1
                    )
                }
            } else {
                Log.w(TAG, "Executing command without client context verification")
                return CommandResult(
                    success = false,
                    output = "",
                    error = "Context and package name required for security",
                    exitCode = -1
                )
            }
            
            // 安全性改进：验证命令
            if (!isValidCommand(command)) {
                Log.e(TAG, "Invalid command detected: $command")
                return CommandResult(
                    success = false,
                    output = "",
                    error = "Invalid command",
                    exitCode = -1
                )
            }
            
            return executeCommand("su -c $command")
        }
        
        // 增加命令验证函数以防止命令注入
        private fun isValidCommand(command: String): Boolean {
            // 检查是否有潜在的危险字符或命令
            val dangerousPatterns = listOf(
                "|", "||", "&", "&&", ";", ">", ">>", "<", 
                "$((", "$(", "`", "eval", "exec", "source", "."
            )
            
            return !dangerousPatterns.any { command.contains(it, ignoreCase = true) }
        }
    }
}

data class CommandResult(
    val success: Boolean,
    val output: String,
    val error: String,
    val exitCode: Int
)