using MediatR;

namespace Application.Features.Notas.Commands;

public class DeleteNotaCommand : IRequest<bool>
{
    public Guid NotaId { get; set; }
    public Guid UsuarioId { get; set; }
}