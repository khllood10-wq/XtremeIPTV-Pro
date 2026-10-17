package com.xtreme.iptv.model

data class VodItem(
    val streamId: Int,
    val name: String,
    val streamIcon: String,
    val rating: String,
    val containerExtension: String,
    val categoryId: String
)
