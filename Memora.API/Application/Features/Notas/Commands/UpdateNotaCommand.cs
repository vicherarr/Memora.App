using Application.Features.Notas.DTOs;
using MediatR;

namespace Application.Features.Notas.Commands;

public class UpdateNotaCommand : IRequest<NotaDto?>
{
    public Guid NotaId { get; set; }
    public string? Titulo { get; set; }
    public string Contenido { get; set; } = string.Empty;
    public Guid UsuarioId { get; set; }
}