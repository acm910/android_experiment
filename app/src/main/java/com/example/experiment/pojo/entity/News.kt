package com.example.experiment.pojo.entity

import java.io.Serializable

data class News(
    val id: String,
    val title: String,
    val author: String,
    val publishTime: String,
    val profile: String,
    val content: String,
    val comments: List<String> = emptyList(),
    val imageLocalPath: String? = null,
    val imageUrl: String? = null
) : Serializable
