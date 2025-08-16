# Plan: Añadir Categorías a Notas - Sistema Dinámico con Sincronización

## 📋 **Resumen del Feature**

Implementar un sistema de categorías dinámicas creadas por usuarios que permita:
- **Categorías N:M**: Una nota puede pertenecer a múltiples categorías
- **Creación dinámica**: Usuarios crean categorías sobre la marcha
- **UI sencilla**: Asignación fácil durante crear/editar nota
- **Limpieza automática**: Eliminación de categorías sin notas
- **Sincronización inteligente**: Integración con sistema de sync existente
- **Mínimo impacto**: Respeto a arquitectura Clean Architecture actual

## 🏗️ **Arquitectura de Base de Datos - SQLDelight**

### **1. Nueva Tabla: Categories**
```sql
-- Categories.sq
CREATE TABLE categories (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    color TEXT NOT NULL DEFAULT '#6750A4', -- Material Design 3 Primary
    icon TEXT DEFAULT NULL, -- Opcional: nombre del icono
    created_at TEXT NOT NULL,
    modified_at TEXT NOT NULL,
    user_id TEXT NOT NULL,
    -- Campos de sincronización
    sync_status TEXT NOT NULL DEFAULT 'PENDING',
    needs_upload INTEGER NOT NULL DEFAULT 1,
    local_created_at INTEGER NOT NULL,
    last_sync_attempt INTEGER,
    remote_id TEXT,
    UNIQUE(name, user_id) -- Una categoría por nombre por usuario
);

-- Índices para performance
CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_categories_sync_status ON categories(sync_status);
CREATE INDEX idx_categories_name ON categories(name);
```

### **2. Nueva Tabla: Note_Categories (Relación N:M)**
```sql
-- NoteCategories.sq
CREATE TABLE note_categories (
    id TEXT NOT NULL PRIMARY KEY,
    note_id TEXT NOT NULL,
    category_id TEXT NOT NULL,
    created_at TEXT NOT NULL,
    -- Campos de sincronización
    sync_status TEXT NOT NULL DEFAULT 'PENDING',
    needs_upload INTEGER NOT NULL DEFAULT 1,
    local_created_at INTEGER NOT NULL,
    FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    UNIQUE(note_id, category_id) -- Evitar duplicados
);

-- Índices para performance
CREATE INDEX idx_note_categories_note_id ON note_categories(note_id);
CREATE INDEX idx_note_categories_category_id ON note_categories(category_id);
CREATE INDEX idx_note_categories_sync_status ON note_categories(sync_status);
```

### **3. Migración SQLDelight**
```sql
-- Migration_Add_Categories.sq
-- Migración para añadir categorías sin afectar datos existentes

-- Versión de esquema: V1 → V2
PRAGMA user_version = 2;

-- Crear tabla categories
CREATE TABLE categories (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    color TEXT NOT NULL DEFAULT '#6750A4',
    icon TEXT DEFAULT NULL,
    created_at TEXT NOT NULL,
    modified_at TEXT NOT NULL,
    user_id TEXT NOT NULL,
    sync_status TEXT NOT NULL DEFAULT 'PENDING',
    needs_upload INTEGER NOT NULL DEFAULT 1,
    local_created_at INTEGER NOT NULL,
    last_sync_attempt INTEGER,
    remote_id TEXT,
    UNIQUE(name, user_id)
);

-- Crear tabla note_categories
CREATE TABLE note_categories (
    id TEXT NOT NULL PRIMARY KEY,
    note_id TEXT NOT NULL,
    category_id TEXT NOT NULL,
    created_at TEXT NOT NULL,
    sync_status TEXT NOT NULL DEFAULT 'PENDING',
    needs_upload INTEGER NOT NULL DEFAULT 1,
    local_created_at INTEGER NOT NULL,
    FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    UNIQUE(note_id, category_id)
);

-- Crear índices
CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_categories_sync_status ON categories(sync_status);
CREATE INDEX idx_categories_name ON categories(name);
CREATE INDEX idx_note_categories_note_id ON note_categories(note_id);
CREATE INDEX idx_note_categories_category_id ON note_categories(category_id);
CREATE INDEX idx_note_categories_sync_status ON note_categories(sync_status);
```

## 🏛️ **Modelos de Dominio**

### **1. Category (Domain Model)**
```kotlin
data class Category(
    val id: String,
    val name: String,
    val color: String, // Hex color
    val icon: String? = null,
    val createdAt: Long,
    val modifiedAt: Long,
    val userId: String,
    val notesCount: Int = 0 // Computed field para UI
)
```

### **2. Note (Modificación del modelo existente)**
```kotlin
data class Note(
    val id: String,
    val titulo: String?,
    val contenido: String,
    val fechaCreacion: Long,
    val fechaModificacion: Long,
    val usuarioId: String,
    val archivosAdjuntos: List<ArchivoAdjunto> = emptyList(),
    val categories: List<Category> = emptyList() // ✅ NUEVO CAMPO
)
```

## 🔄 **Capa de Datos - Repositories y DAOs**

### **1. CategoriesDao**
```kotlin
interface CategoriesDao {
    // CRUD Operations
    suspend fun insertCategory(category: Categories): Long
    suspend fun updateCategory(category: Categories)
    suspend fun deleteCategory(categoryId: String)
    suspend fun getCategoryById(categoryId: String): Categories?
    
    // User-specific queries
    suspend fun getCategoriesByUserId(userId: String): List<Categories>
    fun getCategoriesByUserIdFlow(userId: String): Flow<List<Categories>>
    
    // Category management
    suspend fun getCategoryByNameAndUserId(name: String, userId: String): Categories?
    suspend fun getCategoriesWithNoteCount(userId: String): List<CategoryWithNoteCount>
    suspend fun getUnusedCategories(userId: String): List<Categories>
    
    // Sync operations
    suspend fun getCategoriesPendingSync(userId: String): List<Categories>
    suspend fun markCategoryAsSynced(categoryId: String)
}
```

### **2. NoteCategoriesDao**
```kotlin
interface NoteCategoriesDao {
    // Relationship management
    suspend fun insertNoteCategory(noteCategory: NoteCategories)
    suspend fun deleteNoteCategory(noteCategoryId: String)
    suspend fun deleteNoteCategoriesByNoteId(noteId: String)
    suspend fun deleteNoteCategoriesByCategoryId(categoryId: String)
    
    // Queries
    suspend fun getCategoriesByNoteId(noteId: String): List<Categories>
    suspend fun getNotesByCategoryId(categoryId: String): List<Notes>
    fun getCategoriesByNoteIdFlow(noteId: String): Flow<List<Categories>>
    
    // Sync operations
    suspend fun getNoteCategoriesPendingSync(userId: String): List<NoteCategories>
    suspend fun markNoteCategoryAsSynced(noteCategoryId: String)
}
```

### **3. CategoriesRepository**
```kotlin
interface CategoriesRepository {
    // Category CRUD
    suspend fun createCategory(name: String, color: String, icon: String? = null): Result<Category>
    suspend fun updateCategory(category: Category): Result<Category>
    suspend fun deleteCategory(categoryId: String): Result<Unit>
    suspend fun getCategoriesByUser(): Result<List<Category>>
    fun getCategoriesFlow(): Flow<List<Category>>
    
    // Category management
    suspend fun findOrCreateCategory(name: String, color: String): Result<Category>
    suspend fun cleanupUnusedCategories(): Result<Int> // Returns count of deleted categories
    
    // Note-Category relationships
    suspend fun assignCategoriesToNote(noteId: String, categoryIds: List<String>): Result<Unit>
    suspend fun getCategoriesForNote(noteId: String): Result<List<Category>>
    suspend fun removeNoteFromCategory(noteId: String, categoryId: String): Result<Unit>
}
```

## 🎯 **Capa de Dominio - Use Cases**

### **1. CreateCategoryUseCase**
```kotlin
class CreateCategoryUseCase(
    private val categoriesRepository: CategoriesRepository,
    private val validationService: ValidationService
) {
    suspend fun execute(name: String, color: String, icon: String? = null): Result<Category> {
        // Validación de negocio
        val validationResult = validationService.validateCategoryName(name)
        if (!validationResult.isValid) {
            return Result.failure(IllegalArgumentException(validationResult.errorMessage))
        }
        
        // Crear categoría
        return categoriesRepository.createCategory(name.trim(), color, icon)
    }
}
```

### **2. ManageNoteCategoriesUseCase**
```kotlin
class ManageNoteCategoriesUseCase(
    private val categoriesRepository: CategoriesRepository,
    private val validationService: ValidationService
) {
    suspend fun assignCategoriesToNote(
        noteId: String, 
        categoryNames: List<String>,
        defaultColor: String = "#6750A4"
    ): Result<List<Category>> {
        // Validar entrada
        if (noteId.isBlank()) {
            return Result.failure(IllegalArgumentException("Note ID cannot be blank"))
        }
        
        val categories = mutableListOf<Category>()
        
        // Crear o encontrar categorías
        for (name in categoryNames.map { it.trim() }.filter { it.isNotBlank() }) {
            val categoryResult = categoriesRepository.findOrCreateCategory(name, defaultColor)
            if (categoryResult.isFailure) {
                return Result.failure(categoryResult.exceptionOrNull()!!)
            }
            categories.add(categoryResult.getOrThrow())
        }
        
        // Asignar categorías a nota
        val categoryIds = categories.map { it.id }
        return categoriesRepository.assignCategoriesToNote(noteId, categoryIds)
            .map { categories }
    }
    
    suspend fun cleanupUnusedCategories(): Result<Int> {
        return categoriesRepository.cleanupUnusedCategories()
    }
}
```

### **3. Modificar Use Cases Existentes**

#### **UpdateNoteUseCase (Extensión)**
```kotlin
class UpdateNoteUseCase(
    private val notesRepository: NotesRepository,
    private val validationService: ValidationService,
    private val manageNoteCategoriesUseCase: ManageNoteCategoriesUseCase // ✅ NUEVO
) {
    suspend fun executeWithCategories(
        noteId: String,
        titulo: String?,
        contenido: String,
        existingAttachments: List<ArchivoAdjunto>,
        newMediaFiles: List<MediaFile>,
        categoryNames: List<String> // ✅ NUEVO PARÁMETRO
    ): Result<Note> {
        // Validación existente...
        
        // Actualizar nota
        val noteResult = notesRepository.updateNoteWithAttachments(
            noteId, titulo, contenido, existingAttachments, newMediaFiles
        )
        
        if (noteResult.isFailure) {
            return noteResult
        }
        
        // Asignar categorías
        manageNoteCategoriesUseCase.assignCategoriesToNote(noteId, categoryNames)
        
        // Retornar nota actualizada con categorías
        return notesRepository.getNoteById(noteId)
    }
}
```

## 🎨 **Capa de Presentación**

### **1. CategoryUiState**
```kotlin
data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val isCreatingCategory: Boolean = false,
    val newCategoryName: String = "",
    val newCategoryColor: String = "#6750A4",
    override val isLoading: Boolean = false,
    override val errorMessage: String? = null
) : BaseUiState
```

### **2. CategorySelectionViewModel**
```kotlin
class CategorySelectionViewModel(
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val categoriesRepository: CategoriesRepository,
    private val manageNoteCategoriesUseCase: ManageNoteCategoriesUseCase
) : BaseViewModel<CategoryUiState>() {
    
    fun loadCategoriesForNote(noteId: String?) {
        viewModelScope.launch {
            setLoading(true)
            
            // Cargar todas las categorías del usuario
            val allCategoriesResult = categoriesRepository.getCategoriesByUser()
            
            // Cargar categorías asignadas a la nota (si existe)
            val selectedCategoriesResult = if (noteId != null) {
                categoriesRepository.getCategoriesForNote(noteId)
            } else {
                Result.success(emptyList())
            }
            
            if (allCategoriesResult.isSuccess && selectedCategoriesResult.isSuccess) {
                updateState {
                    copy(
                        categories = allCategoriesResult.getOrThrow(),
                        selectedCategories = selectedCategoriesResult.getOrThrow(),
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } else {
                setError("Error loading categories")
            }
        }
    }
    
    fun createAndSelectCategory(name: String, color: String) {
        viewModelScope.launch {
            createCategoryUseCase.execute(name, color)
                .onSuccess { category ->
                    updateState {
                        copy(
                            categories = categories + category,
                            selectedCategories = selectedCategories + category,
                            isCreatingCategory = false,
                            newCategoryName = ""
                        )
                    }
                }
                .onFailure { setError(it.message ?: "Error creating category") }
        }
    }
    
    fun toggleCategorySelection(category: Category) {
        updateState {
            val isSelected = selectedCategories.contains(category)
            val newSelected = if (isSelected) {
                selectedCategories.filter { it.id != category.id }
            } else {
                selectedCategories + category
            }
            copy(selectedCategories = newSelected)
        }
    }
}
```

### **3. Modificar CreateNoteViewModel**
```kotlin
class CreateNoteViewModel(
    private val createNoteUseCase: CreateNoteUseCase,
    private val manageNoteCategoriesUseCase: ManageNoteCategoriesUseCase, // ✅ NUEVO
    private val mediaViewModel: MediaViewModel
) : BaseViewModel<CreateNoteUiState>() {
    
    // Añadir categorías al estado
    data class CreateNoteUiState(
        val titulo: String = "",
        val contenido: String = "",
        val selectedCategoryNames: List<String> = emptyList(), // ✅ NUEVO
        val isNoteSaved: Boolean = false,
        override val isLoading: Boolean = false,
        override val errorMessage: String? = null
    ) : BaseUiState
    
    fun updateSelectedCategories(categoryNames: List<String>) {
        updateState { copy(selectedCategoryNames = categoryNames) }
    }
    
    fun createNote() {
        val currentState = _uiState.value
        val selectedMedia = mediaViewModel.uiState.value.selectedMedia
        
        viewModelScope.launch {
            setLoading(true)
            
            // Crear nota
            val result = if (selectedMedia.isNotEmpty()) {
                createNoteUseCase.executeWithAttachments(
                    titulo = if (currentState.titulo.isBlank()) null else currentState.titulo.trim(),
                    contenido = currentState.contenido.trim(),
                    attachments = selectedMedia
                )
            } else {
                createNoteUseCase.execute(
                    titulo = if (currentState.titulo.isBlank()) null else currentState.titulo.trim(),
                    contenido = currentState.contenido.trim()
                )
            }
            
            result.onSuccess { note ->
                // Asignar categorías si las hay
                if (currentState.selectedCategoryNames.isNotEmpty()) {
                    manageNoteCategoriesUseCase.assignCategoriesToNote(
                        note.id, 
                        currentState.selectedCategoryNames
                    )
                }
                
                mediaViewModel.clearSelectedMedia()
                updateState { copy(isNoteSaved = true, isLoading = false, errorMessage = null) }
            }.onFailure { exception ->
                setError(exception.message ?: "Error al crear la nota")
            }
        }
    }
}
```

## 🎨 **Componentes UI**

### **1. CategorySelectionComponent**
```kotlin
@Composable
fun CategorySelectionComponent(
    selectedCategories: List<Category>,
    availableCategories: List<Category>,
    onCategoryToggle: (Category) -> Unit,
    onCreateCategory: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Categorías",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Categorías seleccionadas (chips)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(selectedCategories) { category ->
                CategoryChip(
                    category = category,
                    isSelected = true,
                    onClick = { onCategoryToggle(category) }
                )
            }
            
            // Botón para añadir nueva categoría
            item {
                AddCategoryChip(
                    onCreateCategory = onCreateCategory
                )
            }
        }
        
        // Lista de categorías disponibles
        if (availableCategories.isNotEmpty()) {
            Text(
                text = "Categorías disponibles",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableCategories.filter { it !in selectedCategories }) { category ->
                    CategoryChip(
                        category = category,
                        isSelected = false,
                        onClick = { onCategoryToggle(category) }
                    )
                }
            }
        }
    }
}
```

### **2. CategoryChip**
```kotlin
@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        onClick = onClick,
        label = { Text(category.name) },
        selected = isSelected,
        leadingIcon = if (isSelected) {
            { Icon(Icons.Default.Check, contentDescription = null) }
        } else {
            category.icon?.let { iconName ->
                { Icon(getIconByName(iconName), contentDescription = null) }
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(android.graphics.Color.parseColor(category.color)),
            selectedLabelColor = Color.White
        ),
        modifier = modifier
    )
}
```

## 🔄 **Integración con Sincronización Inteligente**

### **1. Extender SyncMetadata**
```kotlin
data class SyncMetadata(
    val userId: String,
    val lastSyncTimestamp: Long,
    val localFingerprint: String,
    val remoteFingerprint: String,
    val notesCount: Int,
    val attachmentsCount: Int,
    val categoriesCount: Int, // ✅ NUEVO
    val noteCategoriesCount: Int, // ✅ NUEVO
    val notesHash: String,
    val attachmentsHash: String,
    val categoriesHash: String, // ✅ NUEVO
    val noteCategoriesHash: String // ✅ NUEVO
)
```

### **2. Extender DatabaseSyncService**
```kotlin
class DatabaseSyncService {
    suspend fun syncCategories(userId: String): SyncResult {
        // Sync categorías pendientes
        val pendingCategories = categoriesDao.getCategoriesPendingSync(userId)
        // Upload a API/Cloud
        // Download cambios remotos
        // Resolver conflictos
        // Actualizar sync_status
    }
    
    suspend fun syncNoteCategories(userId: String): SyncResult {
        // Sync relaciones nota-categoría pendientes
        val pendingRelations = noteCategoriesDao.getNoteCategoriesPendingSync(userId)
        // Upload a API/Cloud
        // Download cambios remotos
        // Resolver conflictos
    }
    
    private suspend fun generateSyncMetadata(userId: String): SyncMetadata {
        // Incluir conteos y hashes de categorías y relaciones
        val categoriesCount = categoriesDao.getCategoriesByUserId(userId).size
        val noteCategoriesCount = noteCategoriesDao.getNoteCategoriesPendingSync(userId).size
        val categoriesHash = calculateCategoriesHash(userId)
        val noteCategoriesHash = calculateNoteCategoriesHash(userId)
        
        // ... resto del código existente
    }
}
```

## 📱 **Modificaciones en Pantallas**

### **1. CreateNoteScreen (Extensión)**
```kotlin
@Composable
fun CreateNoteScreen(
    // ... parámetros existentes
) {
    // ... código existente
    
    // Añadir sección de categorías
    CategorySelectionComponent(
        selectedCategories = categoryViewModel.uiState.collectAsState().value.selectedCategories,
        availableCategories = categoryViewModel.uiState.collectAsState().value.categories,
        onCategoryToggle = { categoryViewModel.toggleCategorySelection(it) },
        onCreateCategory = { name, color -> categoryViewModel.createAndSelectCategory(name, color) },
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}
```

### **2. NoteDetailScreen (Extensión)**
```kotlin
@Composable
fun NoteDetailScreen(
    // ... parámetros existentes
) {
    // Mostrar categorías de la nota
    if (note.categories.isNotEmpty()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(note.categories) { category ->
                CategoryChip(
                    category = category,
                    isSelected = true,
                    onClick = { /* Navigate to category filter */ }
                )
            }
        }
    }
}
```

### **3. NotesListScreen (Filtrado por Categoría)**
```kotlin
// Añadir filtro por categoría en FiltersSection
@Composable
fun FiltersSection(
    // ... parámetros existentes
    selectedCategory: Category? = null,
    onCategorySelected: (Category?) -> Unit,
    availableCategories: List<Category> = emptyList()
) {
    // ... filtros existentes
    
    // Filtro por categoría
    ExposedDropdownMenuBox(
        expanded = categoryDropdownExpanded,
        onExpandedChange = { categoryDropdownExpanded = it }
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "Todas las categorías",
            onValueChange = { },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
            modifier = Modifier.menuAnchor()
        )
        
        ExposedDropdownMenu(
            expanded = categoryDropdownExpanded,
            onDismissRequest = { categoryDropdownExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todas las categorías") },
                onClick = {
                    onCategorySelected(null)
                    categoryDropdownExpanded = false
                }
            )
            
            availableCategories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category)
                        categoryDropdownExpanded = false
                    }
                )
            }
        }
    }
}
```

## 🗃️ **API Backend (Extensiones)**

### **1. Endpoints Nuevos**
```csharp
[ApiController]
[Route("api/[controller]")]
public class CategoriasController : ControllerBase
{
    [HttpGet]
    public async Task<ActionResult<List<CategoriaDto>>> GetUserCategories()
    
    [HttpPost]
    public async Task<ActionResult<CategoriaDto>> CreateCategory(CreateCategoriaDto dto)
    
    [HttpPut("{id}")]
    public async Task<ActionResult<CategoriaDto>> UpdateCategory(string id, UpdateCategoriaDto dto)
    
    [HttpDelete("{id}")]
    public async Task<ActionResult> DeleteCategory(string id)
    
    [HttpPost("{categoryId}/notes/{noteId}")]
    public async Task<ActionResult> AssignNoteToCategory(string categoryId, string noteId)
    
    [HttpDelete("{categoryId}/notes/{noteId}")]
    public async Task<ActionResult> RemoveNoteFromCategory(string categoryId, string noteId)
}
```

### **2. DTOs Nuevos**
```csharp
public class CategoriaDto
{
    public string Id { get; set; }
    public string Name { get; set; }
    public string Color { get; set; }
    public string? Icon { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime ModifiedAt { get; set; }
    public int NotesCount { get; set; }
}

public class CreateCategoriaDto
{
    public string Name { get; set; }
    public string Color { get; set; } = "#6750A4";
    public string? Icon { get; set; }
}
```

## 🧪 **Estrategia de Testing**

### **1. Unit Tests**
```kotlin
class ManageNoteCategoriesUseCaseTest {
    @Test
    fun `assignCategoriesToNote creates new categories and assigns them`() = runTest {
        // Given
        val noteId = "note123"
        val categoryNames = listOf("Work", "Important")
        
        // When
        val result = useCase.assignCategoriesToNote(noteId, categoryNames)
        
        // Then
        assertTrue(result.isSuccess)
        val categories = result.getOrThrow()
        assertEquals(2, categories.size)
        assertTrue(categories.any { it.name == "Work" })
        assertTrue(categories.any { it.name == "Important" })
    }
}
```

### **2. Integration Tests**
```kotlin
class CategoriesSyncIntegrationTest {
    @Test
    fun `categories sync maintains data integrity`() = runTest {
        // Test sincronización de categorías sin perder datos
    }
}
```

## 📋 **Plan de Implementación por Fases**

### **Fase 1: Base de Datos y Migración** ⭐
1. Crear tablas SQLDelight para Categories y NoteCategories
2. Implementar sistema de migración
3. Crear DAOs básicos
4. Tests de migración

### **Fase 2: Capa de Dominio**
1. Modelos de dominio (Category)
2. Extender modelo Note
3. Use Cases básicos (Create, Manage)
4. Tests unitarios

### **Fase 3: Repositories y Mappers**
1. CategoriesRepository
2. Mappers para Category
3. Integración con NotesRepository existente
4. Tests de integración

### **Fase 4: ViewModels y UI**
1. CategorySelectionViewModel
2. Extender CreateNoteViewModel y NoteDetailViewModel
3. Componentes UI básicos
4. Tests de ViewModel

### **Fase 5: Integración UI**
1. Integrar en CreateNoteScreen
2. Integrar en NoteDetailScreen
3. Añadir filtros en NotesListScreen
4. Tests UI

### **Fase 6: Sincronización**
1. Extender SyncMetadata
2. Modificar DatabaseSyncService
3. API endpoints (si aplica)
4. Tests de sincronización

### **Fase 7: Limpieza y Optimización**
1. Cleanup automático de categorías
2. Optimizaciones de performance
3. Polish UI/UX
4. Tests E2E

## 🔄 **Compatibilidad y Migración**

### **Datos Existentes**
- ✅ **Cero impacto**: Notas existentes no se ven afectadas
- ✅ **Migración gradual**: Categorías se añaden opcionalmente
- ✅ **Rollback seguro**: Sistema funciona sin categorías

### **Sincronización**
- ✅ **Backward compatible**: API funciona con y sin categorías
- ✅ **Smart sync**: Solo sincroniza cuando hay cambios en categorías
- ✅ **Conflict resolution**: Manejo de conflictos por nombre de categoría

### **Performance**
- ✅ **Índices optimizados**: Consultas eficientes
- ✅ **Lazy loading**: Categorías se cargan solo cuando es necesario
- ✅ **Caching**: Categorías del usuario en memoria

## 🎯 **Criterios de Éxito**

1. **Funcionalidad**
   - [x] Usuarios pueden crear categorías dinámicamente
   - [x] Notas pueden tener múltiples categorías
   - [x] Categorías sin notas se eliminan automáticamente
   - [x] Filtrado por categoría funciona correctamente

2. **Arquitectura**
   - [x] Mínimo impacto en código existente
   - [x] Clean Architecture mantenida
   - [x] SOLID principles respetados
   - [x] KMP best practices aplicadas

3. **Sincronización**
   - [x] Categorías se sincronizan correctamente
   - [x] Sync inteligente detecta cambios
   - [x] Conflictos se resuelven apropiadamente
   - [x] Performance no degradada

4. **UX**
   - [x] UI sencilla e intuitiva
   - [x] Creación de categorías fluida
   - [x] Asignación rápida en crear/editar nota
   - [x] Filtrado eficiente en lista de notas

Este plan mantiene la arquitectura existente mientras añade funcionalidad robusta de categorías con sincronización inteligente y UX optimizada.