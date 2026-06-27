package com.example.chatappandroid.feature.chat.domain.usecase

import com.example.chatappandroid.feature.chat.domain.model.Message
import com.example.chatappandroid.feature.chat.domain.model.MessageStatus
import com.example.chatappandroid.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class SendMessageUseCaseTest {

    private lateinit var repository: ChatRepository
    private lateinit var useCase: SendMessageUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = SendMessageUseCase(repository)
    }

    @Test
    fun `invoke inserts pending message before contacting server`() = runTest {
        coEvery { repository.sendMessageToServer(any()) } returns Result.success(fakeConfirmed())

        useCase(content = "Hello", senderId = "user_1")

        coVerify(ordering = io.mockk.Ordering.ORDERED) {
            repository.insertPendingMessage(any())
            repository.sendMessageToServer(any())
        }
    }

    @Test
    fun `pending message has correct content, senderId and PENDING status`() = runTest {
        val slot = slot<Message>()
        coEvery { repository.insertPendingMessage(capture(slot)) } returns Unit
        coEvery { repository.sendMessageToServer(any()) } returns Result.success(fakeConfirmed())

        useCase(content = "Hello", senderId = "user_1")

        val captured = slot.captured
        assertEquals("Hello", captured.content)
        assertEquals("user_1", captured.senderId)
        assertEquals(MessageStatus.PENDING, captured.status)
        assertNotNull(captured.id)
    }

    @Test
    fun `on server success message is updated to SENT`() = runTest {
        val confirmed = fakeConfirmed()
        coEvery { repository.sendMessageToServer(any()) } returns Result.success(confirmed)
        val slot = slot<Message>()
        coEvery { repository.updateMessage(capture(slot)) } returns Unit

        useCase(content = "Hello", senderId = "user_1")

        assertEquals(MessageStatus.SENT, slot.captured.status)
    }

    @Test
    fun `on server failure message is updated to FAILED`() = runTest {
        coEvery { repository.sendMessageToServer(any()) } returns Result.failure(RuntimeException("network error"))
        val slot = slot<Message>()
        coEvery { repository.updateMessage(capture(slot)) } returns Unit

        useCase(content = "Hello", senderId = "user_1")

        assertEquals(MessageStatus.FAILED, slot.captured.status)
    }

    @Test
    fun `pending message is own message`() = runTest {
        val slot = slot<Message>()
        coEvery { repository.insertPendingMessage(capture(slot)) } returns Unit
        coEvery { repository.sendMessageToServer(any()) } returns Result.success(fakeConfirmed())

        useCase(content = "Hello", senderId = "user_1")

        assertEquals(true, slot.captured.isOwn)
    }

    @Test
    fun `pending message has a non-zero clientTimestamp`() = runTest {
        val slot = slot<Message>()
        coEvery { repository.insertPendingMessage(capture(slot)) } returns Unit
        coEvery { repository.sendMessageToServer(any()) } returns Result.success(fakeConfirmed())

        useCase(content = "Hello", senderId = "user_1")

        assert(slot.captured.clientTimestamp > 0)
    }

    // --- helpers ---

    private fun fakeConfirmed() = Message(
        id = "server_id_1",
        content = "Hello",
        senderId = "user_1",
        clientTimestamp = System.currentTimeMillis(),
        serverTimestamp = System.currentTimeMillis(),
        status = MessageStatus.SENT,
        isOwn = true,
    )
}
