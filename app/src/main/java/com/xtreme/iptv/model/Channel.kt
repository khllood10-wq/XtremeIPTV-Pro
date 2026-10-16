package com.xtreme.iptv.model

data class Channel(
    val num: Int,
    val name: String,
    val streamId: Int,
    val streamIcon: String,
    val categoryId: String
)
