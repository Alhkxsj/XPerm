package com.xperm.service

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.xperm.service.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()
    private val REQUEST_CODE_NOTIFICATION = 1001
    
    private var isServiceRunning = false
    private var isStartingService = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeUI()
        setupClickListeners()
        checkPermissions()
        updateServiceStatus()
        
        logMessage("XPerm 服务初始化完成")
        logMessage("设备: ${Build.MANUFACTURER} ${Build.MODEL}")
        logMessage("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
    }
    
    private fun initializeUI() {
        // 设置按钮状态
        binding.btnStartService.isEnabled = true
        binding.btnStopService.isEnabled = false
        
        // 设置图标间距
        binding.btnStartService.compoundDrawablePadding = 16
        binding.btnStopService.compoundDrawablePadding = 16
        binding.btnWirelessPairing.compoundDrawablePadding = 16
        binding.btnPermissions.compoundDrawablePadding = 16
        
        // 加载设置
        binding.switchShizuku.isChecked = getSharedPreferences("xperm_config", MODE_PRIVATE)
            .getBoolean("shizuku_compat", true)
    }
    
    private fun setupClickListeners() {
        binding.btnStartService.setOnClickListener {
            if (!isStartingService) {
                startXPermService()
            }
        }
        
        binding.btnStopService.setOnClickListener {
            stopXPermService()
        }
        
        binding.btnWirelessPairing.setOnClickListener {
            showWirelessPairingDialog()
        }
        
        binding.btnPermissions.setOnClickListener {
                showPermissionsDialog()
            }
            
            binding.btnAuthorizedApps.setOnClickListener {
                showAuthorizedApps()
            }
        
        binding.layoutStatus.setOnClickListener {
            updateServiceStatus()
            logMessage("服务状态已刷新")
        }
        
        binding.switchShizuku.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences("xperm_config", MODE_PRIVATE)
                .edit()
                .putBoolean("shizuku_compat", isChecked)
                .apply()
            logMessage("Shizuku 兼容模式: ${if (isChecked) \"启用\" else \"禁用\"}")
        }
    }
    
    private fun startXPermService() {
        isStartingService = true
        showLoading(true)
        logMessage("正在启动 XPerm 服务...")
        
        executor.execute {
            val hasRoot = checkRootAccess()
            
            handler.post {
                if (hasRoot) {
                    val intent = Intent(this@MainActivity, XPermService::class.java)
                    intent.putExtra("shizuku_compat", binding.switchShizuku.isChecked)
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                    
                    handler.postDelayed({
                        updateServiceStatus()
                        showLoading(false)
                        isStartingService = false
                    }, 2000)
                    
                } else {
                    showLoading(false)
                    isStartingService = false
                    showRootRequiredDialog()
                }
            }
        }
    }
    
    private fun stopXPermService() {
        logMessage("正在停止 XPerm 服务...")
        val intent = Intent(this, XPermService::class.java)
        stopService(intent)
        handler.postDelayed({ updateServiceStatus() }, 1000)
    }
    
    private fun checkRootAccess(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c id")
            val output = process.inputStream.bufferedReader().readText()
            val hasRoot = output.contains("uid=0")
            
            if (hasRoot) {
                logMessage("Root 权限验证成功")
            } else {
                logMessage("Root 权限验证失败")
            }
            hasRoot
            
        } catch (e: Exception) {
            logMessage("Root 检查异常: ${e.message}")
            false
        }
    }
    
    private fun showWirelessPairingDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_wireless_pairing, null)
        val editPairingCode = dialogView.findViewById<EditText>(R.id.editPairingCode)
        val editPort = dialogView.findViewById<EditText>(R.id.editPort)
        val btnOpenDeveloper = dialogView.findViewById<Button>(R.id.btnOpenDeveloper)
        val btnPair = dialogView.findViewById<Button>(R.id.btnPair)
        
        editPort.setText("5555")
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("无线调试配对")
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        btnOpenDeveloper.setOnClickListener {
            openDeveloperOptions()
        }
        
        btnPair.setOnClickListener {
            val pairingCode = editPairingCode.text.toString()
            val port = editPort.text.toString()
            
            if (port.isNotEmpty()) {
                performWirelessPairing(port.toInt(), pairingCode)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "请填写端口号", Toast.LENGTH_SHORT).show()
            }
        }
        
        dialog.show()
    }
    
    private fun openDeveloperOptions() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            startActivity(intent)
            logMessage("已跳转到开发者选项")
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
            logMessage("请在设置中手动打开开发者选项")
        }
    }
    
    private fun performWirelessPairing(port: Int, pairingCode: String) {
        logMessage("开始无线配对: 端口 $port")
        
        executor.execute {
            try {
                // 启用无线调试
                Runtime.getRuntime().exec("su -c setprop service.adb.tcp.port $port").waitFor()
                Runtime.getRuntime().exec("su -c stop adbd").waitFor()
                Runtime.getRuntime().exec("su -c start adbd").waitFor()
                
                handler.post {
                    logMessage("ADB 无线调试已启用端口: $port")
                    Toast.makeText(this@MainActivity, "无线调试已启用", Toast.LENGTH_LONG).show()
                }
                
            } catch (e: Exception) {
                handler.post {
                    logMessage("无线调试启用失败: ${e.message}")
                    Toast.makeText(this@MainActivity, "启用失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun showPermissionsDialog() {
        val items = arrayOf("查看已授权应用", "管理 root 权限", "清理权限缓存")
        
        AlertDialog.Builder(this)
            .setTitle("权限管理")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showAuthorizedApps()
                    1 -> manageRootPermissions()
                    2 -> clearPermissionCache()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun showAuthorizedApps() {
        val intent = Intent(this, AuthorizedAppsActivity::class.java)
        startActivity(intent)
    }
    
    private fun manageRootPermissions() {
        logMessage("打开 root 权限管理...")
        try {
            val intent = packageManager.getLaunchIntentForPackage("com.topjohnwu.magisk")
            if (intent != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "未找到 Magisk 应用", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            logMessage("无法打开权限管理: ${e.message}")
        }
    }
    
    private fun clearPermissionCache() {
        getSharedPreferences("xperm_auth", MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        logMessage("权限缓存已清理")
        Toast.makeText(this, "权限缓存已清理", Toast.LENGTH_SHORT).show()
    }
    
    private fun showRootRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("需要 Root 权限")
            .setMessage("XPerm 需要 root 权限才能正常运行。\n\n请确保：\n• 设备已正确 root\n• 已授予 root 权限\n• Magisk/KSU 正常运行")
            .setPositiveButton("重试") { _, _ -> startXPermService() }
            .setNegativeButton("取消", null)
            .setNeutralButton("帮助") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://github.com/topjohnwu/Magisk")
                }
                startActivity(intent)
            }
            .show()
    }
    
    private fun updateServiceStatus() {
        isServiceRunning = isServiceRunning(XPermService::class.java)
        
        if (isServiceRunning) {
            binding.textStatus.text = "服务运行中"
            binding.iconStatus.setImageResource(R.drawable.ic_check_circle)
            binding.iconStatus.setColorFilter(ContextCompat.getColor(this, R.color.colorSuccess))
            binding.btnStartService.isEnabled = false
            binding.btnStopService.isEnabled = true
        } else {
            binding.textStatus.text = "服务未运行"
            binding.iconStatus.setImageResource(R.drawable.ic_error)
            binding.iconStatus.setColorFilter(ContextCompat.getColor(this, R.color.colorError))
            binding.btnStartService.isEnabled = true
            binding.btnStopService.isEnabled = false
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressLoading.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnStartService.isEnabled = !show
    }
    
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        return try {
            val manager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
            manager.getRunningServices(Integer.MAX_VALUE)
                .any { it.service.className == serviceClass.name }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking service status", e)
            false
        }
    }
    
    private fun logMessage(message: String) {
        handler.post {
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(Date())
            binding.textLog.append("[$timestamp] $message\n")
            
            // 自动滚动到底部
            binding.scrollView.post { 
                binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN) 
            }
        }
    }
    
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIFICATION
                )
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_NOTIFICATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logMessage("通知权限已授予")
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
}