package com.example.experiment.data.store

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.experiment.data.db.AppDatabaseHelper
import com.example.experiment.data.db.NewsSchema

/**
 * 正文数据访问层：处理 news_content 表的写入与查询。
 */
class ContentStore(private val dbHelper: AppDatabaseHelper) {

    fun upsertNewsContent(db: SQLiteDatabase, newsId: String, content: String) {
        val values = ContentValues().apply {
            put(NewsSchema.COL_NEWS_CONTENT_NEWS_ID, newsId)
            put(NewsSchema.COL_CONTENT_TEXT, content)
            put(NewsSchema.COL_NEWS_CONTENT_UPDATED_AT, currentTimeText())
        }
        db.insertWithOnConflict(
            NewsSchema.TABLE_NEWS_CONTENT,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun backfillNewsContent(db: SQLiteDatabase) {
        db.execSQL(
            """
            INSERT OR IGNORE INTO ${NewsSchema.TABLE_NEWS_CONTENT}(
                ${NewsSchema.COL_NEWS_CONTENT_NEWS_ID},
                ${NewsSchema.COL_CONTENT_TEXT},
                ${NewsSchema.COL_NEWS_CONTENT_UPDATED_AT}
            )
            SELECT ${NewsSchema.COL_ID}, ${NewsSchema.COL_CONTENT}, strftime('%Y-%m-%d %H:%M', 'now')
            FROM ${NewsSchema.TABLE_NEWS}
            """.trimIndent()
        )
    }

    fun queryContent(newsId: String): String? {
        val db = dbHelper.readableDatabase
        val sql = """
            SELECT ${NewsSchema.COL_CONTENT_TEXT}
            FROM ${NewsSchema.TABLE_NEWS_CONTENT}
            WHERE ${NewsSchema.COL_NEWS_CONTENT_NEWS_ID} = ?
            LIMIT 1
        """.trimIndent()

        db.rawQuery(sql, arrayOf(newsId)).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }
        return null
    }

    private fun currentTimeText(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
}

