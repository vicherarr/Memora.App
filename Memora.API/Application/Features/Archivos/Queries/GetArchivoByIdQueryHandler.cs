using MediatR;
using Microsoft.EntityFrameworkCore;
using Application.Features.Archivos.DTOs;
using Infrastructure.Data;

namespace Application.Features.Archivos.Queries;

public class GetArchivoByIdQueryHandler : IRequestHandler<GetArchivoByIdQuery, ArchivoAdjuntoDto?>
{
    private readonly MemoraDbContext _context;

    public GetArchivoByIdQueryHandler(MemoraDbContext context)
    {
        _context = context;
    }

    public async Task<ArchivoAdjuntoDto?> Handle(GetArchivoByIdQuery request, CancellationToken cancellationToken)
    {
        var archivo = await _context.ArchivosAdjuntos
            .Include(a => a.Nota)
            .Where(a => a.Id == request.ArchivoId && a.Nota.UsuarioId == request.UsuarioId)
            .Select(a => new ArchivoAdjuntoDto
            {
                Id = a.Id,
                NombreOriginal = a.NombreOriginal,
                TipoArchivo = a.TipoArchivo,
                TipoMime = a.TipoMime,
                TamanoBytes = a.TamanoBytes,
                FechaSubida = a.FechaSubida,
                NotaId = a.NotaId
            })
            .FirstOrDefaultAsync(cancellationToken);

        return archivo;
    }
}