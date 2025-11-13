package rikka.recyclerview

import android.util.SparseArray
import androidx.recyclerview.widget.RecyclerView

abstract class IdBasedRecyclerViewAdapter<T>(private val items: MutableList<T>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    private val idMap = SparseArray<Any>()
    private val creatorPool = onCreateCreatorPool()
    
    abstract fun onCreateCreatorPool(): IndexCreatorPool
    
    fun getItems(): MutableList<T> = items
    
    fun getItemAt(position: Int): T = items[position]
    
    override fun getItemCount(): Int = items.size
    
    override fun getItemId(position: Int): Long {
        if (position < 0 || position >= idMap.size()) {
            return RecyclerView.NO_ID
        }
        return idMap.keyAt(position).toLong()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val creator = creatorPool.getCreator(viewType)
        return creator.createViewHolder(parent)
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        @Suppress("UNCHECKED_CAST")
        (holder as? BaseViewHolder<T>)?.onBind(getItemAt(position))
    }
    
    fun addItem(creator: BaseViewHolder.Creator<T>, item: T, id: Long) {
        items.add(item)
        idMap.put(id.toInt(), item)
        creatorPool.putRule(item.javaClass, creator)
    }
    
    fun clear() {
        items.clear()
        idMap.clear()
    }
    
    fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
    }
}