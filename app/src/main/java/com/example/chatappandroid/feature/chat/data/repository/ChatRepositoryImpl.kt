package com.example.chatappandroid.feature.chat.data.repository

import com.example.chatappandroid.core.di.ApplicationScope
import com.example.chatappandroid.feature.chat.data.local.ChatDao
import com.example.chatappandroid.feature.chat.data.mapper.toDomain
import com.example.chatappandroid.feature.chat.data.mapper.toEntity
import com.example.chatappandroid.feature.chat.data.stream.ChatMessageStream
import com.example.chatappandroid.feature.chat.data.stream.FakeNetworkConfig
import com.example.chatappandroid.feature.chat.domain.model.Message
import com.example.chatappandroid.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl
    @Inject
    constructor(
        private val dao: ChatDao,
        private val stream: ChatMessageStream,
        private val fakeNetworkConfig: FakeNetworkConfig,
        @ApplicationScope private val scope: CoroutineScope,
    ) : ChatRepository {
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
            // Simulate network delay — replace with real API call when backend exists
            kotlinx.coroutines.delay(SIMULATED_DELAY_MS)
            return if (fakeNetworkConfig.consumeFailureFlag()) {
                Result.failure(IOException("Simulated network failure"))
            } else {
                Result.success(message.copy(serverTimestamp = System.currentTimeMillis()))
            }
        }

        companion object {
            private const val SIMULATED_DELAY_MS = 500L
        }
    }
