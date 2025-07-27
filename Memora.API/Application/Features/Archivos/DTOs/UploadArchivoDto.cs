using Microsoft.AspNetCore.Http;

namespace Application.Features.Archivos.DTOs;

public record UploadArchivoDto
{
    public Guid NotaId { get; init; }
    public IFormFile Archivo { get; init; } = null!;
}