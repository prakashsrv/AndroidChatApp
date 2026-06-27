package com.example.chatappandroid.core.di

import javax.inject.Qualifier

/**
 * Qualifier for a [kotlinx.coroutines.CoroutineScope] tied to the application's lifetime.
 * Prefer injecting this over creating raw CoroutineScope instances in singletons.
 */
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
