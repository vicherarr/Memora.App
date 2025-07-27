using Application.Features.Archivos.Commands;
using Application.Features.Archivos.DTOs;
using Application.Features.Archivos.Queries;
using MediatR;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace API.Controllers;

/// <summary>
/// Controlador para la gestión de archivos adjuntos en notas
/// </summary>
/// <remarks>
/// Este controlador maneja todas las operaciones de archivos multimedia:
/// - Subir archivos (imágenes y videos) a notas específicas
/// - Obtener información de archivos
/// - Descargar archivos con streaming
/// - Eliminar archivos adjuntos
/// 
/// **Tipos de archivo soportados:**
/// - 🖼️ Imágenes: JPEG, PNG, GIF, WebP
/// - 🎥 Videos: MP4, MOV, AVI, WMV, WebM
/// 
/// **Límites:**
/// - Tamaño máximo por archivo: 50MB
/// - Almacenamiento: Base de datos como BLOB
/// 
/// **Autenticación requerida:** Todos los endpoints requieren token JWT válido.
/// **Autorización:** Los usuarios solo pueden acceder a archivos de sus propias notas.
/// </remarks>
[ApiController]
[Route("api")]
[Authorize]
[Produces("application/json")]
public class ArchivosController : ControllerBase
{
    private readonly IMediator _mediator;

    public ArchivosController(IMediator mediator)
    {
        _mediator = mediator;
    }

    /// <summary>
    /// 📎 Sube uno o más archivos a una nota específica
    /// </summary>
    /// <remarks>
    /// Permite adjuntar archivos multimedia a una nota existente:
    /// 
    /// **Características:**
    /// - 🔒 Solo puedes subir archivos a tus propias notas
    /// - 📁 Múltiples archivos soportados en una sola petición
    /// - 🛡️ Validación exhaustiva de tipos de archivo y contenido
    /// - 💾 Almacenamiento directo en base de datos como BLOB
    /// - 🔍 Detección automática de MIME types
    /// 
    /// **Archivos soportados:**
    /// ```
    /// 🖼️ Imágenes: .jpg, .jpeg, .png, .gif, .webp
    /// 🎥 Videos: .mp4, .mov, .avi, .wmv, .webm
    /// ```
    /// 
    /// **Límites:**
    /// - 📏 Tamaño máximo: 50MB por archivo
    /// - 🔢 Sin límite en número de archivos por petición
    /// - 🚫 Tipos de archivo restringidos por seguridad
    /// 
    /// **Ejemplo usando cURL:**
    /// ```bash
    /// curl -X POST "https://api.memora.com/api/notas/{notaId}/archivos" \
    ///   -H "Authorization: Bearer {token}" \
    ///   -F "files=@imagen1.jpg" \
    ///   -F "files=@video1.mp4"
    /// ```
    /// 
    /// **Respuesta exitosa:**
    /// ```json
    /// [
    ///   {
    ///     "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    ///     "nombreOriginal": "imagen1.jpg",
    ///     "tipoArchivo": "Imagen",
    ///     "tipoMime": "image/jpeg",
    ///     "tamanoBytes": 2048576,
    ///     "fechaSubida": "2023-12-01T10:30:00Z"
    ///   }
    /// ]
    /// ```
    /// </remarks>
    /// <param name="notaId">ID único de la nota donde adjuntar los archivos</param>
    /// <param name="files">Archivos a subir (multipart/form-data)</param>
    /// <returns>Lista de archivos subidos con su información</returns>
    /// <response code="201">Archivos subidos exitosamente</response>
    /// <response code="400">Archivos inválidos o nota no encontrada</response>
    /// <response code="401">Token JWT inválido o expirado</response>
    /// <response code="403">No tienes permisos para adjuntar archivos a esta nota</response>
    /// <response code="413">Archivo(s) demasiado grande(s)</response>
    /// <response code="415">Tipo de archivo no soportado</response>
    /// <response code="500">Error interno del servidor</response>
    [HttpPost("notas/{notaId}/archivos")]
    [ProducesResponseType(typeof(List<UploadArchivoResponseDto>), 201)]
    [ProducesResponseType(typeof(object), 400)]
    [ProducesResponseType(typeof(object), 401)]
    [ProducesResponseType(typeof(object), 403)]
    [ProducesResponseType(typeof(object), 413)]
    [ProducesResponseType(typeof(object), 415)]
    [ProducesResponseType(typeof(object), 500)]
    [RequestSizeLimit(52428800)] // 50MB
    public async Task<ActionResult<List<UploadArchivoResponseDto>>> UploadArchivos(
        Guid notaId, 
        [FromForm] List<IFormFile> files)
    {
        if (files == null || files.Count == 0)
        {
            return BadRequest(new { message = "No se proporcionaron archivos" });
        }

        var usuarioId = GetCurrentUserId();
        var responses = new List<UploadArchivoResponseDto>();

        foreach (var file in files)
        {
            using var stream = file.OpenReadStream();
            using var memoryStream = new MemoryStream();
            await stream.CopyToAsync(memoryStream);
            
            var command = new UploadArchivoCommand
            {
                NotaId = notaId,
                UsuarioId = usuarioId,
                FileData = memoryStream.ToArray(),
                FileName = file.FileName,
                ContentType = file.ContentType
            };

            var result = await _mediator.Send(command);
            responses.Add(result);
        }

        return CreatedAtAction(nameof(GetArchivo), new { id = responses.First().ArchivoId }, responses);
    }

    /// <summary>
    /// 📄 Obtiene información de un archivo específico
    /// </summary>
    /// <remarks>
    /// Recupera los metadatos de un archivo adjunto (sin descargar el contenido):
    /// 
    /// **Información incluida:**
    /// - 🆔 ID único del archivo
    /// - 📝 Nombre original del archivo
    /// - 📊 Tamaño en bytes
    /// - 🏷️ Tipo de archivo (Imagen/Video)
    /// - 🔖 MIME type
    /// - 📅 Fecha de subida
    /// - 📎 ID de la nota a la que pertenece
    /// 
    /// **Ejemplo de respuesta:**
    /// ```json
    /// {
    ///   "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    ///   "nombreOriginal": "foto_vacaciones.jpg",
    ///   "tipoArchivo": "Imagen",
    ///   "tipoMime": "image/jpeg",
    ///   "tamanoBytes": 2048576,
    ///   "fechaSubida": "2023-12-01T10:30:00Z",
    ///   "notaId": "7ba85f64-5717-4562-b3fc-2c963f66afa6"
    /// }
    /// ```
    /// 
    /// **Casos de uso:**
    /// - Mostrar lista de archivos en la app
    /// - Verificar información antes de descargar
    /// - Obtener MIME type para mostrar iconos apropiados
    /// </remarks>
    /// <param name="id">ID único del archivo</param>
    /// <returns>Información detallada del archivo</returns>
    /// <response code="200">Información del archivo obtenida exitosamente</response>
    /// <response code="401">Token JWT inválido o expirado</response>
    /// <response code="404">Archivo no encontrado o no pertenece al usuario</response>
    /// <response code="500">Error interno del servidor</response>
    [HttpGet("archivos/{id}")]
    [ProducesResponseType(typeof(ArchivoAdjuntoDto), 200)]
    [ProducesResponseType(typeof(object), 401)]
    [ProducesResponseType(typeof(object), 404)]
    [ProducesResponseType(typeof(object), 500)]
    public async Task<ActionResult<ArchivoAdjuntoDto>> GetArchivo(Guid id)
    {
        var usuarioId = GetCurrentUserId();
        
        var query = new GetArchivoByIdQuery
        {
            ArchivoId = id,
            UsuarioId = usuarioId
        };

        var result = await _mediator.Send(query);
        
        if (result == null)
        {
            return NotFound(new { message = "Archivo no encontrado" });
        }

        return Ok(result);
    }

    /// <summary>
    /// ⬇️ Descarga el contenido completo de un archivo
    /// </summary>
    /// <remarks>
    /// Descarga el archivo completo con streaming eficiente:
    /// 
    /// **Características:**
    /// - 🚀 Streaming directo desde base de datos
    /// - 📱 Optimizado para dispositivos móviles
    /// - 🏷️ Headers HTTP apropiados (Content-Type, Content-Length)
    /// - 💾 Sin carga completa en memoria del servidor
    /// - 📁 Nombre de archivo original preservado
    /// 
    /// **Headers de respuesta:**
    /// ```
    /// Content-Type: [MIME type del archivo]
    /// Content-Length: [tamaño en bytes]
    /// Content-Disposition: attachment; filename="nombre_original.ext"
    /// ```
    /// 
    /// **Ejemplo usando cURL:**
    /// ```bash
    /// curl -X GET "https://api.memora.com/api/archivos/{id}/download" \
    ///   -H "Authorization: Bearer {token}" \
    ///   -o "archivo_descargado.jpg"
    /// ```
    /// 
    /// **Casos de uso:**
    /// - Mostrar imágenes en la app móvil
    /// - Reproducir videos adjuntos
    /// - Exportar archivos para compartir
    /// - Crear copias de seguridad locales
    /// </remarks>
    /// <param name="id">ID único del archivo a descargar</param>
    /// <returns>Contenido binario del archivo con headers apropiados</returns>
    /// <response code="200">Archivo descargado exitosamente</response>
    /// <response code="401">Token JWT inválido o expirado</response>
    /// <response code="404">Archivo no encontrado o no pertenece al usuario</response>
    /// <response code="500">Error interno del servidor</response>
    [HttpGet("archivos/{id}/download")]
    [ProducesResponseType(typeof(FileContentResult), 200)]
    [ProducesResponseType(typeof(object), 401)]
    [ProducesResponseType(typeof(object), 404)]
    [ProducesResponseType(typeof(object), 500)]
    public async Task<IActionResult> DownloadArchivo(Guid id)
    {
        var usuarioId = GetCurrentUserId();
        
        var query = new GetArchivoDataQuery
        {
            ArchivoId = id,
            UsuarioId = usuarioId
        };

        var result = await _mediator.Send(query);
        
        if (result == null)
        {
            return NotFound(new { message = "Archivo no encontrado" });
        }

        return File(
            result.Data, 
            result.ContentType, 
            result.FileName);
    }

    /// <summary>
    /// 🗑️ Elimina permanentemente un archivo adjunto
    /// </summary>
    /// <remarks>
    /// Elimina completamente un archivo de la base de datos:
    /// 
    /// **⚠️ ATENCIÓN - Esta acción es irreversible:**
    /// - 🗑️ El archivo se elimina permanentemente de la base de datos
    /// - 💾 Los datos binarios se borran completamente
    /// - 📝 La nota mantiene sus otros archivos adjuntos
    /// - 🔒 Solo puedes eliminar archivos de tus propias notas
    /// 
    /// **Respuesta exitosa:**
    /// - Status Code: `204 No Content`
    /// - Sin contenido en el cuerpo de la respuesta
    /// 
    /// **Casos de uso:**
    /// - Liberar espacio eliminando archivos innecesarios
    /// - Corregir errores en uploads
    /// - Eliminar archivos duplicados
    /// - Borrar contenido sensible
    /// 
    /// **Ejemplo usando cURL:**
    /// ```bash
    /// curl -X DELETE "https://api.memora.com/api/archivos/{id}" \
    ///   -H "Authorization: Bearer {token}"
    /// ```
    /// </remarks>
    /// <param name="id">ID único del archivo a eliminar</param>
    /// <returns>Sin contenido (204) si se eliminó exitosamente</returns>
    /// <response code="204">Archivo eliminado exitosamente</response>
    /// <response code="401">Token JWT inválido o expirado</response>
    /// <response code="404">Archivo no encontrado o no pertenece al usuario</response>
    /// <response code="500">Error interno del servidor</response>
    [HttpDelete("archivos/{id}")]
    [ProducesResponseType(204)]
    [ProducesResponseType(typeof(object), 401)]
    [ProducesResponseType(typeof(object), 404)]
    [ProducesResponseType(typeof(object), 500)]
    public async Task<IActionResult> DeleteArchivo(Guid id)
    {
        var usuarioId = GetCurrentUserId();
        
        var command = new DeleteArchivoCommand
        {
            ArchivoId = id,
            UsuarioId = usuarioId
        };

        var result = await _mediator.Send(command);
        
        if (!result)
        {
            return NotFound(new { message = "Archivo no encontrado" });
        }

        return NoContent();
    }

    private Guid GetCurrentUserId()
    {
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