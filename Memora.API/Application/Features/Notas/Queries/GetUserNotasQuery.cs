using Application.Features.Notas.DTOs;
using MediatR;

namespace Application.Features.Notas.Queries;

public class GetUserNotasQuery : IRequest<PaginatedNotasDto>
{
    public Guid UsuarioId { get; set; }
    public int PageNumber { get; set; } = 1;
    public int PageSize { get; set; } = 10;
    public string? SearchTerm { get; set; }
}