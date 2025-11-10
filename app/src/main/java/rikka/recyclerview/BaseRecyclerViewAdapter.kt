package rikka.recyclerview

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerViewAdapter<CreatorPoolType> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<Any>()
    private lateinit var creatorPool: CreatorPoolType

    init {
        creatorPool = onCreateCreatorPool()
    }

    abstract fun onCreateCreatorPool(): CreatorPoolType

    fun getItems(): MutableList<Any> = items

    fun getItemAt(position: Int): Any = items[position]

    fun getCreatorPool(): CreatorPoolType = creatorPool

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return if (position >= 0 && position < items.size) {
            items[position].javaClass.hashCode()
        } else {
            super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val creator = findCreator(viewType)
        return creator.createViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        @Suppress("UNCHECKED_CAST")
        (holder as? BaseViewHolder<Any>)?.onBind(getItemAt(position))
    }

    private fun findCreator(viewType: Int): BaseViewHolder.Creator<out Any> {
        return if (creatorPool is ClassCreatorPool) {
            creatorPool.getCreatorForClass(findClassForViewType(viewType))
        } else {
            throw IllegalStateException("Creator pool must be ClassCreatorPool")
        }
    }

    private fun findClassForViewType(viewType: Int): Class<out Any> {
        for (item in items) {
            if (item.javaClass.hashCode() == viewType) {
                return item.javaClass
            }
        }
        throw IllegalStateException("No class found for viewType: $viewType")
    }
}

class ClassCreatorPool {
    private val creators = mutableMapOf<Class<*>, BaseViewHolder.Creator<out Any>>()

    fun putRule(clazz: Class<*>, creator: BaseViewHolder.Creator<out Any>) {
        creators[clazz] = creator
    }

    fun putRule(clazz: Class<*>, creator: BaseViewHolder.Creator<out Any>) where clazz : Any {
        creators[clazz] = creator
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getCreatorForClass(clazz: Class<T>): BaseViewHolder.Creator<T> {
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            val creator = creators[currentClass]
            if (creator != null) {
                return creator as BaseViewHolder.Creator<T>
            }
            currentClass = currentClass.superclass
        }
        // 如果找不到匹配的创建器，返回一个默认的
        throw IllegalArgumentException("No creator found for class: ${clazz.name}")
    }
}

abstract class BaseViewHolder<T>(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
    protected lateinit var data: T
    protected lateinit var adapter: BaseRecyclerViewAdapter<*>
    protected var position: Int = -1

    fun onBind(item: T) {
        this.data = item
        onBind()
    }

    fun onBind(item: T, position: Int, adapter: BaseRecyclerViewAdapter<*>) {
        this.data = item
        this.position = position
        this.adapter = adapter
        onBind()
    }

    abstract fun onBind()

    open fun onBind(payloads: List<Any>) {
        onBind()
    }

    open fun onRecycle() {}

    interface Creator<T> {
        fun createViewHolder(parent: ViewGroup): BaseViewHolder<T>
    }
}