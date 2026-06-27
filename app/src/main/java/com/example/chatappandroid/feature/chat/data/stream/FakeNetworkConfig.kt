package com.example.chatappandroid.feature.chat.data.stream

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Debug-only seam that lets the UI schedule a simulated network failure on the next send.
 * Injected into both [ChatViewModel] (to set the flag) and [ChatRepositoryImpl] (to consume it).
 */
@Singleton
class FakeNetworkConfig
    @Inject
    constructor() {
        @Volatile private var failNextSend = false

        fun scheduleSendFailure() {
            failNextSend = true
        }

        /** Reads and clears the flag — only one send fails per call. */
        fun consumeFailureFlag(): Boolean {
            val value = failNextSend
            failNextSend = false
            return value
        }
    }
