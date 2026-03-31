package com.example.experiment.data.store

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.experiment.data.db.AppDatabaseHelper
import com.example.experiment.data.db.NewsSchema
import com.example.experiment.pojo.VO.CommentVO
import com.example.experiment.pojo.VO.NewsDetailsVO
import com.example.experiment.pojo.VO.NewsProfileVO
import com.example.experiment.pojo.entity.News
import java.util.UUID

/**
 * 新闻数据访问层：负责新闻与评论的聚合读写。
 */
class NewsStore(
    private val dbHelper: AppDatabaseHelper,
    private val userStore: UserStore,
    private val contentStore: ContentStore
) {

    fun seedFromMockIfEmpty(items: List<NewsDetailsVO>) {
        if (items.isEmpty() || queryNewsCount() > 0) return
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            items.forEach { item ->
                val entity = item.toEntity()
                db.insertWithOnConflict(
                    NewsSchema.TABLE_NEWS,
                    null,
                    entity.toContentValues(),
                    SQLiteDatabase.CONFLICT_REPLACE
                )
                contentStore.upsertNewsContent(db, entity.id, entity.content)
                replaceComments(db, entity.id, entity.comments)
                pruneCommentsOverLimit(db, entity.id)
            }
            pruneNewsOverLimit(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun queryNewsCount(): Int {
        val sql = "SELECT COUNT(1) FROM ${NewsSchema.TABLE_NEWS}"
        dbHelper.readableDatabase.rawQuery(sql, null).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getInt(0)
            }
        }
        return 0
    }

    fun queryNewsDetailsList(): List<NewsDetailsVO> {
        val result = mutableListOf<NewsDetailsVO>()
        val sql = """
            SELECT n.${NewsSchema.COL_ID}, n.${NewsSchema.COL_TITLE}, n.${NewsSchema.COL_AUTHOR}, n.${NewsSchema.COL_PUBLISH_TIME},
                   COALESCE(c.${NewsSchema.COL_CONTENT_TEXT}, n.${NewsSchema.COL_CONTENT}) AS detail_content,
                   n.${NewsSchema.COL_IMAGE_LOCAL_PATH}, n.${NewsSchema.COL_IMAGE_URL}
            FROM ${NewsSchema.TABLE_NEWS} n
            LEFT JOIN ${NewsSchema.TABLE_NEWS_CONTENT} c ON n.${NewsSchema.COL_ID} = c.${NewsSchema.COL_NEWS_CONTENT_NEWS_ID}
            ORDER BY n.${NewsSchema.COL_PUBLISH_TIME} DESC, n.${NewsSchema.COL_ID} DESC
        """.trimIndent()

        dbHelper.readableDatabase.rawQuery(sql, null).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_ID)
            val titleIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_TITLE)
            val authorIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_AUTHOR)
            val publishTimeIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_PUBLISH_TIME)
            val contentIndex = cursor.getColumnIndexOrThrow("detail_content")
            val localPathIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_IMAGE_LOCAL_PATH)
            val imageUrlIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_IMAGE_URL)

            while (cursor.moveToNext()) {
                val newsId = cursor.getString(idIndex)
                result.add(
                    NewsDetailsVO(
                        id = newsId,
                        title = cursor.getString(titleIndex),
                        author = cursor.getString(authorIndex),
                        publishTime = cursor.getString(publishTimeIndex),
                        content = cursor.getString(contentIndex),
                        comments = queryCommentsByNewsId(newsId),
                        imageLocalPath = cursor.getStringOrNull(localPathIndex),
                        imageUrl = cursor.getStringOrNull(imageUrlIndex)
                    )
                )
            }
        }
        return result
    }

    fun insertOrReplace(news: News): Long {
        val db = dbHelper.writableDatabase
        var result = -1L
        db.beginTransaction()
        try {
            result = db.insertWithOnConflict(
                NewsSchema.TABLE_NEWS,
                null,
                news.toContentValues(),
                SQLiteDatabase.CONFLICT_REPLACE
            )
            contentStore.upsertNewsContent(db, news.id, news.content)
            replaceComments(db, news.id, news.comments)
            pruneCommentsOverLimit(db, news.id)
            pruneNewsOverLimit(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return result
    }

    fun addComment(newsId: String, content: String): Long {
        val trimmed = content.trim()
        if (trimmed.isBlank()) return -1L

        val db = dbHelper.writableDatabase
        val (userId, username, avatarLocalPath) = userStore.resolveDefaultUserSnapshot()
        val nextOrder = queryNextCommentOrder(db, newsId)
        val commentId = UUID.randomUUID().toString()

        val values = ContentValues().apply {
            put(NewsSchema.COL_COMMENT_ID, commentId)
            put(NewsSchema.COL_NEWS_ID, newsId)
            put(NewsSchema.COL_COMMENT_USER_ID, userId)
            put(NewsSchema.COL_COMMENT_USERNAME_SNAPSHOT, username)
            put(NewsSchema.COL_COMMENT_AVATAR_SNAPSHOT, avatarLocalPath)
            put(NewsSchema.COL_COMMENT_TIME, currentTimeText())
            put(NewsSchema.COL_COMMENT_CONTENT, trimmed)
            put(NewsSchema.COL_COMMENT_ORDER, nextOrder)
            put(NewsSchema.COL_COMMENT_STATUS, 1)
        }
        return db.insert(NewsSchema.TABLE_NEWS_COMMENTS, null, values)
    }

    fun queryCommentItemsByNewsId(newsId: String): List<CommentVO> {
        val result = mutableListOf<CommentVO>()
        val sql = """
            SELECT ${NewsSchema.COL_COMMENT_ID}, ${NewsSchema.COL_NEWS_ID}, ${NewsSchema.COL_COMMENT_USER_ID},
                   ${NewsSchema.COL_COMMENT_USERNAME_SNAPSHOT}, ${NewsSchema.COL_COMMENT_AVATAR_SNAPSHOT},
                   ${NewsSchema.COL_COMMENT_TIME}, ${NewsSchema.COL_COMMENT_CONTENT}
            FROM ${NewsSchema.TABLE_NEWS_COMMENTS}
            WHERE ${NewsSchema.COL_NEWS_ID} = ? AND ${NewsSchema.COL_COMMENT_STATUS} = 1
            ORDER BY ${NewsSchema.COL_COMMENT_ORDER} ASC, ${NewsSchema.COL_COMMENT_TIME} ASC
        """.trimIndent()

        dbHelper.readableDatabase.rawQuery(sql, arrayOf(newsId)).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_COMMENT_ID)
            val newsIdIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_NEWS_ID)
            val userIdIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_COMMENT_USER_ID)
            val usernameIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_COMMENT_USERNAME_SNAPSHOT)
            val avatarIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_COMMENT_AVATAR_SNAPSHOT)
            val timeIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_COMMENT_TIME)
            val contentIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_COMMENT_CONTENT)

            while (cursor.moveToNext()) {
                result.add(
                    CommentVO(
                        commentId = cursor.getString(idIndex),
                        newsId = cursor.getString(newsIdIndex),
                        userId = cursor.getString(userIdIndex),
                        username = cursor.getString(usernameIndex),
                        avatarLocalPath = cursor.getStringOrNull(avatarIndex),
                        avatarUrl = null,
                        commentTime = cursor.getString(timeIndex),
                        content = cursor.getString(contentIndex)
                    )
                )
            }
        }
        return result
    }

    fun compactIfNeeded() {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            pruneNewsOverLimit(db)
            pruneAllCommentsOverLimit(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun queryNewsProfileList(): List<NewsProfileVO> {
        val result = mutableListOf<NewsProfileVO>()
        val sql = """
            SELECT ${NewsSchema.COL_TITLE}, ${NewsSchema.COL_PROFILE}, ${NewsSchema.COL_IMAGE_LOCAL_PATH}, ${NewsSchema.COL_IMAGE_URL}
            FROM ${NewsSchema.TABLE_NEWS}
            ORDER BY ${NewsSchema.COL_PUBLISH_TIME} DESC
        """.trimIndent()

        dbHelper.readableDatabase.rawQuery(sql, null).use { cursor ->
            val titleIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_TITLE)
            val profileIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_PROFILE)
            val localPathIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_IMAGE_LOCAL_PATH)
            val imageUrlIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_IMAGE_URL)

            while (cursor.moveToNext()) {
                result.add(
                    NewsProfileVO(
                        title = cursor.getString(titleIndex),
                        profile = cursor.getString(profileIndex),
                        imageLocalPath = cursor.getStringOrNull(localPathIndex),
                        imageUrl = cursor.getStringOrNull(imageUrlIndex)
                    )
                )
            }
        }
        return result
    }

    fun queryNewsDetailsById(id: String): NewsDetailsVO? {
        val sql = """
            SELECT n.${NewsSchema.COL_ID}, n.${NewsSchema.COL_TITLE}, n.${NewsSchema.COL_AUTHOR}, n.${NewsSchema.COL_PUBLISH_TIME},
                   COALESCE(c.${NewsSchema.COL_CONTENT_TEXT}, n.${NewsSchema.COL_CONTENT}) AS detail_content,
                   n.${NewsSchema.COL_IMAGE_LOCAL_PATH}, n.${NewsSchema.COL_IMAGE_URL}
            FROM ${NewsSchema.TABLE_NEWS} n
            LEFT JOIN ${NewsSchema.TABLE_NEWS_CONTENT} c ON n.${NewsSchema.COL_ID} = c.${NewsSchema.COL_NEWS_CONTENT_NEWS_ID}
            WHERE n.${NewsSchema.COL_ID} = ?
            LIMIT 1
        """.trimIndent()

        dbHelper.readableDatabase.rawQuery(sql, arrayOf(id)).use { cursor ->
            if (!cursor.moveToFirst()) return null

            val idIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_ID)
            val titleIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_TITLE)
            val authorIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_AUTHOR)
            val publishTimeIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_PUBLISH_TIME)
            val contentIndex = cursor.getColumnIndexOrThrow("detail_content")
            val localPathIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_IMAGE_LOCAL_PATH)
            val imageUrlIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_IMAGE_URL)

            return NewsDetailsVO(
                id = cursor.getString(idIndex),
                title = cursor.getString(titleIndex),
                author = cursor.getString(authorIndex),
                publishTime = cursor.getString(publishTimeIndex),
                content = cursor.getString(contentIndex),
                comments = queryCommentsByNewsId(cursor.getString(idIndex)),
                imageLocalPath = cursor.getStringOrNull(localPathIndex),
                imageUrl = cursor.getStringOrNull(imageUrlIndex)
            )
        }
    }

    private fun replaceComments(db: SQLiteDatabase, newsId: String, comments: List<String>) {
        db.delete(NewsSchema.TABLE_NEWS_COMMENTS, "${NewsSchema.COL_NEWS_ID} = ?", arrayOf(newsId))
        val (userId, username, avatarLocalPath) = userStore.resolveDefaultUserSnapshot()
        comments.forEachIndexed { index, text ->
            val values = ContentValues().apply {
                put(NewsSchema.COL_COMMENT_ID, UUID.randomUUID().toString())
                put(NewsSchema.COL_NEWS_ID, newsId)
                put(NewsSchema.COL_COMMENT_USER_ID, userId)
                put(NewsSchema.COL_COMMENT_USERNAME_SNAPSHOT, username)
                put(NewsSchema.COL_COMMENT_AVATAR_SNAPSHOT, avatarLocalPath)
                put(NewsSchema.COL_COMMENT_TIME, currentTimeText())
                put(NewsSchema.COL_COMMENT_CONTENT, text)
                put(NewsSchema.COL_COMMENT_ORDER, index)
                put(NewsSchema.COL_COMMENT_STATUS, 1)
            }
            db.insert(NewsSchema.TABLE_NEWS_COMMENTS, null, values)
        }
    }

    private fun queryCommentsByNewsId(newsId: String): List<String> {
        val comments = mutableListOf<String>()
        val sql = """
            SELECT ${NewsSchema.COL_COMMENT_CONTENT}
            FROM ${NewsSchema.TABLE_NEWS_COMMENTS}
            WHERE ${NewsSchema.COL_NEWS_ID} = ? AND ${NewsSchema.COL_COMMENT_STATUS} = 1
            ORDER BY ${NewsSchema.COL_COMMENT_ORDER} ASC, ${NewsSchema.COL_COMMENT_TIME} ASC
        """.trimIndent()

        dbHelper.readableDatabase.rawQuery(sql, arrayOf(newsId)).use { cursor ->
            val textIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_COMMENT_CONTENT)
            while (cursor.moveToNext()) {
                comments.add(cursor.getString(textIndex))
            }
        }
        return comments
    }

    private fun pruneNewsOverLimit(db: SQLiteDatabase) {
        db.execSQL(
            """
            DELETE FROM ${NewsSchema.TABLE_NEWS}
            WHERE ${NewsSchema.COL_ID} IN (
                SELECT ${NewsSchema.COL_ID}
                FROM ${NewsSchema.TABLE_NEWS}
                ORDER BY ${NewsSchema.COL_PUBLISH_TIME} DESC, ${NewsSchema.COL_ID} DESC
                LIMIT -1 OFFSET ?
            )
            """.trimIndent(),
            arrayOf(NewsSchema.MAX_NEWS_COUNT)
        )
    }

    private fun pruneCommentsOverLimit(db: SQLiteDatabase, newsId: String) {
        db.execSQL(
            """
            DELETE FROM ${NewsSchema.TABLE_NEWS_COMMENTS}
            WHERE ${NewsSchema.COL_COMMENT_ID} IN (
                SELECT ${NewsSchema.COL_COMMENT_ID}
                FROM ${NewsSchema.TABLE_NEWS_COMMENTS}
                WHERE ${NewsSchema.COL_NEWS_ID} = ?
                ORDER BY ${NewsSchema.COL_COMMENT_ORDER} DESC, ${NewsSchema.COL_COMMENT_TIME} DESC
                LIMIT -1 OFFSET ?
            )
            """.trimIndent(),
            arrayOf<Any>(newsId, NewsSchema.MAX_COMMENTS_PER_NEWS)
        )
    }

    private fun pruneAllCommentsOverLimit(db: SQLiteDatabase) {
        db.rawQuery("SELECT ${NewsSchema.COL_ID} FROM ${NewsSchema.TABLE_NEWS}", null).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_ID)
            while (cursor.moveToNext()) {
                pruneCommentsOverLimit(db, cursor.getString(idIndex))
            }
        }
    }

    private fun queryNextCommentOrder(db: SQLiteDatabase, newsId: String): Int {
        val sql = """
            SELECT COALESCE(MAX(${NewsSchema.COL_COMMENT_ORDER}), -1)
            FROM ${NewsSchema.TABLE_NEWS_COMMENTS}
            WHERE ${NewsSchema.COL_NEWS_ID} = ?
        """.trimIndent()
        db.rawQuery(sql, arrayOf(newsId)).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) + 1
            }
        }
        return 0
    }

    private fun currentTimeText(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date())
    }

    private fun News.toContentValues(): ContentValues {
        return ContentValues().apply {
            put(NewsSchema.COL_ID, id)
            put(NewsSchema.COL_TITLE, title)
            put(NewsSchema.COL_AUTHOR, author)
            put(NewsSchema.COL_PUBLISH_TIME, publishTime)
            put(NewsSchema.COL_PROFILE, profile)
            put(NewsSchema.COL_CONTENT, content)
            put(NewsSchema.COL_IMAGE_LOCAL_PATH, imageLocalPath)
            put(NewsSchema.COL_IMAGE_URL, imageUrl)
        }
    }

    private fun NewsDetailsVO.toEntity(): News {
        return News(
            id = id,
            title = title,
            author = author,
            publishTime = publishTime,
            profile = content.take(80),
            content = content,
            comments = comments,
            imageLocalPath = imageLocalPath,
            imageUrl = imageUrl
        )
    }

    private fun Cursor.getStringOrNull(index: Int): String? {
        return if (isNull(index)) null else getString(index)
    }
}


