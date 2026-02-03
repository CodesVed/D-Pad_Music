package com.example.mediaplayer_project

data class Song(
    val id: Int,
//    val albumId: Int,
    val title: String,
    val artist: String,
    val path: String,
    val duration: Int
)
