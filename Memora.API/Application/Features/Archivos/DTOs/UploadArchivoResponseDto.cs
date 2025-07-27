namespace Application.Features.Archivos.DTOs;

public record UploadArchivoResponseDto
{
    public Guid ArchivoId { get; init; }
    public string NombreOriginal { get; init; } = string.Empty;
    public long TamanoBytes { get; init; }
    public string TipoMime { get; init; } = string.Empty;
    public string Mensaje { get; init; } = string.Empty;
}