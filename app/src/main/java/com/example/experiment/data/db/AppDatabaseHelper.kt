package com.example.experiment.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * 应用数据库入口：只维护当前表结构，不保留历史版本迁移代码。
 */
class AppDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, NewsSchema.DATABASE_NAME, null, NewsSchema.DATABASE_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(NewsSchema.SQL_CREATE_NEWS_TABLE)
        db.execSQL(NewsSchema.SQL_CREATE_NEWS_PUBLISH_TIME_INDEX)
        db.execSQL(NewsSchema.SQL_CREATE_NEWS_TITLE_INDEX)
        db.execSQL(NewsSchema.SQL_CREATE_USERS_TABLE)
        db.execSQL(NewsSchema.SQL_CREATE_NEWS_CONTENT_TABLE)
        db.execSQL(NewsSchema.SQL_CREATE_NEWS_COMMENTS_TABLE)
        db.execSQL(NewsSchema.SQL_CREATE_NEWS_COMMENTS_INDEX)
        ensureDefaultUser(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion >= newVersion) return
        // 当前阶段未上线，不做历史迁移；直接重建为最新结构。
        db.execSQL("DROP TABLE IF EXISTS ${NewsSchema.TABLE_NEWS_COMMENTS}")
        db.execSQL("DROP TABLE IF EXISTS ${NewsSchema.TABLE_NEWS_CONTENT}")
        db.execSQL("DROP TABLE IF EXISTS ${NewsSchema.TABLE_USERS}")
        db.execSQL("DROP TABLE IF EXISTS ${NewsSchema.TABLE_NEWS}")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 允许开发阶段回退版本号，降级时也直接重建。
        onUpgrade(db, newVersion, oldVersion)
    }

    private fun ensureDefaultUser(db: SQLiteDatabase) {
        db.execSQL(
            """
            INSERT OR IGNORE INTO ${NewsSchema.TABLE_USERS}(
                ${NewsSchema.COL_USER_ID},
                ${NewsSchema.COL_USERNAME},
                ${NewsSchema.COL_NICKNAME},
                ${NewsSchema.COL_USER_AVATAR_LOCAL_PATH},
                ${NewsSchema.COL_USER_AVATAR_URL},
                ${NewsSchema.COL_USER_STATUS},
                ${NewsSchema.COL_USER_CREATED_AT},
                ${NewsSchema.COL_USER_UPDATED_AT}
            ) VALUES(
                '${NewsSchema.DEFAULT_USER_ID}',
                '${NewsSchema.DEFAULT_USERNAME}',
                '${NewsSchema.DEFAULT_USERNAME}',
                'ic_launcher_round',
                '',
                1,
                strftime('%Y-%m-%d %H:%M', 'now'),
                strftime('%Y-%m-%d %H:%M', 'now')
            )
            """.trimIndent()
        )
    }
}

