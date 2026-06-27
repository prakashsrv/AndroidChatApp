package com.example.chatappandroid.feature.chat.data.stream

import com.example.chatappandroid.feature.chat.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeChatStream @Inject constructor() : ChatMessageStream {

    private val _messages = MutableSharedFlow<Message>()
    override val messages: Flow<Message> = _messages

    private val _isTyping = MutableStateFlow(false)
    override val isTyping: Flow<Boolean> = _isTyping

    suspend fun emit(message: Message) = _messages.emit(message)

    suspend fun emitBurst(messages: List<Message>) = messages.forEach { _messages.emit(it) }

    fun setTyping(typing: Boolean) { _isTyping.value = typing }
}
