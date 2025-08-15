package com.vicherarr.memora.di

import com.vicherarr.memora.domain.usecase.GetNotesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val useCaseModule = module {
    factoryOf(::GetNotesUseCase)
}
