package com.example.experiment.pojo.VO

data class NewsProfileVO(
	val title: String,
	val profile: String,
	val imageLocalPath: String? = null,
	val imageUrl: String? = null
)
