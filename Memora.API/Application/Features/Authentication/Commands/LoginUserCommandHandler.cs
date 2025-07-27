using Application.Common.Interfaces;
using Application.Features.Authentication.DTOs;
using Infrastructure.Data;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace Application.Features.Authentication.Commands;

public class LoginUserCommandHandler : IRequestHandler<LoginUserCommand, LoginResponseDto>
{
    private readonly MemoraDbContext _context;
    private readonly IPasswordHashService _passwordHashService;
    private readonly IJwtTokenService _jwtTokenService;

    public LoginUserCommandHandler(
        MemoraDbContext context, 
        IPasswordHashService passwordHashService,
        IJwtTokenService jwtTokenService)
    {
        _context = context;
        _passwordHashService = passwordHashService;
        _jwtTokenService = jwtTokenService;
    }

    public async Task<LoginResponseDto> Handle(LoginUserCommand request, CancellationToken cancellationToken)
    {
        // Find user by email
        var usuario = await _context.Usuarios
            .FirstOrDefaultAsync(u => u.CorreoElectronico == request.CorreoElectronico, cancellationToken);

        if (usuario == null)
        {
            throw new UnauthorizedAccessException("Invalid email or password");
        }

        // Verify password
        if (!_passwordHashService.VerifyPassword(request.Contrasena, usuario.ContrasenaHash))
        {
            throw new UnauthorizedAccessException("Invalid email or password");
        }

        // Generate JWT token
        var token = _jwtTokenService.GenerateToken(usuario);
        var expiresAt = _jwtTokenService.GetExpirationTime();

        return new LoginResponseDto
        {
            Token = token,
            ExpiresAt = expiresAt,
            Usuario = new UsuarioDto
            {
                Id = usuario.Id,
                NombreCompleto = usuario.NombreCompleto,
                CorreoElectronico = usuario.CorreoElectronico,
                FechaCreacion = usuario.FechaCreacion
            }
        };
    }
}