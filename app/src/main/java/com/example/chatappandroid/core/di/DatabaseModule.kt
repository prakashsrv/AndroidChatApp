package com.example.chatappandroid.core.di

import android.content.Context
import androidx.room.Room
import com.example.chatappandroid.core.database.ChatDatabase
import com.example.chatappandroid.feature.chat.data.local.ChatDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideChatDatabase(
        @ApplicationContext context: Context,
    ): ChatDatabase =
        Room
            .databaseBuilder(context, ChatDatabase::class.java, "chat.db")
            .fallbackToDestructiveMigration(dropAllTables = true) // dev only — add migrations before release
            .build()

    @Provides
    fun provideChatDao(db: ChatDatabase): ChatDao = db.chatDao()
}
