package com.xperm.service.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xperm.service.R
import com.xperm.service.XPermService

// 数据模型
data class ServiceStatus(
    val isRunning: Boolean = false,
    val uid: Int = -1,
    val apiVersion: Int = 0,
    val patchVersion: Int = 0,
    val permission: Boolean = false
)

// 主页适配器
class HomeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_STATUS = 0
        private const val VIEW_TYPE_MANAGE_APPS = 1
        private const val VIEW_TYPE_TERMINAL = 2
        private const val VIEW_TYPE_START_ROOT = 3
        private const val VIEW_TYPE_START_ADB = 4
        private const val VIEW_TYPE_LEARN_MORE = 5
        private const val VIEW_TYPE_ADB_PERMISSION_LIMITED = 6
    }

    private var serviceStatus: ServiceStatus = ServiceStatus()
    private var grantedCount: Int = 0

    fun updateStatus(status: ServiceStatus, grantedCount: Int = 0) {
        this.serviceStatus = status
        this.grantedCount = grantedCount
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_STATUS
            1 -> if (serviceStatus.permission) VIEW_TYPE_MANAGE_APPS else -1
            2 -> if (serviceStatus.permission) VIEW_TYPE_TERMINAL else -1
            3 -> if (serviceStatus.isRunning && !serviceStatus.permission) VIEW_TYPE_ADB_PERMISSION_LIMITED else -1
            4 -> VIEW_TYPE_START_ROOT
            5 -> VIEW_TYPE_START_ADB
            6 -> VIEW_TYPE_LEARN_MORE
            else -> -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_STATUS -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_item_status, parent, false)
                ServerStatusViewHolder(view)
            }
            VIEW_TYPE_MANAGE_APPS -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_item_manage_apps, parent, false)
                ManageAppsViewHolder(view)
            }
            VIEW_TYPE_START_ROOT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_item_start_root, parent, false)
                StartRootViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_item_status, parent, false)
                ServerStatusViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ServerStatusViewHolder -> holder.bind(serviceStatus)
            is ManageAppsViewHolder -> holder.bind(serviceStatus, grantedCount)
            is StartRootViewHolder -> holder.bind(serviceStatus.uid == 0)
        }
    }

    override fun getItemCount(): Int {
        var count = 1 // status item
        if (serviceStatus.permission) count += 2 // manage apps and terminal
        if (serviceStatus.isRunning && !serviceStatus.permission) count++ // adb permission limited
        count += 3 // root, adb, learn more
        return count
    }
}

// 服务器状态视图持有者
class ServerStatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val iconView: ImageView = itemView.findViewById(R.id.icon)
    private val titleView: TextView = itemView.findViewById(R.id.text1)
    private val summaryView: TextView = itemView.findViewById(R.id.text2)

    fun bind(status: ServiceStatus) {
        val context = itemView.context
        val ok = status.isRunning
        val isRoot = status.uid == 0
        val user = if (isRoot) "root" else "adb"

        if (ok) {
            iconView.setImageResource(R.drawable.ic_check_circle)
            iconView.setColorFilter(context.getColor(R.color.colorSuccess))
        } else {
            iconView.setImageResource(R.drawable.ic_error)
            iconView.setColorFilter(context.getColor(R.color.colorError))
        }

        val title = if (ok) {
            "XPerm 服务运行中"
        } else {
            "XPerm 服务未运行"
        }

        val summary = if (ok) {
            "用户: $user, 版本: ${status.apiVersion}.${status.patchVersion}"
        } else {
            ""
        }

        titleView.text = title
        summaryView.text = summary
        summaryView.visibility = if (summary.isEmpty()) View.GONE else View.VISIBLE
    }
}

// 管理应用视图持有者
class ManageAppsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val titleView: TextView = itemView.findViewById(R.id.text1)
    private val summaryView: TextView = itemView.findViewById(R.id.text2)

    fun bind(status: ServiceStatus, grantedCount: Int) {
        val context = itemView.context
        if (!status.isRunning) {
            itemView.isEnabled = false
            titleView.text = "应用管理"
            summaryView.text = "服务未运行"
        } else {
            itemView.isEnabled = true
            titleView.text = "已授权应用 ($grantedCount)"
            summaryView.text = "查看和管理授权的应用"
        }
    }
}

// 启动Root视图持有者
class StartRootViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val titleView: TextView = itemView.findViewById(R.id.text1)
    private val summaryView: TextView = itemView.findViewById(R.id.text2)

    fun bind(isRunningAsRoot: Boolean) {
        titleView.text = if (isRunningAsRoot) "重新启动 (Root)" else "启动 (Root)"
        summaryView.text = "以 Root 权限运行 XPerm 服务"
    }
}