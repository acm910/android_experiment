package com.example.experiment.data

import android.content.Context
import com.example.experiment.data.db.AppDatabaseHelper
import com.example.experiment.data.store.ContentStore
import com.example.experiment.data.store.NewsStore
import com.example.experiment.data.store.UserStore
import com.example.experiment.pojo.VO.CommentVO
import com.example.experiment.pojo.VO.NewsDetailsVO
import com.example.experiment.pojo.VO.NewsProfileVO
import com.example.experiment.pojo.entity.News

/**
 * 兼容层：保留原有调用入口，内部转发到拆分后的 Store。
 */
class NewsDbHelper(context: Context) {
    private val dbHelper = AppDatabaseHelper(context.applicationContext)
    private val userStore = UserStore(dbHelper)
    private val contentStore = ContentStore(dbHelper)
    private val newsStore = NewsStore(dbHelper, userStore, contentStore)

    fun seedFromMockIfEmpty(items: List<NewsDetailsVO>) {
        newsStore.seedFromMockIfEmpty(items)
    }

    fun queryNewsCount(): Int {
        return newsStore.queryNewsCount()
    }

    fun queryNewsDetailsList(): List<NewsDetailsVO> {
        return newsStore.queryNewsDetailsList()
    }

    fun insertOrReplace(news: News): Long {
        return newsStore.insertOrReplace(news)
    }

    fun addComment(newsId: String, content: String): Long {
        return newsStore.addComment(newsId, content)
    }

    fun queryCommentItemsByNewsId(newsId: String): List<CommentVO> {
        return newsStore.queryCommentItemsByNewsId(newsId)
    }

    fun compactIfNeeded() {
        newsStore.compactIfNeeded()
    }

    fun queryNewsProfileList(): List<NewsProfileVO> {
        return newsStore.queryNewsProfileList()
    }

    fun queryNewsDetailsById(id: String): NewsDetailsVO? {
        return newsStore.queryNewsDetailsById(id)
    }
}
