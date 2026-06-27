package com.example.chatappandroid.feature.chat.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.chatappandroid.feature.chat.domain.model.MessageStatus

@Entity(
    tableName = "messages",
    indices = [Index(value = ["serverId"], unique = true, orders = [Index.Order.ASC])],
)
data class MessageEntity(
    // client UUID — stable from PENDING through SENT
    @PrimaryKey val id: String,
    // server-assigned ID; unique index blocks duplicate inbound messages on reconnect
    @ColumnInfo(name = "serverId") val serverId: String?,
    val content: String,
    val senderId: String,
    val clientTimestamp: Long,
    val serverTimestamp: Long?,
    val status: MessageStatus,
    val isOwn: Boolean,
)
