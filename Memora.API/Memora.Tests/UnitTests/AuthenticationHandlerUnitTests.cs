using Application.Common.Interfaces;
using Application.Features.Authentication.Commands;
using Application.Features.Authentication.DTOs;
using Domain.Entities;
using FluentAssertions;
using Infrastructure.Data;
using Microsoft.EntityFrameworkCore;
using Moq;
using Xunit;

namespace Memora.Tests.UnitTests;

public class AuthenticationHandlerUnitTests
{
    private MemoraDbContext GetInMemoryDbContext()
    {
        var options = new DbContextOptionsBuilder<MemoraDbContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
            .Options;

        return new MemoraDbContext(options);
    }

    [Fact]
    public async Task RegisterUserCommandHandler_ShouldCreateUser_WhenValidDataProvided()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        var mockPasswordHashService = new Mock<IPasswordHashService>();
        var mockJwtTokenService = new Mock<IJwtTokenService>();

        mockPasswordHashService.Setup(x => x.HashPassword("SecureP@ssw0rd2024!"))
            .Returns("hashedpassword");
        
        mockJwtTokenService.Setup(x => x.GenerateToken(It.IsAny<Usuario>()))
            .Returns("test-jwt-token");
        
        mockJwtTokenService.Setup(x => x.GetExpirationTime())
            .Returns(DateTime.UtcNow.AddHours(1));

        var handler = new RegisterUserCommandHandler(context, mockPasswordHashService.Object, mockJwtTokenService.Object);
        var command = new RegisterUserCommand
        {
            NombreCompleto = "testuser",
            CorreoElectronico = "test@example.com",
            Contrasena = "SecureP@ssw0rd2024!"
        };

        // Act
        var result = await handler.Handle(command, CancellationToken.None);

        // Assert
        result.Should().NotBeNull();
        result.Token.Should().Be("test-jwt-token");
        result.Usuario.Should().NotBeNull();
        result.Usuario.NombreCompleto.Should().Be("testuser");
        result.Usuario.CorreoElectronico.Should().Be("test@example.com");

        var userInDb = await context.Usuarios.FirstOrDefaultAsync(u => u.CorreoElectronico == "test@example.com");
        userInDb.Should().NotBeNull();
        userInDb!.ContrasenaHash.Should().Be("hashedpassword");
    }

    [Fact]
    public async Task RegisterUserCommandHandler_ShouldThrowException_WhenUserAlreadyExists()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        var mockPasswordHashService = new Mock<IPasswordHashService>();
        var mockJwtTokenService = new Mock<IJwtTokenService>();

        // Create existing user
        var existingUser = new Usuario
        {
            Id = Guid.NewGuid(),
            NombreCompleto = "testuser",
            CorreoElectronico = "test@example.com",
            ContrasenaHash = "existinghashedpassword",
            FechaCreacion = DateTime.UtcNow
        };

        context.Usuarios.Add(existingUser);
        await context.SaveChangesAsync();

        var handler = new RegisterUserCommandHandler(context, mockPasswordHashService.Object, mockJwtTokenService.Object);
        var command = new RegisterUserCommand
        {
            NombreCompleto = "testuser",
            CorreoElectronico = "test@example.com",
            Contrasena = "SecureP@ssw0rd2024!"
        };

        // Act & Assert
        await FluentActions.Invoking(() => handler.Handle(command, CancellationToken.None))
            .Should().ThrowAsync<ArgumentException>()
            .WithMessage("User with this email already exists");
    }

    [Fact]
    public async Task LoginUserCommandHandler_ShouldReturnToken_WhenValidCredentials()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        var mockPasswordHashService = new Mock<IPasswordHashService>();
        var mockJwtTokenService = new Mock<IJwtTokenService>();

        var existingUser = new Usuario
        {
            Id = Guid.NewGuid(),
            NombreCompleto = "testuser",
            CorreoElectronico = "test@example.com",
            ContrasenaHash = "hashedpassword",
            FechaCreacion = DateTime.UtcNow
        };

        context.Usuarios.Add(existingUser);
        await context.SaveChangesAsync();

        mockPasswordHashService.Setup(x => x.VerifyPassword("SecureP@ssw0rd2024!", "hashedpassword"))
            .Returns(true);
        
        mockJwtTokenService.Setup(x => x.GenerateToken(It.IsAny<Usuario>()))
            .Returns("test-jwt-token");
        
        mockJwtTokenService.Setup(x => x.GetExpirationTime())
            .Returns(DateTime.UtcNow.AddHours(1));

        var handler = new LoginUserCommandHandler(context, mockPasswordHashService.Object, mockJwtTokenService.Object);
        var command = new LoginUserCommand
        {
            CorreoElectronico = "test@example.com",
            Contrasena = "SecureP@ssw0rd2024!"
        };

        // Act
        var result = await handler.Handle(command, CancellationToken.None);

        // Assert
        result.Should().NotBeNull();
        result.Token.Should().Be("test-jwt-token");
        result.Usuario.Should().NotBeNull();
        result.Usuario.NombreCompleto.Should().Be("testuser");
        result.Usuario.CorreoElectronico.Should().Be("test@example.com");
    }

    [Fact]
    public async Task LoginUserCommandHandler_ShouldThrowException_WhenUserNotFound()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        var mockPasswordHashService = new Mock<IPasswordHashService>();
        var mockJwtTokenService = new Mock<IJwtTokenService>();

        var handler = new LoginUserCommandHandler(context, mockPasswordHashService.Object, mockJwtTokenService.Object);
        var command = new LoginUserCommand
        {
            CorreoElectronico = "nonexistent@example.com",
            Contrasena = "SecureP@ssw0rd2024!"
        };

        // Act & Assert
        await FluentActions.Invoking(() => handler.Handle(command, CancellationToken.None))
            .Should().ThrowAsync<UnauthorizedAccessException>();
    }

    [Fact]
    public async Task LoginUserCommandHandler_ShouldThrowException_WhenInvalidPassword()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        var mockPasswordHashService = new Mock<IPasswordHashService>();
        var mockJwtTokenService = new Mock<IJwtTokenService>();

        var existingUser = new Usuario
        {
            Id = Guid.NewGuid(),
            NombreCompleto = "testuser",
            CorreoElectronico = "test@example.com",
            ContrasenaHash = "hashedpassword",
            FechaCreacion = DateTime.UtcNow
        };

        context.Usuarios.Add(existingUser);
        await context.SaveChangesAsync();

        mockPasswordHashService.Setup(x => x.VerifyPassword("WrongPassword!", "hashedpassword"))
            .Returns(false);

        var handler = new LoginUserCommandHandler(context, mockPasswordHashService.Object, mockJwtTokenService.Object);
        var command = new LoginUserCommand
        {
            CorreoElectronico = "test@example.com",
            Contrasena = "WrongPassword!"
        };

        // Act & Assert
        await FluentActions.Invoking(() => handler.Handle(command, CancellationToken.None))
            .Should().ThrowAsync<UnauthorizedAccessException>();
    }
}