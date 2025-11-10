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
        setContentView(R.layout.activity_authorized_apps)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "授权应用管理"
        
        permissionManager = PermissionManager.getInstance(this)
        
        recyclerView = findViewById(R.id.recycler_view)
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

class AuthorizedAppsAdapter : BaseRecyclerViewAdapter<ClassCreatorPool>() {
    
    init {
        getCreatorPool().putRule(PackageInfo::class.java, AuthorizedAppViewHolder.CREATOR)
        setHasStableIds(true)
    }
    
    override fun onCreateCreatorPool(): ClassCreatorPool {
        return ClassCreatorPool()
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

class AuthorizedAppViewHolder(itemView: View) : BaseViewHolder<PackageInfo>(itemView) {
    
    companion object {
        val CREATOR = BaseViewHolder.Creator<PackageInfo> { inflater: LayoutInflater, parent: ViewGroup? ->
            AuthorizedAppViewHolder(inflater.inflate(R.layout.item_authorized_app, parent, false))
        }
    }
    
    private val iconView: ImageView = itemView.findViewById(R.id.app_icon)
    private val nameView: TextView = itemView.findViewById(R.id.app_name)
    private val packageView: TextView = itemView.findViewById(R.id.app_package)
    private val switch: SwitchMaterial = itemView.findViewById(R.id.app_switch)
    
    override fun onBind() {
        val context = itemView.context
        
        // 设置应用图标
        iconView.setImageDrawable(data.applicationInfo?.loadIcon(context.packageManager))
        
        // 设置应用名称
        nameView.text = data.applicationInfo?.loadLabel(context.packageManager) ?: data.packageName
        
        // 设置包名
        packageView.text = data.packageName
        
        // 设置开关状态
        val permissionManager = PermissionManager.getInstance(context)
        switch.isChecked = permissionManager.isGranted(data.packageName, data.applicationInfo!!.uid)
        
        // 设置开关监听器
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                permissionManager.grant(data.packageName, data.applicationInfo!!.uid)
                Toast.makeText(context, "已授权 ${data.packageName}", Toast.LENGTH_SHORT).show()
            } else {
                permissionManager.revoke(data.packageName, data.applicationInfo!!.uid)
                Toast.makeText(context, "已撤销 ${data.packageName} 的授权", Toast.LENGTH_SHORT).show()
            }
        }
    }
}