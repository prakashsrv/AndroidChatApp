package com.example.chatappandroid.feature.chat.data.repository

import com.example.chatappandroid.feature.chat.data.local.ChatDao
import com.example.chatappandroid.feature.chat.data.mapper.toDomain
import com.example.chatappandroid.feature.chat.data.mapper.toEntity
import com.example.chatappandroid.feature.chat.data.stream.ChatMessageStream
import com.example.chatappandroid.feature.chat.domain.model.Message
import com.example.chatappandroid.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val dao: ChatDao,
    private val stream: ChatMessageStream,
) : ChatRepository {

    override fun observeMessages(): Flow<List<Message>> =
        dao.observeMessages().map { entities -> entities.map { it.toDomain() } }

    override suspend fun insertPendingMessage(message: Message) =
        dao.upsert(message.toEntity())

    override suspend fun updateMessage(message: Message) =
        dao.upsert(message.toEntity())

    override suspend fun sendMessageToServer(message: Message): Result<Message> =
        TODO("Wire to real backend when available")
}
