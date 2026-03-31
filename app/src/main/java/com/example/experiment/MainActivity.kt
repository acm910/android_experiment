package com.example.experiment

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.experiment.adapter.NewsAdapter
import com.example.experiment.data.NewsDbHelper
import com.example.experiment.data.NewsMockData

/**
 * 新闻首页：负责列表展示、下拉刷新和投稿入口。
 */
class MainActivity : ComponentActivity() {
    private lateinit var dbHelper: NewsDbHelper
    private lateinit var adapter: NewsAdapter

    private val submissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // 投稿成功后立即回到数据库重新拉取，保证首页和详情一致。
            if (result.resultCode == RESULT_OK) {
                loadNewsFromDb()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 页面初始化：先准备数据库，再绑定列表与交互事件。
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.mainactivity)

        dbHelper = NewsDbHelper(this)
        // 首次启动把 Mock 数据落库，之后统一从数据库读取。
        dbHelper.seedFromMockIfEmpty(NewsMockData.getNewsDetailsList())

        val rootView = findViewById<android.view.View>(R.id.mainRoot)
        val submitButton = findViewById<TextView>(R.id.btnSubmit)
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this)
        adapter = NewsAdapter(emptyList()) { details ->
            val intent = Intent(this, NewsActivity::class.java).apply {
                putExtra(NewsActivity.EXTRA_NEWS_ID, details.id)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        loadNewsFromDb()

        submitButton.setOnClickListener {
            submissionLauncher.launch(Intent(this, SubmissionActivity::class.java))
        }

        swipeRefresh.setOnRefreshListener {
            swipeRefresh.postDelayed({
                // 下拉刷新时从数据库重查新闻，而不是直接读取内存 Mock。
                loadNewsFromDb()
                swipeRefresh.isRefreshing = false
            }, 500L)
        }

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    private fun loadNewsFromDb() {
        // 每次刷新都走数据库，保证显示的是当前持久化数据。
        adapter.replaceItems(dbHelper.queryNewsDetailsList())
    }
}
