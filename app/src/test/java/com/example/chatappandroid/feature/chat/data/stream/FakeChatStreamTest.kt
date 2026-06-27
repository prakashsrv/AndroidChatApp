package com.example.chatappandroid.feature.chat.data.stream

import app.cash.turbine.test
import com.example.chatappandroid.feature.chat.domain.model.Message
import com.example.chatappandroid.feature.chat.domain.model.MessageStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FakeChatStreamTest {
    private lateinit var stream: FakeChatStream

    @Before
    fun setUp() {
        stream = FakeChatStream()
    }

    @Test
    fun `emit delivers single message to collector`() =
        runTest {
            val message = fakeMessage("1")

            stream.messages.test {
                stream.emit(message)
                assertEquals(message, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emitBurst delivers all messages in order`() =
        runTest {
            val messages = (1..5).map { fakeMessage("$it") }

            stream.messages.test {
                stream.emitBurst(messages)
                messages.forEach { expected ->
                    assertEquals(expected, awaitItem())
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emitBurst of 20 messages delivers all without loss`() =
        runTest {
            val messages = (1..20).map { fakeMessage("$it") }

            stream.messages.test {
                stream.emitBurst(messages)
                val received = (1..20).map { awaitItem() }
                assertEquals(messages, received)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `isTyping defaults to false`() =
        runTest {
            stream.isTyping.test {
                assertEquals(false, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `setTyping true updates isTyping`() =
        runTest {
            stream.isTyping.test {
                awaitItem() // consume initial false
                stream.setTyping(true)
                assertEquals(true, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `setTyping toggles correctly`() =
        runTest {
            stream.isTyping.test {
                awaitItem() // false
                stream.setTyping(true)
                assertEquals(true, awaitItem())
                stream.setTyping(false)
                assertEquals(false, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `multiple collectors each receive emitted message`() =
        runTest {
            val message = fakeMessage("1")

            stream.messages.test {
                stream.emit(message)
                assertEquals(message, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            stream.messages.test {
                stream.emit(message)
                assertEquals(message, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    // --- helpers ---

    private fun fakeMessage(id: String) =
        Message(
            id = id,
            content = "Message $id",
            senderId = "other_user",
            clientTimestamp = id.toLong() * 1000L,
            status = MessageStatus.SENT,
            isOwn = false,
        )
}
