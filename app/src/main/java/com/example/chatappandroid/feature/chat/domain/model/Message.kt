package com.example.chatappandroid.feature.chat.domain.model

data class Message(
    val id: String,
    // null until server acknowledges; used for deduplication of inbound messages
    val serverId: String? = null,
    val content: String,
    val senderId: String,
    val clientTimestamp: Long,
    val serverTimestamp: Long? = null,
    val status: MessageStatus,
    val isOwn: Boolean,
)
