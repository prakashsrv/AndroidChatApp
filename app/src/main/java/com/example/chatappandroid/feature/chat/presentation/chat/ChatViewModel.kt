package com.example.chatappandroid.feature.chat.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatappandroid.feature.chat.domain.usecase.ObserveMessagesUseCase
import com.example.chatappandroid.feature.chat.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val observeMessages: ObserveMessagesUseCase,
    private val sendMessage: SendMessageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ChatEffect>()
    val effects = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            observeMessages().collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    fun onAction(action: ChatAction) {
        when (action) {
            is ChatAction.InputChanged -> _uiState.update { it.copy(inputText = action.text) }
            is ChatAction.SendMessage -> onSend()
            is ChatAction.RetryMessage -> Unit // TODO
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
}
