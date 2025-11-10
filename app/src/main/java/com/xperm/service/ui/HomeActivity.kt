package com.xperm.service.ui

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
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

class HomeActivity : AppBarActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: HomeAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = com.xperm.service.databinding.ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        title = "XPerm"
        
        setupRecyclerView()
        updateServiceStatus()
    }
    
    private fun setupRecyclerView() {
        adapter = HomeAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.fixEdgeEffect()
    }
    
    private fun updateServiceStatus() {
        val isRunning = isServiceRunning(XPermService::class.java)
        val status = ServiceStatus(
            isRunning = isRunning,
            uid = if (isRunning) ShizukuCompatAPI.getUid() else -1,
            apiVersion = ShizukuCompatAPI.getVersion(),
            patchVersion = ShizukuCompatAPI.getServerPatchVersion(),
            permission = true // 默认有权限
        )
        
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