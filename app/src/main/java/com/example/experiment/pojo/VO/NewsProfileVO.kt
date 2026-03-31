package com.example.experiment.pojo.VO

/**
 * 新闻简介模型：用于轻量列表或预览场景。
 */
data class NewsProfileVO(
	val title: String,
	val profile: String,
	val imageLocalPath: String? = null,
	val imageUrl: String? = null
)
