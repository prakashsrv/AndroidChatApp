package com.example.chatappandroid.feature.chat.domain.usecase

import com.example.chatappandroid.feature.chat.domain.model.Message
import com.example.chatappandroid.feature.chat.domain.model.MessageStatus
import com.example.chatappandroid.feature.chat.domain.repository.ChatRepository
import java.util.UUID
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke(content: String, senderId: String) {
        val pending = Message(
            id = UUID.randomUUID().toString(),
            content = content,
            senderId = senderId,
            clientTimestamp = System.currentTimeMillis(),
            status = MessageStatus.PENDING,
            isOwn = true,
        )
        repository.insertPendingMessage(pending)

        repository.sendMessageToServer(pending)
            .onSuccess { confirmed ->
                repository.updateMessage(confirmed.copy(status = MessageStatus.SENT))
            }
            .onFailure {
                repository.updateMessage(pending.copy(status = MessageStatus.FAILED))
            }
    }
}
