package com.example.chatappandroid.feature.chat.data.repository

import com.example.chatappandroid.feature.chat.data.local.ChatDao
import com.example.chatappandroid.feature.chat.data.mapper.toDomain
import com.example.chatappandroid.feature.chat.data.mapper.toEntity
import com.example.chatappandroid.feature.chat.data.stream.ChatMessageStream
import com.example.chatappandroid.feature.chat.domain.model.Message
import com.example.chatappandroid.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl
    @Inject
    constructor(
        private val dao: ChatDao,
        private val stream: ChatMessageStream,
    ) : ChatRepository {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        init {
            scope.launch {
                stream.messages.collect { message ->
                    dao.upsert(message.toEntity())
                }
            }
        }

        override fun observeMessages(): Flow<List<Message>> =
            dao.observeMessages().map { entities -> entities.map { it.toDomain() } }

        override suspend fun insertPendingMessage(message: Message) = dao.upsert(message.toEntity())

        override suspend fun updateMessage(message: Message) = dao.upsert(message.toEntity())

        override suspend fun sendMessageToServer(message: Message): Result<Message> {
            // Simulate network delay and success — replace with real API call when backend exists
            kotlinx.coroutines.delay(SIMULATED_DELAY_MS)
            return Result.success(message.copy(serverTimestamp = System.currentTimeMillis()))
        }

        companion object {
            private const val SIMULATED_DELAY_MS = 500L
        }
    }
