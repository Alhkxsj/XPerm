package com.xperm.service.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xperm.service.R
import com.xperm.service.ShizukuCompatAPI
import com.xperm.service.XPermService
import com.xperm.service.databinding.ActivityHomeBinding
import com.xperm.service.utils.AppBarActivity
import rikka.recyclerview.fixEdgeEffect
import rikka.shizuku.Shizuku
import rikka.shizuku.Sui

class HomeActivity : AppBarActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: HomeAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        title = "XPerm"
        
        // 初始化Shizuku API
        initializeShizuku()
        
        setupRecyclerView()
        updateServiceStatus()
    }
    
    private fun initializeShizuku() {
        try {
            // 初始化系统权限支持
            val isSuiAvailable = Sui.init(this.packageName)
            println("系统权限初始化: ${if (isSuiAvailable) "成功" else "失败"}")
            
            // 注册服务生命状态监听器
            val binderReceivedListener = rikka.shizuku.Shizuku.OnBinderReceivedListener {
                println("系统服务Binder已接收")
            }
            Shizuku.addBinderReceivedListener(binderReceivedListener)
            
            val binderDeadListener = rikka.shizuku.Shizuku.OnBinderDeadListener {
                println("系统服务Binder已断开")
            }
            Shizuku.addBinderDeadListener(binderDeadListener)
            
        } catch (e: Exception) {
            println("系统权限API初始化失败: ${e.message}")
        }
    }
    
    private fun setupRecyclerView() {
        adapter = HomeAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.fixEdgeEffect()
    }
    
    private fun updateServiceStatus() {
        val isRunning = isServiceRunning(XPermService::class.java)
        val status = if (isRunning) {
            ServiceStatus(
                isRunning = true,
                uid = ShizukuCompatAPI.getUid(),
                apiVersion = ShizukuCompatAPI.getVersion(),
                patchVersion = ShizukuCompatAPI.getServerPatchVersion(),
                permission = ShizukuCompatAPI.checkSelfPermission() == PackageManager.PERMISSION_GRANTED,
                serviceAvailable = ShizukuCompatAPI.pingBinder(),
                serviceUid = ShizukuCompatAPI.getUid()
            )
        } else {
            ServiceStatus.NOT_RUNNING
        }
        
        adapter.updateStatus(status)
    }
    
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // 启动设置Activity
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }
}