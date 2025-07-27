using Application.Features.Notas.DTOs;
using MediatR;

namespace Application.Features.Notas.Queries;

public class GetNotaByIdQuery : IRequest<NotaDetailDto?>
{
    public Guid NotaId { get; set; }
    public Guid UsuarioId { get; set; }
}