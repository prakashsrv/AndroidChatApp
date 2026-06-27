package com.example.chatappandroid.feature.chat.data.mapper

import com.example.chatappandroid.feature.chat.data.local.MessageEntity
import com.example.chatappandroid.feature.chat.domain.model.Message

fun MessageEntity.toDomain(): Message = Message(
    id = id,
    content = content,
    senderId = senderId,
    clientTimestamp = clientTimestamp,
    serverTimestamp = serverTimestamp,
    status = status,
    isOwn = isOwn,
)

fun Message.toEntity(): MessageEntity = MessageEntity(
    id = id,
    content = content,
    senderId = senderId,
    clientTimestamp = clientTimestamp,
    serverTimestamp = serverTimestamp,
    status = status,
    isOwn = isOwn,
)
