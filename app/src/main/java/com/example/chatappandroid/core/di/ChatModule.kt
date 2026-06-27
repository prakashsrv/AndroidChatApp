package com.example.chatappandroid.core.di

import com.example.chatappandroid.feature.chat.data.repository.ChatRepositoryImpl
import com.example.chatappandroid.feature.chat.data.stream.ChatMessageStream
import com.example.chatappandroid.feature.chat.data.stream.FakeChatStream
import com.example.chatappandroid.feature.chat.domain.repository.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatModule {
    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindChatMessageStream(impl: FakeChatStream): ChatMessageStream
}
