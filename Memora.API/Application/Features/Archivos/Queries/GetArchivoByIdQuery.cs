using MediatR;
using Application.Features.Archivos.DTOs;

namespace Application.Features.Archivos.Queries;

public record GetArchivoByIdQuery : IRequest<ArchivoAdjuntoDto?>
{
    public Guid ArchivoId { get; init; }
    public Guid UsuarioId { get; init; }
}