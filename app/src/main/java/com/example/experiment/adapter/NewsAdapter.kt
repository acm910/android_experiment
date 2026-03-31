package com.example.experiment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.experiment.R
import com.example.experiment.pojo.VO.NewsDetailsVO

/**
 * 新闻列表适配器：负责条目绑定、图片加载和点击回调。
 */
class NewsAdapter(
    initialItems: List<NewsDetailsVO>,
    private val onItemClick: (NewsDetailsVO) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    private val items = initialItems.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val item = items[position]
        holder.titleTextView.text = item.title
        holder.contentTextView.text = item.content
        holder.thumbImageView.load(resolveImageModel(holder, item)) {
            crossfade(true)
            placeholder(R.mipmap.ic_launcher)
            error(R.mipmap.ic_launcher)
        }
        holder.dividerView.visibility = if (position == items.lastIndex) View.GONE else View.VISIBLE
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun appendItems(newItems: List<NewsDetailsVO>) {
        if (newItems.isEmpty()) return
        val start = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(start, newItems.size)
    }

    fun replaceItems(newItems: List<NewsDetailsVO>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    private fun resolveImageModel(holder: NewsViewHolder, item: NewsDetailsVO): Any {
        // 优先使用本地路径；本地路径无法解析时回退到 URL 或默认图。
        val localPath = item.imageLocalPath
        if (!localPath.isNullOrBlank()) {
            if (localPath.startsWith("http://") || localPath.startsWith("https://")) {
                return localPath
            }

            return when {
                localPath.startsWith("/") ||
                    localPath.startsWith("file://") ||
                    localPath.startsWith("content://") ||
                    localPath.startsWith("android.resource://") -> localPath
                else -> {
                    val context = holder.itemView.context
                    val resourceName = localPath
                        .substringAfterLast('/')
                        .substringAfterLast('\\')
                        .substringBeforeLast('.')
                    val drawableId = context.resources.getIdentifier(
                        resourceName,
                        "drawable",
                        context.packageName
                    )
                    val mipmapId = context.resources.getIdentifier(
                        resourceName,
                        "mipmap",
                        context.packageName
                    )
                    when {
                        drawableId != 0 -> drawableId
                        mipmapId != 0 -> mipmapId
                        else -> R.mipmap.ic_launcher
                    }
                }
            }
        }

        return if (!item.imageUrl.isNullOrBlank()) {
            item.imageUrl
        } else {
            R.mipmap.ic_launcher
        }
    }

    /**
     * 条目视图缓存，减少滚动时的重复查找。
     */
    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tvTitle)
        val contentTextView: TextView = itemView.findViewById(R.id.tvContent)
        val thumbImageView: ImageView = itemView.findViewById(R.id.ivNewsThumb)
        val dividerView: View = itemView.findViewById(R.id.itemDivider)
    }
}
