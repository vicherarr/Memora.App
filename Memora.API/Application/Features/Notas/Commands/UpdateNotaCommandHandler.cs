using Application.Features.Notas.DTOs;
using Infrastructure.Data;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace Application.Features.Notas.Commands;

public class UpdateNotaCommandHandler : IRequestHandler<UpdateNotaCommand, NotaDto?>
{
    private readonly MemoraDbContext _context;

    public UpdateNotaCommandHandler(MemoraDbContext context)
    {
        _context = context;
    }

    public async Task<NotaDto?> Handle(UpdateNotaCommand request, CancellationToken cancellationToken)
    {
        var nota = await _context.Notas
            .FirstOrDefaultAsync(n => n.Id == request.NotaId && n.UsuarioId == request.UsuarioId, cancellationToken);

        if (nota == null)
        {
            return null;
        }

        nota.Titulo = string.IsNullOrWhiteSpace(request.Titulo) ? null : request.Titulo.Trim();
        nota.Contenido = request.Contenido.Trim();
        nota.FechaModificacion = DateTime.UtcNow;

        await _context.SaveChangesAsync(cancellationToken);

        return new NotaDto
        {
            Id = nota.Id,
            Titulo = nota.Titulo,
            Contenido = nota.Contenido,
            FechaCreacion = nota.FechaCreacion,
            FechaModificacion = nota.FechaModificacion,
            UsuarioId = nota.UsuarioId
        };
    }
}