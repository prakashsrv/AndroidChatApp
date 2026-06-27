package com.example.chatappandroid.feature.chat.domain.model

data class Message(
    val id: String,
    val serverId: String? = null,  // null until server acknowledges
    val content: String,
    val senderId: String,
    val clientTimestamp: Long,
    val serverTimestamp: Long? = null,
    val status: MessageStatus,
    val isOwn: Boolean,
)
