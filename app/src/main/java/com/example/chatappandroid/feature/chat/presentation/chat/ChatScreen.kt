package com.example.chatappandroid.feature.chat.presentation.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chatappandroid.feature.chat.presentation.components.ChatInputBar
import com.example.chatappandroid.feature.chat.presentation.components.DebugPanel
import com.example.chatappandroid.feature.chat.presentation.components.MessageBubble
import com.example.chatappandroid.feature.chat.presentation.components.TypingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ChatEffect.ScrollToBottom -> {
                    if (uiState.messages.isNotEmpty()) {
                        listState.animateScrollToItem(uiState.messages.lastIndex)
                    }
                }
                is ChatEffect.ShowError -> Unit // TODO: show snackbar
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Chat") })
        },
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                HorizontalDivider()
                ChatInputBar(
                    value = uiState.inputText,
                    onValueChange = { viewModel.onAction(ChatAction.InputChanged(it)) },
                    onSend = { viewModel.onAction(ChatAction.SendMessage) },
                )
                DebugPanel(
                    onSimulateRapidInbound = { viewModel.onAction(ChatAction.SimulateRapidInbound) },
                    onToggleTyping = { viewModel.onAction(ChatAction.ToggleTyping) },
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        ) {
            items(
                items = uiState.messages,
                key = { it.id },
            ) { message ->
                MessageBubble(
                    message = message,
                    onRetry = { viewModel.onAction(ChatAction.RetryMessage(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                )
            }

            if (uiState.isTypingIndicatorVisible) {
                item(key = "typing_indicator") {
                    TypingIndicator(
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}
