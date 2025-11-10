package com.xperm.service

import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class XPermService : Service() {
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "xperm_service"
        private const val SOCKET_PATH = "/data/local/tmp/xperm.sock"
        private const val SOCKET_PORT = 38388
        private const val TAG = "XPermService"
        private const val MAX_CLIENT_CONNECTIONS = 10
        private const val CLIENT_TIMEOUT_MS = 30000L
        private var instance: XPermService? = null
        
        fun getInstance(): XPermService? = instance
    }
    
    private var isRunning = false
    private var serverThread: Thread? = null
    private val executor = Executors.newCachedThreadPool()
    private lateinit var notificationManager: NotificationManager
    private lateinit var permissionManager: PermissionManager
    private val clientConnections = mutableListOf<Socket>()
    private val clientConnectionsLock = Any()
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        notificationManager = getSystemService(NotificationManager::class.java)
        permissionManager = PermissionManager.getInstance(this)
        createNotificationChannel()
        Log.d(TAG, "XPermService created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Starting XPermService")
        startForegroundService()
        
        if (!isRunning) {
            isRunning = true
            startSocketServer()
        }
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        Log.d(TAG, "Destroying XPermService")
        isRunning = false
        serverThread?.interrupt()
        
        // 关闭所有客户端连接
        synchronized(clientConnectionsLock) {
            clientConnections.forEach { socket ->
                try {
                    socket.close()
                } catch (e: IOException) {
                    Log.w(TAG, "Error closing client socket", e)
                }
            }
            clientConnections.clear()
        }
        
        executor.shutdown()
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            Log.e(TAG, "Interrupted while waiting for executor termination", e)
            executor.shutdownNow()
            Thread.currentThread().interrupt()
        }
        instance = null
        stopForeground(true)
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        // 提供Shizuku兼容的Binder接口
        return ShizukuCompatBinder(this)
    }
    
    fun getServiceBinder(): IBinder {
        return ShizukuCompatBinder(this)
    }
    
    fun getUid(): Int = Process.myUid()
    
    fun checkRemotePermission(permission: String): Int {
        return try {
            val hasRoot = checkRootAccess()
            if (hasRoot) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
        } catch (e: Exception) {
            Log.e(TAG, "Error checking root access", e)
            PackageManager.PERMISSION_DENIED
        }
    }
    
    private fun checkRootAccess(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c id")
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val error = process.errorStream.bufferedReader().use { it.readText() }
            
            process.waitFor()
            
            val hasRoot = output.contains("uid=0")
            if (!hasRoot) {
                Log.w(TAG, "Root check failed. Output: $output, Error: $error")
            }
            hasRoot
        } catch (e: Exception) {
            Log.e(TAG, "Root check failed with exception", e)
            false
        }
    }
    
    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "XPerm 服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "XPerm 权限服务运行中"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("XPerm 服务运行中")
            .setContentText("点击查看服务状态")
            .setSmallIcon(R.drawable.ic_rocket)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun startSocketServer() {
        serverThread = Thread {
            Log.d(TAG, "Starting socket server on port $SOCKET_PORT")
            try {
                val serverSocket = ServerSocket(SOCKET_PORT)
                Log.d(TAG, "Socket server started successfully on port $SOCKET_PORT")
                
                while (isRunning) {
                    try {
                        // Set a timeout to allow checking isRunning periodically
                        serverSocket.soTimeout = 5000 // 5 seconds
                        val client = serverSocket.accept()
                        
                        // 检查连接数限制
                        synchronized(clientConnectionsLock) {
                            if (clientConnections.size >= MAX_CLIENT_CONNECTIONS) {
                                Log.w(TAG, "Max client connections reached, rejecting new connection")
                                try {
                                    client.close()
                                } catch (e: IOException) {
                                    Log.w(TAG, "Error closing rejected client socket", e)
                                }
                                continue
                            }
                            
                            // 设置客户端超时
                            client.soTimeout = CLIENT_TIMEOUT_MS.toInt()
                            clientConnections.add(client)
                        }
                        
                        Log.d(TAG, "New client connected from ${client.remoteSocketAddress}")
                        executor.execute { handleClient(client) }
                    } catch (e: IOException) {
                        if (isRunning) {
                            // This might be a timeout, just continue the loop
                            continue
                        } else {
                            // Service is stopping, exit the loop
                            break
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Unexpected error in socket server loop", e)
                        if (!isRunning) {
                            break
                        }
                    }
                }
                
                serverSocket.close()
                Log.d(TAG, "Socket server stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting socket server", e)
            }
        }.apply { 
            name = "XPermSocketServer"
            start() 
        }
    }
    
    private fun handleClient(client: Socket) {
        var reader: BufferedReader? = null
        var writer: PrintWriter? = null
        var clientPackageName: String? = null
        
        try {
            Log.d(TAG, "Handling client ${client.remoteSocketAddress}")
            reader = BufferedReader(InputStreamReader(client.getInputStream()))
            writer = PrintWriter(client.getOutputStream(), true)
            
            // 首先进行客户端认证
            clientPackageName = authenticateClient(reader, writer)
            if (clientPackageName == null) {
                Log.w(TAG, "Client authentication failed for ${client.remoteSocketAddress}")
                writer.println("{\"success\":false,\"error\":\"Authentication required\"}")
                return
            }
            
            Log.d(TAG, "Client authenticated: $clientPackageName")
            
            var line: String?
            while (isRunning && reader.readLine().also { line = it } != null) {
                line?.let { request ->
                    Log.d(TAG, "Received request from $clientPackageName: ${request.take(200)}") // Log first 200 chars
                    val response = processCommand(request, client, clientPackageName)
                    writer.println(response)
                    Log.d(TAG, "Sent response to $clientPackageName: ${response.take(200)}") // Log first 200 chars
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Client handling error", e)
        } finally {
            try {
                reader?.close()
                writer?.close()
                client.close()
                Log.d(TAG, "Client connection closed for $clientPackageName")
                
                // 从连接列表中移除
                synchronized(clientConnectionsLock) {
                    clientConnections.remove(client)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error closing client connection", e)
            }
        }
    }
    
    /**
     * 对客户端进行身份验证
     */
    private fun authenticateClient(reader: BufferedReader, writer: PrintWriter): String? {
        return try {
            // 发送认证请求
            writer.println("{\"type\":\"auth_required\",\"message\":\"Authentication required\"}")
            
            // 等待客户端发送认证信息
            val authRequest = reader.readLine()
            if (authRequest.isNullOrEmpty()) {
                Log.w(TAG, "No authentication data received")
                return null
            }
            
            // 简化实现：假设客户端发送的是包名
            // 在实际应用中，客户端应该发送包含包名和签名的JSON数据
            val packageName = authRequest.trim()
            
            // 验证客户端是否已授权
            val isAuthorized = com.xperm.service.utils.ClientAuthUtils.isClientAuthorized(this, packageName)
            if (isAuthorized) {
                Log.d(TAG, "Client authenticated successfully: $packageName")
                packageName
            } else {
                Log.w(TAG, "Client not authorized: $packageName")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during client authentication", e)
            null
        }
    }
    
    private fun processCommand(command: String, client: Socket, clientPackageName: String): String {
        return try {
            Log.d(TAG, "Processing command from $clientPackageName: $command")
            when {
                command == "PING" -> {
                    Log.d(TAG, "Responding to PING from $clientPackageName")
                    "{\"success\":true,\"output\":\"PONG\"}"
                }
                command == "VERSION" -> {
                    Log.d(TAG, "Responding to VERSION from $clientPackageName")
                    "{\"success\":true,\"output\":\"XPerm 1.0.0\"}"
                }
                command.startsWith("EXEC:") -> {
                    val cmd = command.removePrefix("EXEC:")
                    Log.d(TAG, "Executing root command for $clientPackageName: $cmd")
                    // 使用已验证的客户端包名执行命令
                    executeRootCommand(cmd, clientPackageName)
                }
                command.startsWith("CHECK_AUTH:") -> {
                    val packageName = command.removePrefix("CHECK_AUTH:")
                    val uid = getUidForPackage(packageName)
                    val isAuthorized = if (uid != -1) {
                        permissionManager.isGranted(packageName, uid)
                    } else {
                        false
                    }
                    "{\"success\":true,\"output\":\"$isAuthorized\"}"
                }
                command.startsWith("GRANT:") -> {
                    val packageName = command.removePrefix("GRANT:")
                    val uid = getUidForPackage(packageName)
                    if (uid != -1) {
                        permissionManager.grant(packageName, uid)
                        "{\"success\":true,\"output\":\"Granted permission to $packageName\"}"
                    } else {
                        "{\"success\":false,\"error\":\"Package not found: $packageName\"}"
                    }
                }
                command.startsWith("REVOKE:") -> {
                    val packageName = command.removePrefix("REVOKE:")
                    val uid = getUidForPackage(packageName)
                    if (uid != -1) {
                        permissionManager.revoke(packageName, uid)
                        "{\"success\":true,\"output\":\"Revoked permission from $packageName\"}"
                    } else {
                        "{\"success\":false,\"error\":\"Package not found: $packageName\"}"
                    }
                }
                else -> {
                    Log.d(TAG, "Unknown command from $clientPackageName: $command")
                    "{\"success\":false,\"error\":\"Unknown command: $command\"}"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing command from $clientPackageName", e)
            "{\"success\":false,\"error\":\"${e.message?.escapeJson()}\"}"
        }
    }
    
    // 从Socket连接获取客户端包名
    private fun getPackageNameFromSocket(client: Socket): String? {
        try {
            // 在实际实现中，这里可以通过以下方式获取客户端包名：
            // 1. 通过Binder调用获取客户端UID，然后查询包名
            // 2. 通过Unix Domain Socket的peer credentials获取UID
            // 3. 通过客户端发送的认证信息验证身份
            
            // 使用客户端身份验证工具
            // 注意：标准TCP Socket无法直接获取客户端UID，需要客户端主动认证
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting package name from socket", e)
            return null
        }
    }
    
    private fun getUidForPackage(packageName: String): Int {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            if (packageInfo.applicationInfo != null) {
                packageInfo.applicationInfo.uid
            } else {
                Log.e(TAG, "Application info is null for package: $packageName")
                -1
            }
        } catch (e: Exception) {
            Log.e(TAG, "Package not found: $packageName", e)
            -1
        }
    }
    
    private fun executeRootCommand(command: String, packageName: String): String {
        return try {
            Log.d(TAG, "Executing root command for package $packageName: $command")
            val result = CommandExecutor.executeRootCommand(command, this, packageName)
            
            if (result.success) {
                Log.d(TAG, "Root command succeeded for package $packageName: $command")
                "{\"success\":true,\"output\":\"${result.output.escapeJson()}\"}"
            } else {
                Log.d(TAG, "Root command failed for package $packageName: $command, error: ${result.error}")
                "{\"success\":false,\"error\":\"${result.error.escapeJson()}\"}"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception executing root command for package $packageName: $command", e)
            "{\"success\":false,\"error\":\"${e.message?.escapeJson()}\"}"
        }
    }
    
    private fun String.escapeJson(): String {
        return this.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}