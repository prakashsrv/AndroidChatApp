package com.example.chatappandroid.feature.chat.presentation.chat

import app.cash.turbine.test
import com.example.chatappandroid.feature.chat.domain.model.Message
import com.example.chatappandroid.feature.chat.domain.model.MessageStatus
import com.example.chatappandroid.feature.chat.domain.usecase.ObserveMessagesUseCase
import com.example.chatappandroid.feature.chat.domain.usecase.SendMessageUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var observeMessages: ObserveMessagesUseCase
    private lateinit var sendMessage: SendMessageUseCase
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        observeMessages = mockk()
        sendMessage = mockk(relaxed = true)
        every { observeMessages() } returns flowOf(emptyList())
        viewModel = ChatViewModel(observeMessages, sendMessage)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState has empty messages and input`() = runTest {
        val state = viewModel.uiState.value
        assertTrue(state.messages.isEmpty())
        assertEquals("", state.inputText)
    }

    @Test
    fun `uiState updates when messages are emitted`() = runTest {
        val messages = listOf(fakeMessage("1"), fakeMessage("2"))
        every { observeMessages() } returns flowOf(messages)
        viewModel = ChatViewModel(observeMessages, sendMessage)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(messages, viewModel.uiState.value.messages)
    }

    @Test
    fun `InputChanged action updates inputText in state`() = runTest {
        viewModel.onAction(ChatAction.InputChanged("Hello"))
        assertEquals("Hello", viewModel.uiState.value.inputText)
    }

    @Test
    fun `SendMessage action clears input`() = runTest {
        viewModel.onAction(ChatAction.InputChanged("Hello"))
        viewModel.onAction(ChatAction.SendMessage)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.inputText)
    }

    @Test
    fun `SendMessage action calls sendMessage use case`() = runTest {
        viewModel.onAction(ChatAction.InputChanged("Hello"))
        viewModel.onAction(ChatAction.SendMessage)

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { sendMessage(content = "Hello", senderId = any()) }
    }

    @Test
    fun `SendMessage emits ScrollToBottom effect`() = runTest {
        viewModel.effects.test {
            viewModel.onAction(ChatAction.InputChanged("Hello"))
            viewModel.onAction(ChatAction.SendMessage)

            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(ChatEffect.ScrollToBottom, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SendMessage with blank input does not call use case`() = runTest {
        viewModel.onAction(ChatAction.InputChanged("   "))
        viewModel.onAction(ChatAction.SendMessage)

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { sendMessage(any(), any()) }
    }

    // --- helpers ---

    private fun fakeMessage(id: String) = Message(
        id = id,
        content = "Message $id",
        senderId = "user_1",
        clientTimestamp = System.currentTimeMillis(),
        status = MessageStatus.SENT,
        isOwn = false,
    )
}
