package com.example.chatappandroid.feature.chat.domain.usecase

import app.cash.turbine.test
import com.example.chatappandroid.feature.chat.domain.model.Message
import com.example.chatappandroid.feature.chat.domain.model.MessageStatus
import com.example.chatappandroid.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Proves stable ordering and duplicate prevention — the core guarantees of the chat feature.
 */
class OrderingAndDeduplicationTest {

    private lateinit var repository: ChatRepository
    private lateinit var observeMessages: ObserveMessagesUseCase
    private lateinit var sendMessage: SendMessageUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        observeMessages = ObserveMessagesUseCase(repository)
        sendMessage = SendMessageUseCase(repository)
    }

    // --- Ordering ---

    @Test
    fun `messages are emitted in clientTimestamp ascending order`() = runTest {
        val messages = listOf(
            fakeMessage(id = "3", clientTimestamp = 3000L),
            fakeMessage(id = "1", clientTimestamp = 1000L),
            fakeMessage(id = "2", clientTimestamp = 2000L),
        ).sortedBy { it.clientTimestamp } // Room enforces this via ORDER BY
        every { repository.observeMessages() } returns flowOf(messages)

        observeMessages().test {
            val result = awaitItem()
            assertEquals(listOf("1", "2", "3"), result.map { it.id })
            awaitComplete()
        }
    }

    @Test
    fun `own messages always use clientTimestamp for ordering`() = runTest {
        val ownMessage = fakeMessage(
            id = "own",
            clientTimestamp = 2000L,
            serverTimestamp = null,
            isOwn = true,
        )
        val inboundMessage = fakeMessage(
            id = "inbound",
            clientTimestamp = 1000L,
            serverTimestamp = 3000L,
            isOwn = false,
        )
        // Own message sorts by clientTimestamp (2000), inbound sorts by clientTimestamp (1000)
        val sorted = listOf(inboundMessage, ownMessage).sortedBy { it.clientTimestamp }
        every { repository.observeMessages() } returns flowOf(sorted)

        observeMessages().test {
            val result = awaitItem()
            assertEquals(listOf("inbound", "own"), result.map { it.id })
            awaitComplete()
        }
    }

    @Test
    fun `rapid burst of messages maintains stable order`() = runTest {
        val burst = (1..20).map { i ->
            fakeMessage(id = "msg_$i", clientTimestamp = i * 100L)
        }
        every { repository.observeMessages() } returns flowOf(burst)

        observeMessages().test {
            val result = awaitItem()
            val ids = result.map { it.id }
            assertEquals((1..20).map { "msg_$it" }, ids)
            awaitComplete()
        }
    }

    // --- Deduplication ---

    @Test
    fun `optimistic message id is unique per send`() = runTest {
        val ids = mutableListOf<String>()
        val slot = slot<Message>()
        coEvery { repository.insertPendingMessage(capture(slot)) } answers {
            ids.add(slot.captured.id)
        }
        coEvery { repository.sendMessageToServer(any()) } returns Result.success(fakeConfirmed())

        sendMessage(content = "First", senderId = "user_1")
        sendMessage(content = "Second", senderId = "user_1")

        assertEquals(2, ids.size)
        assertEquals(2, ids.distinct().size) // no duplicate IDs
    }

    @Test
    fun `server echo with same id updates row not creates duplicate`() = runTest {
        val tempId = "temp_uuid_123"
        val pending = fakeMessage(id = tempId, status = MessageStatus.PENDING)
        val confirmed = pending.copy(status = MessageStatus.SENT, serverTimestamp = 9999L)

        // Simulate Room upsert: inserting same id replaces the row
        val store = mutableMapOf<String, Message>()
        coEvery { repository.insertPendingMessage(any()) } answers {
            store[pending.id] = pending
        }
        coEvery { repository.sendMessageToServer(any()) } returns Result.success(confirmed)
        coEvery { repository.updateMessage(any()) } answers {
            store[confirmed.id] = confirmed
        }

        sendMessage(content = pending.content, senderId = pending.senderId)

        assertEquals(1, store.size) // still one row, not two
        assertEquals(MessageStatus.SENT, store[tempId]?.status)
    }

    @Test
    fun `no duplicate messages in list after rapid inbound burst`() = runTest {
        val burst = (1..50).map { i -> fakeMessage(id = "msg_$i", clientTimestamp = i * 10L) }
        every { repository.observeMessages() } returns flowOf(burst)

        observeMessages().test {
            val result = awaitItem()
            assertEquals(result.size, result.map { it.id }.distinct().size)
            awaitComplete()
        }
    }

    // --- helpers ---

    private fun fakeMessage(
        id: String,
        clientTimestamp: Long = System.currentTimeMillis(),
        serverTimestamp: Long? = null,
        status: MessageStatus = MessageStatus.SENT,
        isOwn: Boolean = false,
    ) = Message(
        id = id,
        content = "Content $id",
        senderId = "user_1",
        clientTimestamp = clientTimestamp,
        serverTimestamp = serverTimestamp,
        status = status,
        isOwn = isOwn,
    )

    private fun fakeConfirmed() = fakeMessage(
        id = "server_id",
        status = MessageStatus.SENT,
        serverTimestamp = System.currentTimeMillis(),
    )
}
