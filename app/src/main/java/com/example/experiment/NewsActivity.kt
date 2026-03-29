package com.example.experiment

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.experiment.data.NewsDbHelper
import com.example.experiment.data.NewsMockData
import com.example.experiment.pojo.VO.NewsDetailsVO

class NewsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NEWS_ID = "extra_news_id"
        const val EXTRA_NEWS_DETAILS = "extra_news_details"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
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
        val commentsView = findViewById<TextView>(R.id.tvNewsComments)

        fun bindDetails(item: NewsDetailsVO) {
            titleView.text = item.title
            authorView.text = "作者: ${item.author}"
            timeView.text = "时间: ${item.publishTime}"
            bodyView.text = item.content
            commentsView.text = if (item.comments.isEmpty()) {
                "暂无评论"
            } else {
                item.comments.joinToString(separator = "\n") { "- $it" }
            }
        }

        bindDetails(details)

        swipeRefresh.setOnRefreshListener {
            swipeRefresh.postDelayed({
                // 详情页刷新时优先从数据库读取，保证评论等内容是最新状态。
                if (!newsId.isNullOrBlank()) {
                    details = dbHelper.queryNewsDetailsById(newsId) ?: details
                }
                bindDetails(details)
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
        return intent.getSerializableExtra(EXTRA_NEWS_DETAILS, NewsDetailsVO::class.java)
    }
}