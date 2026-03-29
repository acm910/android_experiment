package com.example.experiment

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.experiment.adapter.NewsAdapter
import com.example.experiment.data.NewsMockData

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.mainactivity)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val newsList = NewsMockData.getNewsDetailsList()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = NewsAdapter(newsList) { details ->
            val intent = Intent(this, NewsActivity::class.java).apply {
                putExtra(NewsActivity.EXTRA_NEWS_DETAILS, details)
            }
            startActivity(intent)
        }
    }
}

