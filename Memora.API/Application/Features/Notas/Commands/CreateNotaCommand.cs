using Application.Features.Notas.DTOs;
using MediatR;

namespace Application.Features.Notas.Commands;

public class CreateNotaCommand : IRequest<NotaDto>
{
    public string? Titulo { get; set; }
    public string Contenido { get; set; } = string.Empty;
    public Guid UsuarioId { get; set; }
}