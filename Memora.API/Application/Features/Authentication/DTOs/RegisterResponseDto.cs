namespace Application.Features.Authentication.DTOs;

public class RegisterResponseDto
{
    public string Token { get; set; } = string.Empty;
    public UsuarioDto Usuario { get; set; } = null!;
    public DateTime ExpiresAt { get; set; }
}