package com.example.experiment.pojo

data class News(
	val title: String,
	val content: String,
	val imageLocalPath: String? = null,
	val imageUrl: String? = null
)
