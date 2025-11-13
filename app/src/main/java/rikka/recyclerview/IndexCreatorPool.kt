package rikka.recyclerview

import android.view.ViewGroup

class IndexCreatorPool {
    private val creators = mutableMapOf<Int, BaseViewHolder.Creator<out Any>>()
    
    fun putRule(clazz: Class<*>, creator: BaseViewHolder.Creator<out Any>) {
        creators[clazz.hashCode()] = creator
    }
    
    fun getCreator(viewType: Int): BaseViewHolder.Creator<out Any> {
        return creators[viewType] ?: throw IllegalArgumentException("No creator found for viewType: $viewType")
    }
}