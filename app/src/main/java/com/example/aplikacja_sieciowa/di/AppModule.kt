package com.example.aplikacja_sieciowa.di

import com.example.aplikacja_sieciowa.data.repository.IRCRepositoryImpl
import com.example.aplikacja_sieciowa.domain.repository.IRCRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindIRCRepository(
        ircRepositoryImpl: IRCRepositoryImpl
    ): IRCRepository
}