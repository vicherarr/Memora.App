using Infrastructure.Data;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace Application.Features.Notas.Commands;

public class DeleteNotaCommandHandler : IRequestHandler<DeleteNotaCommand, bool>
{
    private readonly MemoraDbContext _context;

    public DeleteNotaCommandHandler(MemoraDbContext context)
    {
        _context = context;
    }

    public async Task<bool> Handle(DeleteNotaCommand request, CancellationToken cancellationToken)
    {
        var nota = await _context.Notas
            .Include(n => n.ArchivosAdjuntos)
            .FirstOrDefaultAsync(n => n.Id == request.NotaId && n.UsuarioId == request.UsuarioId, cancellationToken);

        if (nota == null)
        {
            return false;
        }

        // Remove all attachments first (cascade delete should handle this, but being explicit)
        if (nota.ArchivosAdjuntos.Any())
        {
            _context.ArchivosAdjuntos.RemoveRange(nota.ArchivosAdjuntos);
        }

        _context.Notas.Remove(nota);
        await _context.SaveChangesAsync(cancellationToken);

        return true;
    }
}