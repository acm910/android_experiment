package com.example.experiment

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
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
import androidx.core.net.toUri
import androidx.core.view.isVisible

/**
 * 新闻详情页：优先按 newsId 从数据库读取，并支持评论发布与下拉刷新。
 */
class NewsActivity : AppCompatActivity() {
    private var currentVideoUrl: String? = null
    private var lastVideoPositionMs: Int = 0
    private var shouldResumeVideo = false

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
        val videoContainer = findViewById<FrameLayout>(R.id.newsVideoContainer)
        val newsVideoView = findViewById<VideoView>(R.id.vvNewsVideo)
        val commentInput = findViewById<EditText>(R.id.etCommentInput)
        val publishButton = findViewById<Button>(R.id.btnPublishComment)
        val commentsEmptyView = findViewById<TextView>(R.id.tvCommentsEmpty)
        val commentsRecyclerView = findViewById<RecyclerView>(R.id.rvComments)

        val commentAdapter = CommentAdapter(emptyList())
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentsRecyclerView.adapter = commentAdapter

        MediaController(this).also {
            it.setAnchorView(newsVideoView)
            newsVideoView.setMediaController(it)
        }

        fun bindDetails(item: NewsDetailsVO) {
            titleView.text = item.title
            authorView.text = "作者: ${item.author}"
            timeView.text = "时间: ${item.publishTime}"
            bodyView.text = item.content
            bindVideo(item.videoUrl, videoContainer, newsVideoView)
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

    private fun bindVideo(videoUrl: String?, container: FrameLayout, videoView: VideoView) {
        val normalizedUrl = videoUrl?.trim().orEmpty()
        if (normalizedUrl.isBlank()) {
            currentVideoUrl = null
            container.visibility = FrameLayout.GONE
            videoView.stopPlayback()
            return
        }

        val uri = normalizedUrl.toUri()
        val scheme = uri.scheme?.lowercase()
        if ((scheme != "http" && scheme != "https") || uri.host.isNullOrBlank()) {
            currentVideoUrl = null
            container.visibility = FrameLayout.GONE
            videoView.stopPlayback()
            return
        }

        if (currentVideoUrl == normalizedUrl && container.isVisible) {
            return
        }

        currentVideoUrl = normalizedUrl
        lastVideoPositionMs = 0
        shouldResumeVideo = false
        container.visibility = FrameLayout.VISIBLE
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = false
            if (lastVideoPositionMs > 0) {
                videoView.seekTo(lastVideoPositionMs)
            }
            if (shouldResumeVideo || lastVideoPositionMs == 0) {
                videoView.start()
                shouldResumeVideo = false
            }
        }
        videoView.setOnErrorListener { _, _, _ ->
            currentVideoUrl = null
            container.visibility = FrameLayout.GONE
            videoView.stopPlayback()
            Toast.makeText(this, R.string.video_preview_failed, Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun onPause() {
        super.onPause()
        val videoView = findViewById<VideoView>(R.id.vvNewsVideo)
        if (videoView.isPlaying) {
            shouldResumeVideo = true
            lastVideoPositionMs = videoView.currentPosition
            videoView.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        val videoView = findViewById<VideoView>(R.id.vvNewsVideo)
        if (shouldResumeVideo && !currentVideoUrl.isNullOrBlank()) {
            videoView.seekTo(lastVideoPositionMs)
            videoView.start()
            shouldResumeVideo = false
        }
    }

    override fun onStop() {
        super.onStop()
        val videoView = findViewById<VideoView>(R.id.vvNewsVideo)
        if (!currentVideoUrl.isNullOrBlank()) {
            lastVideoPositionMs = videoView.currentPosition
        }
        videoView.pause()
    }
}