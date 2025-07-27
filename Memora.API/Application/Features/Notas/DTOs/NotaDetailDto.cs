namespace Application.Features.Notas.DTOs;

public class NotaDetailDto
{
    public Guid Id { get; set; }
    public string? Titulo { get; set; }
    public string Contenido { get; set; } = string.Empty;
    public DateTime FechaCreacion { get; set; }
    public DateTime FechaModificacion { get; set; }
    public Guid UsuarioId { get; set; }
    public int TotalArchivosAdjuntos { get; set; }
}