package com.xperm.service.ui

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.xperm.service.PermissionManager
import rikka.recyclerview.BaseRecyclerViewAdapter
import rikka.recyclerview.ClassCreatorPool

class AuthorizedAppsActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AuthorizedAppsAdapter
    private lateinit var permissionManager: PermissionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.xperm.service.R.layout.activity_authorized_apps)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "授权应用管理"
        
        permissionManager = PermissionManager.getInstance(this)
        
        recyclerView = findViewById(com.xperm.service.R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = AuthorizedAppsAdapter()
        recyclerView.adapter = adapter
        
        loadAuthorizedApps()
    }
    
    private fun loadAuthorizedApps() {
        val apps = permissionManager.getAuthorizedApps(packageManager)
        adapter.updateData(apps)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

class AuthorizedAppsAdapter : rikka.recyclerview.BaseRecyclerViewAdapter<rikka.recyclerview.ClassCreatorPool>() {
    
    init {
        getCreatorPool().putRule(PackageInfo::class.java, AuthorizedAppViewHolder.CREATOR)
        setHasStableIds(true)
    }
    
    override fun onCreateCreatorPool(): rikka.recyclerview.ClassCreatorPool {
        return rikka.recyclerview.ClassCreatorPool()
    }
    
    override fun getItemId(position: Int): Long {
        return getItemAt(position).hashCode().toLong()
    }
    
    fun updateData(data: List<PackageInfo>) {
        getItems().clear()
        if (data.isEmpty()) {
            // 添加空状态提示
            getItems().add(Any())
        } else {
            getItems().addAll(data)
        }
        notifyDataSetChanged()
    }
}

class AuthorizedAppViewHolder(itemView: View) : rikka.recyclerview.BaseViewHolder<PackageInfo>(itemView) {
    
    companion object {
        val CREATOR = rikka.recyclerview.BaseViewHolder.Creator<PackageInfo> { inflater: LayoutInflater, parent: ViewGroup? ->
            AuthorizedAppViewHolder(inflater.inflate(com.xperm.service.R.layout.item_authorized_app, parent, false))
        }
    }
    
    private val iconView: ImageView = itemView.findViewById(com.xperm.service.R.id.app_icon)
    private val nameView: TextView = itemView.findViewById(com.xperm.service.R.id.app_name)
    private val packageView: TextView = itemView.findViewById(com.xperm.service.R.id.app_package)
    private val switch: SwitchMaterial = itemView.findViewById(com.xperm.service.R.id.app_switch)
    
    override fun onBind() {
        val item = data
        val context = itemView.context
        
        // 设置应用图标
        iconView.setImageDrawable(item.applicationInfo?.loadIcon(context.packageManager))
        
        // 设置应用名称
        nameView.text = item.applicationInfo?.loadLabel(context.packageManager) ?: item.packageName
        
        // 设置包名
        packageView.text = item.packageName
        
        // 设置开关状态
        val permissionManager = PermissionManager.getInstance(context)
        switch.isChecked = permissionManager.isGranted(item.packageName, item.applicationInfo!!.uid)
        
        // 设置开关监听器
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                permissionManager.grant(item.packageName, item.applicationInfo!!.uid)
                Toast.makeText(context, "已授权 ${item.packageName}", Toast.LENGTH_SHORT).show()
            } else {
                permissionManager.revoke(item.packageName, item.applicationInfo!!.uid)
                Toast.makeText(context, "已撤销 ${item.packageName} 的授权", Toast.LENGTH_SHORT).show()
            }
        }
    }
}