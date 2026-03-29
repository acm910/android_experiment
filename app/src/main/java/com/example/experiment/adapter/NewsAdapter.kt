package com.example.experiment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.experiment.R
import com.example.experiment.pojo.NewsDetails

class NewsAdapter(
    private val items: List<NewsDetails>,
    private val onItemClick: (NewsDetails) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val item = items[position]
        holder.titleTextView.text = item.title
        holder.contentTextView.text = item.content
        holder.dividerView.visibility = if (position == items.lastIndex) View.GONE else View.VISIBLE
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tvTitle)
        val contentTextView: TextView = itemView.findViewById(R.id.tvContent)
        val dividerView: View = itemView.findViewById(R.id.itemDivider)
    }
}

