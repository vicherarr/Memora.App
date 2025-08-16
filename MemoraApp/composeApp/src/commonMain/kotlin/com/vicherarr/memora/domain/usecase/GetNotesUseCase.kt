package com.vicherarr.memora.domain.usecase

import com.vicherarr.memora.domain.models.*
import com.vicherarr.memora.domain.repository.NotesRepository
import com.vicherarr.memora.domain.utils.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Use Case for getting and filtering notes.
 * Encapsulates the business logic of combining the notes source with various filters.
 */
class GetNotesUseCase(
    private val notesRepository: NotesRepository
) {

    /**
     * Exposes a flow that emits the full list of notes and the filtered list
     * based on the provided filter flows.
     *
     * @param searchQuery Flow of the user's search query.
     * @param dateFilter Flow of the selected date filter.
     * @param fileTypeFilter Flow of the selected file type filter.
     * @param customDateRange Flow of the selected custom date range.
     * @param categoryFilter Flow of the selected category filter.
     * @param selectedCategoryId Flow of the selected category ID.
     * @return A flow that emits a Pair containing the complete list of notes and the filtered list.
     */
    fun execute(
        searchQuery: Flow<String>,
        dateFilter: Flow<DateFilter>,
        fileTypeFilter: Flow<FileTypeFilter>,
        customDateRange: Flow<DateRange?>,
        categoryFilter: Flow<CategoryFilter>,
        selectedCategoryId: Flow<String?>
    ): Flow<Pair<List<Note>, List<Note>>> {
        return combine(
            notesRepository.getNotesFlow(),
            searchQuery,
            dateFilter,
            fileTypeFilter,
            customDateRange,
            categoryFilter,
            selectedCategoryId
        ) { flows ->
            val notes = flows[0] as List<Note>
            val query = flows[1] as String
            val dateF = flows[2] as DateFilter
            val fileTypeF = flows[3] as FileTypeFilter
            val customRange = flows[4] as DateRange?
            val categoryF = flows[5] as CategoryFilter
            val selectedCatId = flows[6] as String?
            val filteredNotes = notes.filter { note ->
                val matchesSearch = if (query.isNotBlank()) {
                    note.titulo?.contains(query, ignoreCase = true) == true ||
                            note.contenido.contains(query, ignoreCase = true)
                } else {
                    true
                }

                val matchesDate = DateTimeUtils.matchesDateFilter(
                    timestamp = note.fechaModificacion,
                    dateFilter = dateF,
                    customRange = customRange
                )

                val matchesFileType = when (fileTypeF) {
                    FileTypeFilter.WITH_IMAGES -> note.archivosAdjuntos.any { it.tipoArchivo == TipoDeArchivo.Imagen }
                    FileTypeFilter.WITH_VIDEOS -> note.archivosAdjuntos.any { it.tipoArchivo == TipoDeArchivo.Video }
                    FileTypeFilter.WITH_ATTACHMENTS -> note.archivosAdjuntos.isNotEmpty()
                    FileTypeFilter.TEXT_ONLY -> note.archivosAdjuntos.isEmpty()
                    FileTypeFilter.ALL -> true
                }

                val matchesCategory = when (categoryF) {
                    CategoryFilter.ALL -> true
                    CategoryFilter.UNCATEGORIZED -> note.categories.isEmpty()
                    CategoryFilter.SPECIFIC_CATEGORY -> {
                        selectedCatId != null && note.categories.any { it.id == selectedCatId }
                    }
                }

                matchesSearch && matchesDate && matchesFileType && matchesCategory
            }
            // Return a pair of the original list and the filtered list
            Pair(notes, filteredNotes)
        }
    }
}