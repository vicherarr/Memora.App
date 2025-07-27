using Application.Features.Notas.DTOs;
using Infrastructure.Data;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace Application.Features.Notas.Queries;

public class GetNotaByIdQueryHandler : IRequestHandler<GetNotaByIdQuery, NotaDetailDto?>
{
    private readonly MemoraDbContext _context;

    public GetNotaByIdQueryHandler(MemoraDbContext context)
    {
        _context = context;
    }

    public async Task<NotaDetailDto?> Handle(GetNotaByIdQuery request, CancellationToken cancellationToken)
    {
        var nota = await _context.Notas
            .Where(n => n.Id == request.NotaId && n.UsuarioId == request.UsuarioId)
            .Select(n => new NotaDetailDto
            {
                Id = n.Id,
                Titulo = n.Titulo,
                Contenido = n.Contenido,
                FechaCreacion = n.FechaCreacion,
                FechaModificacion = n.FechaModificacion,
                UsuarioId = n.UsuarioId,
                TotalArchivosAdjuntos = n.ArchivosAdjuntos.Count()
            })
            .FirstOrDefaultAsync(cancellationToken);

        return nota;
    }
}