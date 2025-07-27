using MediatR;

namespace Application.Features.Archivos.Queries;

public record GetArchivoDataQuery : IRequest<ArchivoDataResult?>
{
    public Guid ArchivoId { get; init; }
    public Guid UsuarioId { get; init; }
}

public record ArchivoDataResult
{
    public byte[] Data { get; init; } = Array.Empty<byte>();
    public string ContentType { get; init; } = string.Empty;
    public string FileName { get; init; } = string.Empty;
}