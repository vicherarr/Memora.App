using MediatR;
using Microsoft.EntityFrameworkCore;
using Infrastructure.Data;

namespace Application.Features.Archivos.Queries;

public class GetArchivoDataQueryHandler : IRequestHandler<GetArchivoDataQuery, ArchivoDataResult?>
{
    private readonly MemoraDbContext _context;

    public GetArchivoDataQueryHandler(MemoraDbContext context)
    {
        _context = context;
    }

    public async Task<ArchivoDataResult?> Handle(GetArchivoDataQuery request, CancellationToken cancellationToken)
    {
        var archivo = await _context.ArchivosAdjuntos
            .Include(a => a.Nota)
            .Where(a => a.Id == request.ArchivoId && a.Nota.UsuarioId == request.UsuarioId)
            .Select(a => new ArchivoDataResult
            {
                Data = a.DatosArchivo,
                ContentType = a.TipoMime,
                FileName = a.NombreOriginal
            })
            .FirstOrDefaultAsync(cancellationToken);

        return archivo;
    }
}