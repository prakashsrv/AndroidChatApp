package com.example.chatappandroid.feature.chat.presentation.chat

import com.example.chatappandroid.feature.chat.domain.model.Message

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val isTypingIndicatorVisible: Boolean = false,
    val isLoading: Boolean = false,
    // Debug: true means the next send will be forced to fail
    val isFailNextSendArmed: Boolean = false,
)
