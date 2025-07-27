namespace Application.Features.Authentication.DTOs;

public class RegisterUserDto
{
    public string NombreCompleto { get; set; } = string.Empty;
    public string CorreoElectronico { get; set; } = string.Empty;
    public string Contrasena { get; set; } = string.Empty;
}