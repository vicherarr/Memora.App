namespace Application.Features.Authentication.DTOs;

public class UsuarioDto
{
    public Guid Id { get; set; }
    public string NombreCompleto { get; set; } = string.Empty;
    public string CorreoElectronico { get; set; } = string.Empty;
    public DateTime FechaCreacion { get; set; }
}