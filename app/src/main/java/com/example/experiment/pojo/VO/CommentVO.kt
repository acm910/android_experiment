package com.example.experiment.pojo.VO

import java.io.Serializable

/**
 * 评论展示模型：用于详情页评论列表渲染。
 */
data class CommentVO(
    val commentId: String,
    val newsId: String,
    val userId: String,
    val username: String,
    val avatarLocalPath: String? = null,
    val avatarUrl: String? = null,
    val commentTime: String,
    val content: String
) : Serializable

