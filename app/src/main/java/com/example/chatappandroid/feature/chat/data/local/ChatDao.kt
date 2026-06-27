package com.example.chatappandroid.feature.chat.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM messages ORDER BY clientTimestamp ASC")
    fun observeMessages(): Flow<List<MessageEntity>>

    @Upsert
    suspend fun upsert(message: MessageEntity)
}
