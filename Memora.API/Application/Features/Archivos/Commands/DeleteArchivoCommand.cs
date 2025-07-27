using MediatR;

namespace Application.Features.Archivos.Commands;

public record DeleteArchivoCommand : IRequest<bool>
{
    public Guid ArchivoId { get; init; }
    public Guid UsuarioId { get; init; }
}