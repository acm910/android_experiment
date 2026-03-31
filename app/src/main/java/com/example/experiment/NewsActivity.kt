package com.example.experiment

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.experiment.adapter.CommentAdapter
import com.example.experiment.data.NewsDbHelper
import com.example.experiment.data.NewsMockData
import com.example.experiment.pojo.VO.NewsDetailsVO

/**
 * 新闻详情页：优先按 newsId 从数据库读取，并支持评论发布与下拉刷新。
 */
class NewsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NEWS_ID = "extra_news_id"
        const val EXTRA_NEWS_DETAILS = "extra_news_details"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        // 详情页初始化：确定数据来源并完成视图绑定。
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_news)

        val dbHelper = NewsDbHelper(this)
        val newsId = intent.getStringExtra(EXTRA_NEWS_ID) ?: getNewsDetailsFromIntent()?.id
        var details = if (newsId.isNullOrBlank()) {
            getNewsDetailsFromIntent() ?: NewsMockData.getNewsDetailsList().first()
        } else {
            dbHelper.queryNewsDetailsById(newsId)
                ?: getNewsDetailsFromIntent()
                ?: NewsMockData.getNewsDetailsList().first()
        }

        val rootView = findViewById<android.view.View>(R.id.newsRoot)
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.newsSwipeRefresh)
        val titleView = findViewById<TextView>(R.id.tvNewsTitle)
        val authorView = findViewById<TextView>(R.id.tvNewsAuthor)
        val timeView = findViewById<TextView>(R.id.tvNewsTime)
        val bodyView = findViewById<TextView>(R.id.tvNewsBody)
        val commentInput = findViewById<EditText>(R.id.etCommentInput)
        val publishButton = findViewById<Button>(R.id.btnPublishComment)
        val commentsEmptyView = findViewById<TextView>(R.id.tvCommentsEmpty)
        val commentsRecyclerView = findViewById<RecyclerView>(R.id.rvComments)

        val commentAdapter = CommentAdapter(emptyList())
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentsRecyclerView.adapter = commentAdapter

        fun bindDetails(item: NewsDetailsVO) {
            titleView.text = item.title
            authorView.text = "作者: ${item.author}"
            timeView.text = "时间: ${item.publishTime}"
            bodyView.text = item.content
        }

        fun loadComments() {
            if (newsId.isNullOrBlank()) {
                commentAdapter.replaceItems(emptyList())
                commentsEmptyView.visibility = TextView.VISIBLE
                return
            }
            val comments = dbHelper.queryCommentItemsByNewsId(newsId)
            commentAdapter.replaceItems(comments)
            commentsEmptyView.visibility = if (comments.isEmpty()) TextView.VISIBLE else TextView.GONE
        }

        bindDetails(details)
        loadComments()

        publishButton.setOnClickListener {
            if (newsId.isNullOrBlank()) {
                Toast.makeText(this, R.string.comment_publish_failed, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val input = commentInput.text.toString().trim()
            if (input.isBlank()) {
                Toast.makeText(this, R.string.comment_publish_empty, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val result = dbHelper.addComment(newsId, input)
            if (result == -1L) {
                Toast.makeText(this, R.string.comment_publish_failed, Toast.LENGTH_SHORT).show()
            } else {
                commentInput.text?.clear()
                loadComments()
                Toast.makeText(this, R.string.comment_publish_success, Toast.LENGTH_SHORT).show()
            }
        }

        swipeRefresh.setOnRefreshListener {
            swipeRefresh.postDelayed({
                // 详情页刷新时优先从数据库读取，保证正文和评论都是最新状态。
                if (!newsId.isNullOrBlank()) {
                    details = dbHelper.queryNewsDetailsById(newsId) ?: details
                }
                bindDetails(details)
                loadComments()
                swipeRefresh.isRefreshing = false
            }, 500L)
        }

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    private fun getNewsDetailsFromIntent(): NewsDetailsVO? {
        // 兼容旧的对象透传方式，避免历史跳转参数失效。
        return intent.getSerializableExtra(EXTRA_NEWS_DETAILS, NewsDetailsVO::class.java)
    }
}