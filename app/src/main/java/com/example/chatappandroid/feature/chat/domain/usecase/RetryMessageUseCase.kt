package com.example.chatappandroid.feature.chat.domain.usecase

import com.example.chatappandroid.feature.chat.domain.model.Message
import com.example.chatappandroid.feature.chat.domain.model.MessageStatus
import com.example.chatappandroid.feature.chat.domain.repository.ChatRepository
import javax.inject.Inject

/**
 * Retries a FAILED message in-place: resets it to PENDING (same id, same list position),
 * then reconciles to SENT or FAILED. Never creates a duplicate row.
 */
class RetryMessageUseCase
    @Inject
    constructor(
        private val repository: ChatRepository,
    ) {
        suspend operator fun invoke(message: Message) {
            val pending = message.copy(status = MessageStatus.PENDING)
            repository.updateMessage(pending)

            repository.sendMessageToServer(pending)
                .onSuccess { confirmed ->
                    repository.updateMessage(confirmed.copy(status = MessageStatus.SENT))
                }
                .onFailure {
                    repository.updateMessage(pending.copy(status = MessageStatus.FAILED))
                }
        }
    }
