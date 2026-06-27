package com.example.chatappandroid.feature.chat.domain.repository

import com.example.chatappandroid.feature.chat.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(): Flow<List<Message>>
    suspend fun insertPendingMessage(message: Message)
    suspend fun sendMessageToServer(message: Message): Result<Message>
    suspend fun updateMessage(message: Message)
}
