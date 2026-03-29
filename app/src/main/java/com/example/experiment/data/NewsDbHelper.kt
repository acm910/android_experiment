package com.example.experiment.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.experiment.pojo.VO.NewsDetailsVO
import com.example.experiment.pojo.VO.NewsProfileVO
import com.example.experiment.pojo.entity.News
import org.json.JSONArray

class NewsDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_NEWS_TABLE)
        db.execSQL(SQL_CREATE_NEWS_PUBLISH_TIME_INDEX)
        db.execSQL(SQL_CREATE_NEWS_TITLE_INDEX)
        db.execSQL(SQL_CREATE_NEWS_COMMENTS_TABLE)
        db.execSQL(SQL_CREATE_NEWS_COMMENTS_INDEX)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(SQL_ADD_PROFILE_COLUMN)
        }
        if (oldVersion < 3) {
            db.execSQL(SQL_CREATE_NEWS_COMMENTS_TABLE)
            db.execSQL(SQL_CREATE_NEWS_COMMENTS_INDEX)
            migrateCommentsJsonToCommentsTable(db)
        }
    }

    // 仅在数据库为空时灌入 Mock 数据，避免每次启动重复写入。
    fun seedFromMockIfEmpty(items: List<NewsDetailsVO>) {
        if (items.isEmpty() || queryNewsCount() > 0) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            items.forEach { item ->
                val entity = item.toEntity()
                db.insertWithOnConflict(
                    TABLE_NEWS,
                    null,
                    entity.toContentValues(),
                    SQLiteDatabase.CONFLICT_REPLACE
                )
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
        val sql = "SELECT COUNT(1) FROM $TABLE_NEWS"
        readableDatabase.rawQuery(sql, null).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getInt(0)
            }
        }
        return 0
    }

    // 列表页面使用该方法统一从数据库读取，确保刷新后数据一致。
    fun queryNewsDetailsList(): List<NewsDetailsVO> {
        val result = mutableListOf<NewsDetailsVO>()
        val sql = """
            SELECT $COL_ID, $COL_TITLE, $COL_AUTHOR, $COL_PUBLISH_TIME, $COL_CONTENT,
                   $COL_IMAGE_LOCAL_PATH, $COL_IMAGE_URL
            FROM $TABLE_NEWS
            ORDER BY $COL_PUBLISH_TIME DESC, $COL_ID DESC
        """.trimIndent()

        readableDatabase.rawQuery(sql, null).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(COL_ID)
            val titleIndex = cursor.getColumnIndexOrThrow(COL_TITLE)
            val authorIndex = cursor.getColumnIndexOrThrow(COL_AUTHOR)
            val publishTimeIndex = cursor.getColumnIndexOrThrow(COL_PUBLISH_TIME)
            val contentIndex = cursor.getColumnIndexOrThrow(COL_CONTENT)
            val localPathIndex = cursor.getColumnIndexOrThrow(COL_IMAGE_LOCAL_PATH)
            val imageUrlIndex = cursor.getColumnIndexOrThrow(COL_IMAGE_URL)

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
        val db = writableDatabase
        var result = -1L
        db.beginTransaction()
        try {
            result = db.insertWithOnConflict(
                TABLE_NEWS,
                null,
                news.toContentValues(),
                SQLiteDatabase.CONFLICT_REPLACE
            )
            replaceComments(db, news.id, news.comments)
            pruneCommentsOverLimit(db, news.id)
            pruneNewsOverLimit(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return result
    }

    fun compactIfNeeded() {
        val db = writableDatabase
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
            SELECT $COL_TITLE, $COL_PROFILE, $COL_IMAGE_LOCAL_PATH, $COL_IMAGE_URL
            FROM $TABLE_NEWS
            ORDER BY $COL_PUBLISH_TIME DESC
        """.trimIndent()

        readableDatabase.rawQuery(sql, null).use { cursor ->
            val titleIndex = cursor.getColumnIndexOrThrow(COL_TITLE)
            val profileIndex = cursor.getColumnIndexOrThrow(COL_PROFILE)
            val localPathIndex = cursor.getColumnIndexOrThrow(COL_IMAGE_LOCAL_PATH)
            val imageUrlIndex = cursor.getColumnIndexOrThrow(COL_IMAGE_URL)

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
            SELECT $COL_ID, $COL_TITLE, $COL_AUTHOR, $COL_PUBLISH_TIME, $COL_CONTENT,
                   $COL_IMAGE_LOCAL_PATH, $COL_IMAGE_URL
            FROM $TABLE_NEWS
            WHERE $COL_ID = ?
            LIMIT 1
        """.trimIndent()

        readableDatabase.rawQuery(sql, arrayOf(id)).use { cursor ->
            if (!cursor.moveToFirst()) return null

            val idIndex = cursor.getColumnIndexOrThrow(COL_ID)
            val titleIndex = cursor.getColumnIndexOrThrow(COL_TITLE)
            val authorIndex = cursor.getColumnIndexOrThrow(COL_AUTHOR)
            val publishTimeIndex = cursor.getColumnIndexOrThrow(COL_PUBLISH_TIME)
            val contentIndex = cursor.getColumnIndexOrThrow(COL_CONTENT)
            val localPathIndex = cursor.getColumnIndexOrThrow(COL_IMAGE_LOCAL_PATH)
            val imageUrlIndex = cursor.getColumnIndexOrThrow(COL_IMAGE_URL)

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

    private fun News.toContentValues(): ContentValues {
        return ContentValues().apply {
            put(COL_ID, id)
            put(COL_TITLE, title)
            put(COL_AUTHOR, author)
            put(COL_PUBLISH_TIME, publishTime)
            put(COL_PROFILE, profile)
            put(COL_CONTENT, content)
            put(COL_IMAGE_LOCAL_PATH, imageLocalPath)
            put(COL_IMAGE_URL, imageUrl)
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

    private fun replaceComments(db: SQLiteDatabase, newsId: String, comments: List<String>) {
        db.delete(TABLE_NEWS_COMMENTS, "$COL_NEWS_ID = ?", arrayOf(newsId))
        comments.forEachIndexed { index, text ->
            val values = ContentValues().apply {
                put(COL_NEWS_ID, newsId)
                put(COL_COMMENT_TEXT, text)
                put(COL_COMMENT_ORDER, index)
            }
            db.insert(TABLE_NEWS_COMMENTS, null, values)
        }
    }

    private fun queryCommentsByNewsId(newsId: String): List<String> {
        val comments = mutableListOf<String>()
        val sql = """
            SELECT $COL_COMMENT_TEXT
            FROM $TABLE_NEWS_COMMENTS
            WHERE $COL_NEWS_ID = ?
            ORDER BY $COL_COMMENT_ORDER ASC, $COL_COMMENT_ID ASC
        """.trimIndent()

        readableDatabase.rawQuery(sql, arrayOf(newsId)).use { cursor ->
            val textIndex = cursor.getColumnIndexOrThrow(COL_COMMENT_TEXT)
            while (cursor.moveToNext()) {
                comments.add(cursor.getString(textIndex))
            }
        }
        return comments
    }

    private fun pruneNewsOverLimit(db: SQLiteDatabase) {
        db.execSQL(
            """
            DELETE FROM $TABLE_NEWS
            WHERE $COL_ID IN (
                SELECT $COL_ID
                FROM $TABLE_NEWS
                ORDER BY $COL_PUBLISH_TIME DESC, $COL_ID DESC
                LIMIT -1 OFFSET ?
            )
            """.trimIndent(),
            arrayOf(MAX_NEWS_COUNT)
        )
    }

    private fun pruneCommentsOverLimit(db: SQLiteDatabase, newsId: String) {
        db.execSQL(
            """
            DELETE FROM $TABLE_NEWS_COMMENTS
            WHERE $COL_COMMENT_ID IN (
                SELECT $COL_COMMENT_ID
                FROM $TABLE_NEWS_COMMENTS
                WHERE $COL_NEWS_ID = ?
                ORDER BY $COL_COMMENT_ORDER DESC, $COL_COMMENT_ID DESC
                LIMIT -1 OFFSET ?
            )
            """.trimIndent(),
            arrayOf<Any>(newsId, MAX_COMMENTS_PER_NEWS)
        )
    }

    private fun pruneAllCommentsOverLimit(db: SQLiteDatabase) {
        db.rawQuery("SELECT $COL_ID FROM $TABLE_NEWS", null).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(COL_ID)
            while (cursor.moveToNext()) {
                pruneCommentsOverLimit(db, cursor.getString(idIndex))
            }
        }
    }

    private fun migrateCommentsJsonToCommentsTable(db: SQLiteDatabase) {
        if (!hasColumn(db, TABLE_NEWS, COL_COMMENTS_JSON)) return

        db.rawQuery("SELECT $COL_ID, $COL_COMMENTS_JSON FROM $TABLE_NEWS", null).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(COL_ID)
            val commentsIndex = cursor.getColumnIndexOrThrow(COL_COMMENTS_JSON)
            while (cursor.moveToNext()) {
                val newsId = cursor.getString(idIndex)
                val comments = commentsFromJson(cursor.getString(commentsIndex))
                replaceComments(db, newsId, comments)
            }
        }
    }

    private fun hasColumn(db: SQLiteDatabase, tableName: String, columnName: String): Boolean {
        db.rawQuery("PRAGMA table_info($tableName)", null).use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == columnName) {
                    return true
                }
            }
        }
        return false
    }

    private fun commentsFromJson(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(json)
            buildList {
                for (i in 0 until array.length()) {
                    add(array.optString(i))
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun android.database.Cursor.getStringOrNull(index: Int): String? {
        return if (isNull(index)) null else getString(index)
    }

    companion object {
        const val DATABASE_NAME = "news.db"
        const val DATABASE_VERSION = 3
        const val MAX_NEWS_COUNT = 200
        const val MAX_COMMENTS_PER_NEWS = 100

        const val TABLE_NEWS = "news"
        const val TABLE_NEWS_COMMENTS = "news_comments"

        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_AUTHOR = "author"
        const val COL_PUBLISH_TIME = "publish_time"
        const val COL_PROFILE = "profile"
        const val COL_CONTENT = "content"
        const val COL_COMMENTS_JSON = "comments_json"
        const val COL_IMAGE_LOCAL_PATH = "image_local_path"
        const val COL_IMAGE_URL = "image_url"

        const val COL_COMMENT_ID = "comment_id"
        const val COL_NEWS_ID = "news_id"
        const val COL_COMMENT_TEXT = "comment_text"
        const val COL_COMMENT_ORDER = "comment_order"

        val SQL_CREATE_NEWS_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_NEWS (
                $COL_ID TEXT PRIMARY KEY,
                $COL_TITLE TEXT NOT NULL,
                $COL_AUTHOR TEXT NOT NULL,
                $COL_PUBLISH_TIME TEXT NOT NULL,
                $COL_PROFILE TEXT NOT NULL DEFAULT '',
                $COL_CONTENT TEXT NOT NULL DEFAULT '',
                $COL_IMAGE_LOCAL_PATH TEXT,
                $COL_IMAGE_URL TEXT
            )
        """.trimIndent()

        val SQL_CREATE_NEWS_COMMENTS_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_NEWS_COMMENTS (
                $COL_COMMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NEWS_ID TEXT NOT NULL,
                $COL_COMMENT_TEXT TEXT NOT NULL,
                $COL_COMMENT_ORDER INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY($COL_NEWS_ID) REFERENCES $TABLE_NEWS($COL_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        val SQL_CREATE_NEWS_PUBLISH_TIME_INDEX = """
            CREATE INDEX IF NOT EXISTS idx_news_publish_time
            ON $TABLE_NEWS($COL_PUBLISH_TIME DESC)
        """.trimIndent()

        val SQL_CREATE_NEWS_TITLE_INDEX = """
            CREATE INDEX IF NOT EXISTS idx_news_title
            ON $TABLE_NEWS($COL_TITLE)
        """.trimIndent()

        val SQL_CREATE_NEWS_COMMENTS_INDEX = """
            CREATE INDEX IF NOT EXISTS idx_news_comments_news_id_order
            ON $TABLE_NEWS_COMMENTS($COL_NEWS_ID, $COL_COMMENT_ORDER)
        """.trimIndent()

        val SQL_ADD_PROFILE_COLUMN = """
            ALTER TABLE $TABLE_NEWS
            ADD COLUMN $COL_PROFILE TEXT NOT NULL DEFAULT ''
        """.trimIndent()
    }
}

