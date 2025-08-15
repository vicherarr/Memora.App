package com.vicherarr.memora.di

import com.vicherarr.memora.data.mappers.AttachmentDomainMapper
import com.vicherarr.memora.data.mappers.NoteDomainMapper
import com.vicherarr.memora.data.mappers.CategoryDomainMapper
import org.koin.dsl.module

/**
 * Koin module for Domain Mappers following Clean Architecture
 * Separates mapping concerns from repository logic
 */
val mapperModule = module {
    single<AttachmentDomainMapper> { AttachmentDomainMapper() }
    single<NoteDomainMapper> { 
        NoteDomainMapper(
            attachmentsDao = get(),
            attachmentMapper = get(),
            noteCategoriesDao = get(),
            categoryMapper = get()
        ) 
    }
    single<CategoryDomainMapper> { CategoryDomainMapper() }
}