using Application.Features.Notas.DTOs;
using Domain.Entities;
using Infrastructure.Data;
using MediatR;

namespace Application.Features.Notas.Commands;

public class CreateNotaCommandHandler : IRequestHandler<CreateNotaCommand, NotaDto>
{
    private readonly MemoraDbContext _context;

    public CreateNotaCommandHandler(MemoraDbContext context)
    {
        _context = context;
    }

    public async Task<NotaDto> Handle(CreateNotaCommand request, CancellationToken cancellationToken)
    {
        var nota = new Nota
        {
            Id = Guid.NewGuid(),
            Titulo = string.IsNullOrWhiteSpace(request.Titulo) ? null : request.Titulo.Trim(),
            Contenido = request.Contenido.Trim(),
            FechaCreacion = DateTime.UtcNow,
            FechaModificacion = DateTime.UtcNow,
            UsuarioId = request.UsuarioId
        };

        _context.Notas.Add(nota);
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