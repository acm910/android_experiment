package com.example.experiment

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.experiment.adapter.NewsAdapter
import com.example.experiment.data.NewsMockData

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.mainactivity)

        val rootView = findViewById<android.view.View>(R.id.mainRoot)
        val submitButton = findViewById<TextView>(R.id.btnSubmit)
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this)
        val adapter = NewsAdapter(NewsMockData.getNewsDetailsList()) { details ->
            val intent = Intent(this, NewsActivity::class.java).apply {
                putExtra(NewsActivity.EXTRA_NEWS_DETAILS, details)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        submitButton.setOnClickListener {
            startActivity(Intent(this, SubmissionActivity::class.java))
        }

        swipeRefresh.setOnRefreshListener {
            swipeRefresh.postDelayed({
                adapter.replaceItems(NewsMockData.getNewsDetailsList())
                swipeRefresh.isRefreshing = false
            }, 500L)
        }

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }
}

