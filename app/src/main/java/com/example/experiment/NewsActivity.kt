package com.example.experiment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.experiment.data.NewsMockData
import com.example.experiment.pojo.NewsDetails

class NewsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NEWS_DETAILS = "extra_news_details"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_news)

        val details = getNewsDetailsFromIntent() ?: NewsMockData.getNewsDetailsList().first()

        val rootView = findViewById<android.view.View>(R.id.newsRoot)
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.newsSwipeRefresh)
        val scrollView = findViewById<NestedScrollView>(R.id.main)
        val titleView = findViewById<TextView>(R.id.tvNewsTitle)
        val authorView = findViewById<TextView>(R.id.tvNewsAuthor)
        val timeView = findViewById<TextView>(R.id.tvNewsTime)
        val bodyView = findViewById<TextView>(R.id.tvNewsBody)
        val commentsView = findViewById<TextView>(R.id.tvNewsComments)

        fun bindDetails() {
            titleView.text = details.title
            authorView.text = "作者: ${details.author}"
            timeView.text = "时间: ${details.publishTime}"
            bodyView.text = details.content
            commentsView.text = if (details.comments.isEmpty()) {
                "暂无评论"
            } else {
                details.comments.joinToString(separator = "\n") { "- $it" }
            }
        }

        bindDetails()

        swipeRefresh.setOnRefreshListener {
            swipeRefresh.postDelayed({
                bindDetails()
                swipeRefresh.isRefreshing = false
            }, 500L)
        }

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    private fun getNewsDetailsFromIntent(): NewsDetails? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_NEWS_DETAILS, NewsDetails::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_NEWS_DETAILS) as? NewsDetails
        }
    }
}