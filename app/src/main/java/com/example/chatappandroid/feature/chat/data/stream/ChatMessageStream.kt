package com.example.chatappandroid.feature.chat.data.stream

import com.example.chatappandroid.feature.chat.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatMessageStream {
    val messages: Flow<Message>
    val isTyping: Flow<Boolean>
}
