package com.example.stray.data

data class Post(
    var postId: String = "",
    val title: String = "",
    val content: String = "",
    val author: String = "", // 사용자의 displayName 또는 UID 등으로 대체
    val imageUrls: List<Any>? = null, // 이미지의 Firebase Storage URL
    val timestamp: String = "",
    var hits: Int = 0,  // Add hits field
    val comments: List<Comment> = emptyList()
)

data class Comment(
    val author: String = "",
    val content: String = "",
    val timestamp: String = ""
)

enum class SearchType {
    TITLE,
    CONTENT,
    AUTHOR,  // Add a new case for author
    BOTH
}

