using Application.Features.Notas.Commands;
using Application.Features.Notas.DTOs;
using Application.Features.Notas.Queries;
using MediatR;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using System.Text.Json;
using System.Text;

namespace API.Controllers;

/// <summary>
/// Controlador para la gestión de notas personales
/// </summary>
/// <remarks>
/// Este controlador maneja todas las operaciones CRUD de notas:
/// - Listar notas con paginación y búsqueda
/// - Obtener detalles de una nota específica
/// - Crear nuevas notas
/// - Actualizar notas existentes
/// - Eliminar notas
/// 
/// **Autenticación requerida:** Todos los endpoints requieren token JWT válido.
/// **Autorización:** Los usuarios solo pueden acceder a sus propias notas.
/// </remarks>
[ApiController]
[Route("api/[controller]")]
[Authorize]
[Produces("application/json")]
public class NotasController : ControllerBase
{
    private readonly IMediator _mediator;

    public NotasController(IMediator mediator)
    {
        _mediator = mediator;
    }

    /// <summary>
    /// 📝 Obtiene las notas del usuario con paginación y búsqueda
    /// </summary>
    /// <remarks>
    /// Recupera todas las notas del usuario autenticado con soporte para:
    /// 
    /// **Características:**
    /// - ✅ Paginación configurable (1-100 notas por página)
    /// - 🔍 Búsqueda opcional por título o contenido
    /// - 📊 Metadatos de paginación incluidos
    /// - 🔒 Solo notas del usuario autenticado
    /// 
    /// **Ejemplo de respuesta:**
    /// ```json
    /// {
    ///   "notas": [
    ///     {
    ///       "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    ///       "titulo": "Mi primera nota",
    ///       "contenido": "Contenido de la nota...",
    ///       "fechaCreacion": "2023-12-01T10:30:00Z",
    ///       "fechaModificacion": "2023-12-01T10:30:00Z"
    ///     }
    ///   ],
    ///   "totalCount": 25,
    ///   "pageNumber": 1,
    ///   "pageSize": 10,
    ///   "totalPages": 3,
    ///   "hasPreviousPage": false,
    ///   "hasNextPage": true
    /// }
    /// ```
    /// 
    /// **Búsqueda:**
    /// El parámetro `searchTerm` busca coincidencias en título y contenido (no distingue mayúsculas).
    /// </remarks>
    /// <param name="pageNumber">Número de página (1-N, por defecto 1)</param>
    /// <param name="pageSize">Elementos por página (1-100, por defecto 10)</param>
    /// <param name="searchTerm">Término de búsqueda opcional para filtrar por título o contenido</param>
    /// <returns>Lista paginada de notas con metadatos de paginación</returns>
    /// <response code="200">Lista de notas obtenida exitosamente</response>
    /// <response code="401">Token JWT inválido o expirado</response>
    /// <response code="500">Error interno del servidor</response>
    [HttpGet]
    [ProducesResponseType(typeof(PaginatedNotasDto), 200)]
    [ProducesResponseType(typeof(object), 401)]
    [ProducesResponseType(typeof(object), 500)]
    public async Task<ActionResult<PaginatedNotasDto>> GetUserNotas(
        [FromQuery] int pageNumber = 1,
        [FromQuery] int pageSize = 10,
        [FromQuery] string? searchTerm = null)
    {
        try
        {
            var usuarioId = GetCurrentUserId();
            
            var query = new GetUserNotasQuery
            {
                UsuarioId = usuarioId,
                PageNumber = pageNumber,
                PageSize = pageSize,
                SearchTerm = searchTerm
            };

            var result = await _mediator.Send(query);
            return Ok(result);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error interno del servidor", error = ex.Message, stackTrace = ex.StackTrace });
        }
    }

    /// <summary>
    /// 📄 Obtiene los detalles de una nota específica
    /// </summary>
    /// <remarks>
    /// Recupera una nota específica del usuario autenticado incluyendo:
    /// 
    /// **Información incluida:**
    /// - 📝 Título y contenido completo
    /// - 📅 Fechas de creación y modificación
    /// - 📎 Número de archivos adjuntos
    /// - 🆔 ID único de la nota
    /// 
    /// **Ejemplo de respuesta:**
    /// ```json
    /// {
    ///   "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    ///   "titulo": "Notas de la reunión",
    ///   "contenido": "Puntos importantes discutidos...",
    ///   "fechaCreacion": "2023-12-01T10:30:00Z",
    ///   "fechaModificacion": "2023-12-01T15:45:00Z",
    ///   "numeroArchivosAdjuntos": 3
    /// }
    /// ```
    /// 
    /// **Seguridad:**
    /// Solo puedes acceder a tus propias notas. Intentar acceder a notas de otros usuarios resultará en 404.
    /// </remarks>
    /// <param name="id">ID único de la nota (formato GUID)</param>
    /// <returns>Detalles completos de la nota con número de archivos adjuntos</returns>
    /// <response code="200">Nota encontrada y devuelta exitosamente</response>
    /// <response code="401">Token JWT inválido o expirado</response>
    /// <response code="404">Nota no encontrada o no pertenece al usuario</response>
    /// <response code="500">Error interno del servidor</response>
    [HttpGet("{id}")]
    [ProducesResponseType(typeof(NotaDetailDto), 200)]
    [ProducesResponseType(typeof(object), 401)]
    [ProducesResponseType(typeof(object), 404)]
    [ProducesResponseType(typeof(object), 500)]
    public async Task<ActionResult<NotaDetailDto>> GetNotaById(Guid id)
    {
        try
        {
            var usuarioId = GetCurrentUserId();
            
            var query = new GetNotaByIdQuery
            {
                NotaId = id,
                UsuarioId = usuarioId
            };

            var result = await _mediator.Send(query);
            
            if (result == null)
            {
                return NotFound(new { message = "Nota no encontrada" });
            }

            return Ok(result);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error interno del servidor", error = ex.Message, stackTrace = ex.StackTrace });
        }
    }

    /// <summary>
    /// ➕ Crea una nueva nota
    /// </summary>
    /// <remarks>
    /// Crea una nueva nota personal para el usuario autenticado:
    /// 
    /// **Características:**
    /// - 📝 Título opcional (máximo 200 caracteres)
    /// - 📄 Contenido obligatorio
    /// - 👤 Automáticamente asociada al usuario autenticado
    /// - 📅 Fecha de creación y modificación automáticas
    /// 
    /// **Ejemplo de request:**
    /// ```json
    /// {
    ///   "titulo": "Lista de compras",
    ///   "contenido": "- Leche\n- Pan\n- Huevos\n- Queso"
    /// }
    /// ```
    /// 
    /// **Ejemplo de respuesta:**
    /// ```json
    /// {
    ///   "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    ///   "titulo": "Lista de compras",
    ///   "contenido": "- Leche\n- Pan\n- Huevos\n- Queso",
    ///   "fechaCreacion": "2023-12-01T10:30:00Z",
    ///   "fechaModificacion": "2023-12-01T10:30:00Z"
    /// }
    /// ```
    /// 
    /// **Nota:** Después de crear la nota, puedes añadir archivos usando el endpoint de archivos.
    /// </remarks>
    /// <param name="request">Datos de la nueva nota (título opcional, contenido obligatorio)</param>
    /// <returns>La nota creada con su ID único</returns>
    /// <response code="201">Nota creada exitosamente</response>
    /// <response code="400">Datos de entrada inválidos</response>
    /// <response code="401">Token JWT inválido o expirado</response>
    /// <response code="500">Error interno del servidor</response>
    [HttpPost]
    [ProducesResponseType(typeof(NotaDto), 201)]
    [ProducesResponseType(typeof(object), 400)]
    [ProducesResponseType(typeof(object), 401)]
    [ProducesResponseType(typeof(object), 500)]
    public async Task<ActionResult<NotaDto>> CreateNota(CreateNotaDto request)
    {
        try
        {
            var usuarioId = GetCurrentUserId();
            
            var command = new CreateNotaCommand
            {
                Titulo = request.Titulo,
                Contenido = request.Contenido,
                UsuarioId = usuarioId
            };

            var result = await _mediator.Send(command);
            return CreatedAtAction(nameof(GetNotaById), new { id = result.Id }, result);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error interno del servidor", error = ex.Message, stackTrace = ex.StackTrace });
        }
    }

    /// <summary>
    /// ✏️ Actualiza una nota existente
    /// </summary>
    /// <remarks>
    /// Modifica el título y/o contenido de una nota existente:
    /// 
    /// **Características:**
    /// - 📝 Actualiza título y/o contenido
    /// - 📅 Actualiza automáticamente la fecha de modificación
    /// - 🔒 Solo puedes actualizar tus propias notas
    /// - 📎 Los archivos adjuntos se mantienen intactos
    /// 
    /// **Ejemplo de request:**
    /// ```json
    /// {
    ///   "titulo": "Lista de compras actualizada",
    ///   "contenido": "- Leche desnatada\n- Pan integral\n- Huevos orgánicos\n- Queso fresco\n- Frutas"
    /// }
    /// ```
    /// 
    /// **Ejemplo de respuesta:**
    /// ```json
    /// {
    ///   "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    ///   "titulo": "Lista de compras actualizada",
    ///   "contenido": "- Leche desnatada\n- Pan integral\n- Huevos orgánicos\n- Queso fresco\n- Frutas",
    ///   "fechaCreacion": "2023-12-01T10:30:00Z",
    ///   "fechaModificacion": "2023-12-01T16:20:00Z"
    /// }
    /// ```
    /// </remarks>
    /// <param name="id">ID único de la nota a actualizar</param>
    /// <param name="request">Nuevos datos de la nota</param>
    /// <returns>La nota actualizada con la nueva fecha de modificación</returns>
    /// <response code="200">Nota actualizada exitosamente</response>
    /// <response code="400">Datos de entrada inválidos</response>
    /// <response code="401">Token JWT inválido o expirado</response>
    /// <response code="404">Nota no encontrada o no pertenece al usuario</response>
    /// <response code="500">Error interno del servidor</response>
    [HttpPut("{id}")]
    [ProducesResponseType(typeof(NotaDto), 200)]
    [ProducesResponseType(typeof(object), 400)]
    [ProducesResponseType(typeof(object), 401)]
    [ProducesResponseType(typeof(object), 404)]
    [ProducesResponseType(typeof(object), 500)]
    public async Task<ActionResult<NotaDto>> UpdateNota(Guid id, UpdateNotaDto request)
    {
        try
        {
            var usuarioId = GetCurrentUserId();
            
            var command = new UpdateNotaCommand
            {
                NotaId = id,
                Titulo = request.Titulo,
                Contenido = request.Contenido,
                UsuarioId = usuarioId
            };

            var result = await _mediator.Send(command);
            
            if (result == null)
            {
                return NotFound(new { message = "Nota no encontrada" });
            }

            return Ok(result);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error interno del servidor", error = ex.Message, stackTrace = ex.StackTrace });
        }
    }

    /// <summary>
    /// 🗑️ Elimina una nota permanentemente
    /// </summary>
    /// <remarks>
    /// Elimina completamente una nota y todos sus archivos adjuntos:
    /// 
    /// **⚠️ ATENCIÓN - Esta acción es irreversible:**
    /// - 🗑️ La nota se elimina permanentemente
    /// - 📎 Todos los archivos adjuntos se eliminan también
    /// - 💾 No hay papelera de reciclaje
    /// - 🔒 Solo puedes eliminar tus propias notas
    /// 
    /// **Respuesta exitosa:**
    /// - Status Code: `204 No Content`
    /// - Sin contenido en el cuerpo de la respuesta
    /// 
    /// **Casos de uso:**
    /// - Limpiar notas obsoletas
    /// - Eliminar notas duplicadas
    /// - Borrar información sensible
    /// 
    /// **Tip:** Si solo quieres vaciar el contenido, considera usar PUT para actualizar en lugar de DELETE.
    /// </remarks>
    /// <param name="id">ID único de la nota a eliminar</param>
    /// <returns>Sin contenido (204) si se eliminó exitosamente</returns>
    /// <response code="204">Nota eliminada exitosamente</response>
    /// <response code="401">Token JWT inválido o expirado</response>
    /// <response code="404">Nota no encontrada o no pertenece al usuario</response>
    /// <response code="500">Error interno del servidor</response>
    [HttpDelete("{id}")]
    [ProducesResponseType(204)]
    [ProducesResponseType(typeof(object), 401)]
    [ProducesResponseType(typeof(object), 404)]
    [ProducesResponseType(typeof(object), 500)]
    public async Task<ActionResult> DeleteNota(Guid id)
    {
        try
        {
            var usuarioId = GetCurrentUserId();
            
            var command = new DeleteNotaCommand
            {
                NotaId = id,
                UsuarioId = usuarioId
            };

            var result = await _mediator.Send(command);
            
            if (!result)
            {
                return NotFound(new { message = "Nota no encontrada" });
            }

            return NoContent();
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Error interno del servidor", error = ex.Message, stackTrace = ex.StackTrace });
        }
    }

    private Guid GetCurrentUserId()
    {
        // Buscar el user ID en los claims del JWT
        var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        
        if (string.IsNullOrEmpty(userIdClaim))
        {
            throw new UnauthorizedAccessException("Usuario no autenticado - no se encontró claim de identificación");
        }
        
        if (!Guid.TryParse(userIdClaim, out var userId))
        {
            throw new UnauthorizedAccessException($"ID de usuario inválido: {userIdClaim}");
        }
        
        return userId;
    }
}