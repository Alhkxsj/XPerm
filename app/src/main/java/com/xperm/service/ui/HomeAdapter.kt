package com.xperm.service.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.xperm.service.R
import rikka.recyclerview.IdBasedRecyclerViewAdapter
import rikka.recyclerview.IndexCreatorPool

class HomeAdapter : IdBasedRecyclerViewAdapter<Any>() {
    
    private var serviceStatus: ServiceStatus? = null
    
    companion object {
        private const val ID_STATUS = 0L
    }
    
    init {
        setHasStableIds(true)
    }
    
    override fun onCreateCreatorPool(): IndexCreatorPool {
        return IndexCreatorPool()
    }
    
    fun updateStatus(status: ServiceStatus) {
        this.serviceStatus = status
        clear()
        addItem(ServiceStatusViewHolder.CREATOR, status, ID_STATUS)
        notifyDataSetChanged()
    }
}

class ServiceStatusViewHolder(itemView: View) : rikka.recyclerview.BaseViewHolder<ServiceStatus>(itemView) {
    
    companion object {
        val CREATOR = Creator<ServiceStatus> { inflater: LayoutInflater, parent: ViewGroup? ->
            ServiceStatusViewHolder(inflater.inflate(R.layout.item_service_status, parent, false))
        }
    }
    
    private val statusText: TextView = itemView.findViewById(R.id.status_text)
    private val versionText: TextView = itemView.findViewById(R.id.version_text)
    
    override fun onBind() {
        val context = itemView.context
        val item = data
        
        if (item.isRunning) {
            statusText.text = context.getString(R.string.service_running)
            statusText.setTextColor(context.getColor(R.color.colorSuccess))
            versionText.text = context.getString(R.string.version_info, item.apiVersion, item.patchVersion)
        } else {
            statusText.text = context.getString(R.string.service_not_running)
            statusText.setTextColor(context.getColor(R.color.colorError))
            versionText.text = ""
        }
    }
}