package com.example.reelapp

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class VideoItem(
//    val url: String,
//    val likeCount: Int = 0,

    val likeCount: Int,
    val url: String? = null

//    val comment: String? = null

//    val comment: List<String> = emptyList(),
//    val likeCount: Int = 0,
//    val url: String = "url"

//    val comment: String? = null,
//    val likeCount: Int = 0,
//    val url: String? = null
) : Parcelable
