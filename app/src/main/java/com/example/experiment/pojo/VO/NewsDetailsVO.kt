package com.example.experiment.pojo.VO

import java.io.Serializable

/**
 * 新闻详情展示模型：承载详情页和列表页共用字段。
 */
data class NewsDetailsVO(
    val id: String,
    val title: String,
    val author: String,
    val publishTime: String,
    val content: String,
    val comments: List<String>,
    val imageLocalPath: String? = null,
    val imageUrl: String? = null
) : Serializable

