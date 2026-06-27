package com.example.chatappandroid.feature.chat.presentation.chat

sealed interface ChatAction {
    data class InputChanged(val text: String) : ChatAction
    data object SendMessage : ChatAction
    data class RetryMessage(val messageId: String) : ChatAction
}
