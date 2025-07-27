using Domain.Enums;

namespace Application.Features.Archivos.DTOs;

public record ArchivoAdjuntoDto
{
    public Guid Id { get; init; }
    public string NombreOriginal { get; init; } = string.Empty;
    public TipoDeArchivo TipoArchivo { get; init; }
    public string TipoMime { get; init; } = string.Empty;
    public long TamanoBytes { get; init; }
    public DateTime FechaSubida { get; init; }
    public Guid NotaId { get; init; }
}