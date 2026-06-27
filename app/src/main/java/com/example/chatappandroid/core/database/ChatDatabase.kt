package com.example.chatappandroid.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.chatappandroid.feature.chat.data.local.ChatDao
import com.example.chatappandroid.feature.chat.data.local.MessageEntity

@Database(
    entities = [MessageEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}
