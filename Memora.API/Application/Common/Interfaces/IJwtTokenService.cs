using Domain.Entities;

namespace Application.Common.Interfaces;

public interface IJwtTokenService
{
    string GenerateToken(Usuario usuario);
    DateTime GetExpirationTime();
}