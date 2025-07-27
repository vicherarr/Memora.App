using MediatR;
using Application.Features.Archivos.DTOs;

namespace Application.Features.Archivos.Commands;

public record UploadArchivoCommand : IRequest<UploadArchivoResponseDto>
{
    public Guid NotaId { get; init; }
    public Guid UsuarioId { get; init; }
    public byte[] FileData { get; init; } = Array.Empty<byte>();
    public string FileName { get; init; } = string.Empty;
    public string ContentType { get; init; } = string.Empty;
}