package com.example.experiment.data.store

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.experiment.data.db.AppDatabaseHelper
import com.example.experiment.data.db.NewsSchema

/**
 * 用户数据访问层：管理默认用户和评论所需的用户快照。
 */
class UserStore(private val dbHelper: AppDatabaseHelper) {

    fun ensureDefaultUser() {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(NewsSchema.COL_USER_ID, NewsSchema.DEFAULT_USER_ID)
            put(NewsSchema.COL_USERNAME, NewsSchema.DEFAULT_USERNAME)
            put(NewsSchema.COL_NICKNAME, NewsSchema.DEFAULT_USERNAME)
            put(NewsSchema.COL_USER_AVATAR_LOCAL_PATH, "ic_launcher_round")
            put(NewsSchema.COL_USER_AVATAR_URL, "")
            put(NewsSchema.COL_USER_STATUS, 1)
            put(NewsSchema.COL_USER_CREATED_AT, currentTimeText())
            put(NewsSchema.COL_USER_UPDATED_AT, currentTimeText())
        }
        db.insertWithOnConflict(
            NewsSchema.TABLE_USERS,
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )
    }

    fun resolveDefaultUserSnapshot(): Triple<String, String, String?> {
        val db = dbHelper.writableDatabase
        ensureDefaultUser()

        val sql = """
            SELECT ${NewsSchema.COL_USER_ID}, ${NewsSchema.COL_USERNAME}, ${NewsSchema.COL_USER_AVATAR_LOCAL_PATH}
            FROM ${NewsSchema.TABLE_USERS}
            WHERE ${NewsSchema.COL_USER_STATUS} = 1
            ORDER BY ${NewsSchema.COL_USER_CREATED_AT} ASC
            LIMIT 1
        """.trimIndent()

        db.rawQuery(sql, null).use { cursor ->
            if (cursor.moveToFirst()) {
                val userId = cursor.getString(cursor.getColumnIndexOrThrow(NewsSchema.COL_USER_ID))
                val username = cursor.getString(cursor.getColumnIndexOrThrow(NewsSchema.COL_USERNAME))
                val avatarIndex = cursor.getColumnIndexOrThrow(NewsSchema.COL_USER_AVATAR_LOCAL_PATH)
                val avatar = if (cursor.isNull(avatarIndex)) null else cursor.getString(avatarIndex)
                return Triple(userId, username, avatar)
            }
        }
        return Triple(NewsSchema.DEFAULT_USER_ID, NewsSchema.DEFAULT_USERNAME, "ic_launcher_round")
    }

    private fun currentTimeText(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
}

