using Application.Features.Authentication.DTOs;
using MediatR;

namespace Application.Features.Authentication.Commands;

public class RegisterUserCommand : IRequest<RegisterResponseDto>
{
    public string NombreCompleto { get; set; } = string.Empty;
    public string CorreoElectronico { get; set; } = string.Empty;
    public string Contrasena { get; set; } = string.Empty;
}