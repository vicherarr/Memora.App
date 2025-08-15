package com.vicherarr.memora.di

import com.vicherarr.memora.domain.usecase.GetNotesUseCase
import com.vicherarr.memora.domain.usecases.CreateNoteUseCase
import com.vicherarr.memora.domain.usecases.UpdateNoteUseCase
import com.vicherarr.memora.domain.usecases.DeleteNoteUseCase
import com.vicherarr.memora.domain.usecases.SearchNotesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin module for Use Cases following Clean Architecture
 * Use Cases contain business logic and coordinate between repositories and validation
 */
val useCaseModule = module {
    // Existing Use Case
    factoryOf(::GetNotesUseCase)
    
    // New Clean Architecture Use Cases
    single<CreateNoteUseCase> { 
        CreateNoteUseCase(
            notesRepository = get(),
            validationService = get()
        ) 
    }
    
    single<UpdateNoteUseCase> { 
        UpdateNoteUseCase(
            notesRepository = get(),
            validationService = get()
        ) 
    }
    
    single<DeleteNoteUseCase> { 
        DeleteNoteUseCase(
            notesRepository = get()
        ) 
    }
    
    single<SearchNotesUseCase> { 
        SearchNotesUseCase(
            notesRepository = get()
        ) 
    }
}
