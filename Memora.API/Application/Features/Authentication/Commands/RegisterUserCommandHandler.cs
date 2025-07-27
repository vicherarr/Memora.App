using Application.Common.Interfaces;
using Application.Features.Authentication.DTOs;
using Domain.Entities;
using Infrastructure.Data;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace Application.Features.Authentication.Commands;

public class RegisterUserCommandHandler : IRequestHandler<RegisterUserCommand, RegisterResponseDto>
{
    private readonly MemoraDbContext _context;
    private readonly IPasswordHashService _passwordHashService;
    private readonly IJwtTokenService _jwtTokenService;

    public RegisterUserCommandHandler(MemoraDbContext context, IPasswordHashService passwordHashService, IJwtTokenService jwtTokenService)
    {
        _context = context;
        _passwordHashService = passwordHashService;
        _jwtTokenService = jwtTokenService;
    }

    public async Task<RegisterResponseDto> Handle(RegisterUserCommand request, CancellationToken cancellationToken)
    {

        // Check if user already exists
        var existingUser = await _context.Usuarios
            .FirstOrDefaultAsync(u => u.CorreoElectronico == request.CorreoElectronico,
                                cancellationToken);

        if (existingUser != null)
        {
            throw new ArgumentException("User with this email already exists");
        }

        // Create new user
        var usuario = new Usuario
        {
            Id = Guid.NewGuid(),
            NombreCompleto = request.NombreCompleto,
            CorreoElectronico = request.CorreoElectronico,
            ContrasenaHash = _passwordHashService.HashPassword(request.Contrasena),
            FechaCreacion = DateTime.UtcNow
        };

        _context.Usuarios.Add(usuario);
        await _context.SaveChangesAsync(cancellationToken);

        // Generate JWT token
        var token = _jwtTokenService.GenerateToken(usuario);
        var expiresAt = _jwtTokenService.GetExpirationTime();

        return new RegisterResponseDto
        {
            Token = token,
            Usuario = new UsuarioDto
            {
                Id = usuario.Id,
                NombreCompleto = usuario.NombreCompleto,
                CorreoElectronico = usuario.CorreoElectronico,
                FechaCreacion = usuario.FechaCreacion
            },
            ExpiresAt = expiresAt
        };

    }
}