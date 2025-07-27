using Application.Features.Notas.DTOs;
using Infrastructure.Data;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace Application.Features.Notas.Queries;

public class GetUserNotasQueryHandler : IRequestHandler<GetUserNotasQuery, PaginatedNotasDto>
{
    private readonly MemoraDbContext _context;

    public GetUserNotasQueryHandler(MemoraDbContext context)
    {
        _context = context;
    }

    public async Task<PaginatedNotasDto> Handle(GetUserNotasQuery request, CancellationToken cancellationToken)
    {
        var query = _context.Notas
            .Where(n => n.UsuarioId == request.UsuarioId);

        // Aplicar filtro de búsqueda si se proporciona
        if (!string.IsNullOrWhiteSpace(request.SearchTerm))
        {
            var searchTerm = request.SearchTerm.ToLower();
            query = query.Where(n => 
                (n.Titulo != null && n.Titulo.ToLower().Contains(searchTerm)) ||
                n.Contenido.ToLower().Contains(searchTerm));
        }

        query = query.OrderByDescending(n => n.FechaModificacion);

        var totalCount = await query.CountAsync(cancellationToken);

        var notas = await query
            .Skip((request.PageNumber - 1) * request.PageSize)
            .Take(request.PageSize)
            .Select(n => new NotaDto
            {
                Id = n.Id,
                Titulo = n.Titulo,
                Contenido = n.Contenido,
                FechaCreacion = n.FechaCreacion,
                FechaModificacion = n.FechaModificacion,
                UsuarioId = n.UsuarioId
            })
            .ToListAsync(cancellationToken);

        return new PaginatedNotasDto
        {
            Notas = notas,
            TotalCount = totalCount,
            PageNumber = request.PageNumber,
            PageSize = request.PageSize
        };
    }
}