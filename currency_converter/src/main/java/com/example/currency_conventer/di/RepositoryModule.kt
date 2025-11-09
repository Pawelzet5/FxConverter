package com.example.currency_conventer.di

import com.example.currency_conventer.data.repository.FxRatesRepositoryImpl
import com.example.currency_conventer.domain.repository.FxRatesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFxRatesRepository(
        impl: FxRatesRepositoryImpl
    ): FxRatesRepository
}