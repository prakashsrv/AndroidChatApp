package com.example.chatappandroid.feature.chat.presentation.chat

sealed interface ChatEffect {
    data object ScrollToBottom : ChatEffect
    data class ShowError(val message: String) : ChatEffect
}
