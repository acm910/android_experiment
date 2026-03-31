package com.example.experiment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.experiment.R
import com.example.experiment.pojo.VO.CommentVO

/**
 * 评论列表适配器：按“头像/用户名/时间/内容”展示评论。
 */
class CommentAdapter(initialItems: List<CommentVO>) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val items = initialItems.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val item = items[position]
        holder.usernameTextView.text = item.username
        holder.timeTextView.text = item.commentTime
        holder.contentTextView.text = item.content

        val avatarModel = when {
            !item.avatarLocalPath.isNullOrBlank() -> item.avatarLocalPath
            !item.avatarUrl.isNullOrBlank() -> item.avatarUrl
            else -> R.mipmap.ic_launcher_round
        }
        holder.avatarImageView.load(avatarModel) {
            crossfade(true)
            placeholder(R.mipmap.ic_launcher_round)
            error(R.mipmap.ic_launcher_round)
        }

        holder.dividerView.visibility = if (position == items.lastIndex) View.GONE else View.VISIBLE
    }

    override fun getItemCount(): Int = items.size

    fun replaceItems(newItems: List<CommentVO>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatarImageView: ImageView = itemView.findViewById(R.id.ivCommentAvatar)
        val usernameTextView: TextView = itemView.findViewById(R.id.tvCommentUsername)
        val timeTextView: TextView = itemView.findViewById(R.id.tvCommentTime)
        val contentTextView: TextView = itemView.findViewById(R.id.tvCommentContent)
        val dividerView: View = itemView.findViewById(R.id.commentDivider)
    }
}

