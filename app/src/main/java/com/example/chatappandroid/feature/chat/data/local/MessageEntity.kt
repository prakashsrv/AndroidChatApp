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
    @PrimaryKey val id: String,           // client UUID — stable from PENDING through SENT
    @ColumnInfo(name = "serverId") val serverId: String?, // server-assigned ID; unique once known
    val content: String,
    val senderId: String,
    val clientTimestamp: Long,
    val serverTimestamp: Long?,
    val status: MessageStatus,
    val isOwn: Boolean,
)
