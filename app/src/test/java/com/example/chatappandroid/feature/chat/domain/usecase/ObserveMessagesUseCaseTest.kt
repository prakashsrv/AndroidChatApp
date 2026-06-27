package com.example.chatappandroid.feature.chat.domain.usecase

import app.cash.turbine.test
import com.example.chatappandroid.feature.chat.domain.model.Message
import com.example.chatappandroid.feature.chat.domain.model.MessageStatus
import com.example.chatappandroid.feature.chat.domain.repository.ChatRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObserveMessagesUseCaseTest {

    private lateinit var repository: ChatRepository
    private lateinit var useCase: ObserveMessagesUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = ObserveMessagesUseCase(repository)
    }

    @Test
    fun `invoke emits messages from repository`() = runTest {
        val messages = listOf(fakeMessage("1"), fakeMessage("2"))
        every { repository.observeMessages() } returns flowOf(messages)

        useCase().test {
            assertEquals(messages, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits empty list when no messages`() = runTest {
        every { repository.observeMessages() } returns flowOf(emptyList())

        useCase().test {
            assertEquals(emptyList<Message>(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits multiple updates in order`() = runTest {
        val first = listOf(fakeMessage("1"))
        val second = listOf(fakeMessage("1"), fakeMessage("2"))
        every { repository.observeMessages() } returns flowOf(first, second)

        useCase().test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `messages are ordered by clientTimestamp ascending`() = runTest {
        val messages = listOf(
            fakeMessage(id = "1", clientTimestamp = 1000L),
            fakeMessage(id = "2", clientTimestamp = 2000L),
            fakeMessage(id = "3", clientTimestamp = 3000L),
        )
        every { repository.observeMessages() } returns flowOf(messages)

        useCase().test {
            val result = awaitItem()
            assertEquals(listOf("1", "2", "3"), result.map { it.id })
            awaitComplete()
        }
    }

    // --- helpers ---

    private fun fakeMessage(
        id: String,
        clientTimestamp: Long = System.currentTimeMillis(),
    ) = Message(
        id = id,
        content = "Message $id",
        senderId = "user_1",
        clientTimestamp = clientTimestamp,
        status = MessageStatus.SENT,
        isOwn = false,
    )
}
