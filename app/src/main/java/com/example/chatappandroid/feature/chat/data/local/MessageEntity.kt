package com.example.chatappandroid.feature.chat.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.chatappandroid.feature.chat.domain.model.MessageStatus

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val content: String,
    val senderId: String,
    val clientTimestamp: Long,
    val serverTimestamp: Long?,
    val status: MessageStatus,
    val isOwn: Boolean,
)
