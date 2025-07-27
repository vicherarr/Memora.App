namespace Application.Features.Authentication.DTOs;

public class SwaggerAuthDto
{
    public string CorreoElectronico { get; set; } = string.Empty;
    public string Contrasena { get; set; } = string.Empty;
}

public class SwaggerTokenResponseDto
{
    public string Token { get; set; } = string.Empty;
}