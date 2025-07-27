using MediatR;
using Microsoft.EntityFrameworkCore;
using Infrastructure.Data;

namespace Application.Features.Archivos.Commands;

public class DeleteArchivoCommandHandler : IRequestHandler<DeleteArchivoCommand, bool>
{
    private readonly MemoraDbContext _context;

    public DeleteArchivoCommandHandler(MemoraDbContext context)
    {
        _context = context;
    }

    public async Task<bool> Handle(DeleteArchivoCommand request, CancellationToken cancellationToken)
    {
        // Buscar el archivo y verificar que el usuario tenga permisos
        var archivo = await _context.ArchivosAdjuntos
            .Include(a => a.Nota)
            .FirstOrDefaultAsync(a => a.Id == request.ArchivoId && a.Nota.UsuarioId == request.UsuarioId, cancellationToken);

        if (archivo == null)
        {
            throw new UnauthorizedAccessException("No tienes permisos para eliminar este archivo o el archivo no existe.");
        }

        _context.ArchivosAdjuntos.Remove(archivo);
        var result = await _context.SaveChangesAsync(cancellationToken);

        return result > 0;
    }
}