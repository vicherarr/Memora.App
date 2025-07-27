namespace Application.Features.Notas.DTOs;

public class CreateNotaDto
{
    public string? Titulo { get; set; }
    public string Contenido { get; set; } = string.Empty;
}