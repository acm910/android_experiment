package com.example.experiment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.experiment.adapter.NewsAdapter
import com.example.experiment.data.NewsDbHelper
import com.example.experiment.data.NewsMockData

/**
 * 新闻首页 Fragment：负责新闻列表、下拉刷新和投稿入口。
 */
class NewsHomeFragment : Fragment(R.layout.fragment_news_home) {
    private lateinit var dbHelper: NewsDbHelper
    private lateinit var adapter: NewsAdapter

    private val submissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // 投稿成功后回查数据库，确保列表展示最新数据。
            if (result.resultCode == Activity.RESULT_OK) {
                loadNewsFromDb()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = NewsDbHelper(requireContext())
        dbHelper.seedFromMockIfEmpty(NewsMockData.getNewsDetailsList())

        val submitButton = view.findViewById<TextView>(R.id.btnSubmit)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        adapter = NewsAdapter(emptyList()) { details ->
            val intent = Intent(requireContext(), NewsActivity::class.java).apply {
                putExtra(NewsActivity.EXTRA_NEWS_ID, details.id)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        loadNewsFromDb()

        submitButton.setOnClickListener {
            submissionLauncher.launch(Intent(requireContext(), SubmissionActivity::class.java))
        }

        swipeRefresh.setOnRefreshListener {
            swipeRefresh.postDelayed({
                loadNewsFromDb()
                swipeRefresh.isRefreshing = false
            }, 500L)
        }
    }

    private fun loadNewsFromDb() {
        adapter.replaceItems(dbHelper.queryNewsDetailsList())
    }
}

