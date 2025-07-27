namespace Application.Features.Authentication.DTOs;

public class LoginUserDto
{
    public string CorreoElectronico { get; set; } = string.Empty;
    public string Contrasena { get; set; } = string.Empty;
}