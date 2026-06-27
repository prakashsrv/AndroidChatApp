package com.example.chatappandroid.feature.chat.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatappandroid.feature.chat.data.stream.FakeChatStream
import com.example.chatappandroid.feature.chat.data.stream.FakeNetworkConfig
import com.example.chatappandroid.feature.chat.domain.model.Message
import com.example.chatappandroid.feature.chat.domain.model.MessageStatus
import com.example.chatappandroid.feature.chat.domain.usecase.ObserveMessagesUseCase
import com.example.chatappandroid.feature.chat.domain.usecase.RetryMessageUseCase
import com.example.chatappandroid.feature.chat.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel
    @Inject
    constructor(
        private val observeMessages: ObserveMessagesUseCase,
        private val sendMessage: SendMessageUseCase,
        private val retryMessage: RetryMessageUseCase,
        // debug only — swap for a real push source when backend exists
        private val fakeChatStream: FakeChatStream,
        private val fakeNetworkConfig: FakeNetworkConfig,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ChatUiState())
        val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

        private val _effects = MutableSharedFlow<ChatEffect>()
        val effects = _effects.asSharedFlow()

        private var isTyping = false

        init {
            viewModelScope.launch {
                observeMessages().collect { messages ->
                    _uiState.update { it.copy(messages = messages) }
                }
            }
            viewModelScope.launch {
                fakeChatStream.isTyping.collect { typing ->
                    _uiState.update { it.copy(isTypingIndicatorVisible = typing) }
                }
            }
        }

        fun onAction(action: ChatAction) {
            when (action) {
                is ChatAction.InputChanged -> _uiState.update { it.copy(inputText = action.text) }
                is ChatAction.SendMessage -> onSend()
                is ChatAction.RetryMessage -> onRetry(action.messageId)
                is ChatAction.SimulateRapidInbound -> simulateRapidInbound()
                is ChatAction.ToggleTyping -> toggleTyping()
                is ChatAction.SimulateNextFailure -> fakeNetworkConfig.scheduleSendFailure()
            }
        }

        private fun onSend() {
            val text = _uiState.value.inputText.trim()
            if (text.isEmpty()) return
            _uiState.update { it.copy(inputText = "") }
            viewModelScope.launch {
                sendMessage(content = text, senderId = "user_local")
                _effects.emit(ChatEffect.ScrollToBottom)
            }
        }

        private fun onRetry(messageId: String) {
            val message = _uiState.value.messages.find { it.id == messageId } ?: return
            viewModelScope.launch {
                retryMessage(message)
                _effects.emit(ChatEffect.ScrollToBottom)
            }
        }

        private fun simulateRapidInbound() {
            viewModelScope.launch {
                val base = System.currentTimeMillis()
                val messages =
                    (1..RAPID_INBOUND_COUNT).map { i ->
                        Message(
                            id = UUID.randomUUID().toString(),
                            content = "Inbound message #$i",
                            senderId = "other_user",
                            clientTimestamp = base + i,
                            status = MessageStatus.SENT,
                            isOwn = false,
                        )
                    }
                fakeChatStream.emitBurst(messages)
                _effects.emit(ChatEffect.ScrollToBottom)
            }
        }

        private fun toggleTyping() {
            isTyping = !isTyping
            fakeChatStream.setTyping(isTyping)
        }

        companion object {
            private const val RAPID_INBOUND_COUNT = 30
        }
    }
