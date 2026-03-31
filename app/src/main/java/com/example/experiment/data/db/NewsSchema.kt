package com.example.experiment.data.db

/**
 * 新闻模块数据库结构定义：统一管理库名、表结构和索引 SQL。
 */
object NewsSchema {
    const val DATABASE_NAME = "app_data.db"
    const val DATABASE_VERSION = 1

    const val MAX_NEWS_COUNT = 200
    const val MAX_COMMENTS_PER_NEWS = 100

    const val DEFAULT_USER_ID = "user_system"
    const val DEFAULT_USERNAME = "系统用户"

    const val TABLE_NEWS = "news"
    const val TABLE_NEWS_CONTENT = "news_content"
    const val TABLE_NEWS_COMMENTS = "news_comments"
    const val TABLE_USERS = "users"

    const val COL_ID = "id"
    const val COL_TITLE = "title"
    const val COL_AUTHOR = "author"
    const val COL_PUBLISH_TIME = "publish_time"
    const val COL_PROFILE = "profile"
    const val COL_CONTENT = "content"
    const val COL_IMAGE_LOCAL_PATH = "image_local_path"
    const val COL_IMAGE_URL = "image_url"

    const val COL_NEWS_CONTENT_NEWS_ID = "news_id"
    const val COL_CONTENT_TEXT = "content_text"
    const val COL_NEWS_CONTENT_UPDATED_AT = "updated_at"

    const val COL_USER_ID = "user_id"
    const val COL_USERNAME = "username"
    const val COL_NICKNAME = "nickname"
    const val COL_USER_AVATAR_LOCAL_PATH = "avatar_local_path"
    const val COL_USER_AVATAR_URL = "avatar_url"
    const val COL_USER_CREATED_AT = "created_at"
    const val COL_USER_UPDATED_AT = "updated_at"
    const val COL_USER_STATUS = "status"

    const val COL_COMMENT_ID = "comment_id"
    const val COL_NEWS_ID = "news_id"
    const val COL_COMMENT_USER_ID = "user_id"
    const val COL_COMMENT_USERNAME_SNAPSHOT = "username_snapshot"
    const val COL_COMMENT_AVATAR_SNAPSHOT = "avatar_snapshot"
    const val COL_COMMENT_TIME = "comment_time"
    const val COL_COMMENT_CONTENT = "comment_content"
    const val COL_COMMENT_ORDER = "comment_order"
    const val COL_COMMENT_STATUS = "status"

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

    val SQL_CREATE_NEWS_CONTENT_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_NEWS_CONTENT (
            $COL_NEWS_CONTENT_NEWS_ID TEXT PRIMARY KEY,
            $COL_CONTENT_TEXT TEXT NOT NULL,
            $COL_NEWS_CONTENT_UPDATED_AT TEXT NOT NULL,
            FOREIGN KEY($COL_NEWS_CONTENT_NEWS_ID) REFERENCES $TABLE_NEWS($COL_ID) ON DELETE CASCADE
        )
    """.trimIndent()

    val SQL_CREATE_USERS_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_USERS (
            $COL_USER_ID TEXT PRIMARY KEY,
            $COL_USERNAME TEXT NOT NULL UNIQUE,
            $COL_NICKNAME TEXT NOT NULL DEFAULT '',
            $COL_USER_AVATAR_LOCAL_PATH TEXT,
            $COL_USER_AVATAR_URL TEXT,
            $COL_USER_CREATED_AT TEXT NOT NULL,
            $COL_USER_UPDATED_AT TEXT NOT NULL,
            $COL_USER_STATUS INTEGER NOT NULL DEFAULT 1
        )
    """.trimIndent()

    val SQL_CREATE_NEWS_COMMENTS_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_NEWS_COMMENTS (
            $COL_COMMENT_ID TEXT PRIMARY KEY,
            $COL_NEWS_ID TEXT NOT NULL,
            $COL_COMMENT_USER_ID TEXT NOT NULL,
            $COL_COMMENT_USERNAME_SNAPSHOT TEXT NOT NULL,
            $COL_COMMENT_AVATAR_SNAPSHOT TEXT,
            $COL_COMMENT_TIME TEXT NOT NULL,
            $COL_COMMENT_CONTENT TEXT NOT NULL,
            $COL_COMMENT_ORDER INTEGER NOT NULL DEFAULT 0,
            $COL_COMMENT_STATUS INTEGER NOT NULL DEFAULT 1,
            FOREIGN KEY($COL_NEWS_ID) REFERENCES $TABLE_NEWS($COL_ID) ON DELETE CASCADE,
            FOREIGN KEY($COL_COMMENT_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID) ON DELETE RESTRICT
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
}
