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
     * @return A flow that emits a Pair containing the complete list of notes and the filtered list.
     */
    fun execute(
        searchQuery: Flow<String>,
        dateFilter: Flow<DateFilter>,
        fileTypeFilter: Flow<FileTypeFilter>,
        customDateRange: Flow<DateRange?>
    ): Flow<Pair<List<Note>, List<Note>>> {
        return combine(
            notesRepository.getNotesFlow(),
            searchQuery,
            dateFilter,
            fileTypeFilter,
            customDateRange
        ) { notes, query, dateF, fileTypeF, customRange ->
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

                matchesSearch && matchesDate && matchesFileType
            }
            // Return a pair of the original list and the filtered list
            Pair(notes, filteredNotes)
        }
    }
}