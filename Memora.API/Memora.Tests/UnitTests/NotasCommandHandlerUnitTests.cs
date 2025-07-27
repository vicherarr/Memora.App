using Application.Features.Notas.Commands;
using Application.Features.Notas.Queries;
using Domain.Entities;
using FluentAssertions;
using Infrastructure.Data;
using Microsoft.EntityFrameworkCore;
using Xunit;

namespace Memora.Tests.UnitTests;

public class NotasCommandHandlerUnitTests
{
    private MemoraDbContext GetInMemoryDbContext()
    {
        var options = new DbContextOptionsBuilder<MemoraDbContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
            .Options;

        return new MemoraDbContext(options);
    }

    [Fact]
    public async Task CreateNotaCommandHandler_ShouldCreateNota_WhenValidDataProvided()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        
        var usuarioId = Guid.NewGuid();
        var usuario = new Usuario
        {
            Id = usuarioId,
            NombreCompleto = "testuser",
            CorreoElectronico = "test@example.com",
            ContrasenaHash = "hashedpassword",
            FechaCreacion = DateTime.UtcNow
        };

        context.Usuarios.Add(usuario);
        await context.SaveChangesAsync();

        var handler = new CreateNotaCommandHandler(context);
        var command = new CreateNotaCommand
        {
            Titulo = "Test Nota",
            Contenido = "Test Content",
            UsuarioId = usuarioId
        };

        // Act
        var result = await handler.Handle(command, CancellationToken.None);

        // Assert
        result.Should().NotBeNull();
        result.Titulo.Should().Be("Test Nota");
        result.Contenido.Should().Be("Test Content");
        result.UsuarioId.Should().Be(usuarioId);

        var notaInDb = await context.Notas.FirstOrDefaultAsync(n => n.Id == result.Id);
        notaInDb.Should().NotBeNull();
        notaInDb!.Titulo.Should().Be("Test Nota");
        notaInDb.Contenido.Should().Be("Test Content");
    }

    [Fact]
    public async Task UpdateNotaCommandHandler_ShouldUpdateNota_WhenValidDataProvided()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        
        var usuarioId = Guid.NewGuid();
        var usuario = new Usuario
        {
            Id = usuarioId,
            NombreCompleto = "testuser",
            CorreoElectronico = "test@example.com",
            ContrasenaHash = "hashedpassword",
            FechaCreacion = DateTime.UtcNow
        };

        var existingNota = new Nota
        {
            Id = Guid.NewGuid(),
            Titulo = "Original Title",
            Contenido = "Original Content",
            FechaCreacion = DateTime.UtcNow.AddDays(-1),
            FechaModificacion = DateTime.UtcNow.AddDays(-1),
            UsuarioId = usuarioId,
            Usuario = usuario
        };

        context.Usuarios.Add(usuario);
        context.Notas.Add(existingNota);
        await context.SaveChangesAsync();

        var handler = new UpdateNotaCommandHandler(context);
        var command = new UpdateNotaCommand
        {
            NotaId = existingNota.Id,
            Titulo = "Updated Title",
            Contenido = "Updated Content",
            UsuarioId = usuarioId
        };

        // Act
        var result = await handler.Handle(command, CancellationToken.None);

        // Assert
        result.Should().NotBeNull();
        result!.Titulo.Should().Be("Updated Title");
        result.Contenido.Should().Be("Updated Content");

        var notaInDb = await context.Notas.FirstOrDefaultAsync(n => n.Id == existingNota.Id);
        notaInDb!.Titulo.Should().Be("Updated Title");
        notaInDb.Contenido.Should().Be("Updated Content");
    }

    [Fact]
    public async Task UpdateNotaCommandHandler_ShouldReturnNull_WhenNotaNotFound()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        
        var usuarioId = Guid.NewGuid();
        var nonExistentNotaId = Guid.NewGuid();

        var handler = new UpdateNotaCommandHandler(context);
        var command = new UpdateNotaCommand
        {
            NotaId = nonExistentNotaId,
            Titulo = "Updated Title",
            Contenido = "Updated Content",
            UsuarioId = usuarioId
        };

        // Act
        var result = await handler.Handle(command, CancellationToken.None);

        // Assert
        result.Should().BeNull();
    }

    [Fact]
    public async Task UpdateNotaCommandHandler_ShouldReturnNull_WhenNotaDoesNotBelongToUser()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        
        var usuarioId = Guid.NewGuid();
        var otroUsuarioId = Guid.NewGuid();
        
        var otroUsuario = new Usuario
        {
            Id = otroUsuarioId,
            NombreCompleto = "otheruser",
            CorreoElectronico = "other@example.com",
            ContrasenaHash = "hashedpassword",
            FechaCreacion = DateTime.UtcNow
        };

        var notaDeOtroUsuario = new Nota
        {
            Id = Guid.NewGuid(),
            Titulo = "Other User's Note",
            Contenido = "Other User's Content",
            FechaCreacion = DateTime.UtcNow,
            FechaModificacion = DateTime.UtcNow,
            UsuarioId = otroUsuarioId,
            Usuario = otroUsuario
        };

        context.Usuarios.Add(otroUsuario);
        context.Notas.Add(notaDeOtroUsuario);
        await context.SaveChangesAsync();

        var handler = new UpdateNotaCommandHandler(context);
        var command = new UpdateNotaCommand
        {
            NotaId = notaDeOtroUsuario.Id,
            Titulo = "Hacked Title",
            Contenido = "Hacked Content",
            UsuarioId = usuarioId // Different user trying to update
        };

        // Act
        var result = await handler.Handle(command, CancellationToken.None);

        // Assert
        result.Should().BeNull();
        
        // Verify original note wasn't modified
        var originalNota = await context.Notas.FirstOrDefaultAsync(n => n.Id == notaDeOtroUsuario.Id);
        originalNota!.Titulo.Should().Be("Other User's Note");
        originalNota.Contenido.Should().Be("Other User's Content");
    }

    [Fact]
    public async Task DeleteNotaCommandHandler_ShouldDeleteNota_WhenValidDataProvided()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        
        var usuarioId = Guid.NewGuid();
        var usuario = new Usuario
        {
            Id = usuarioId,
            NombreCompleto = "testuser",
            CorreoElectronico = "test@example.com",
            ContrasenaHash = "hashedpassword",
            FechaCreacion = DateTime.UtcNow
        };

        var notaToDelete = new Nota
        {
            Id = Guid.NewGuid(),
            Titulo = "Note to Delete",
            Contenido = "This will be deleted",
            FechaCreacion = DateTime.UtcNow,
            FechaModificacion = DateTime.UtcNow,
            UsuarioId = usuarioId,
            Usuario = usuario
        };

        context.Usuarios.Add(usuario);
        context.Notas.Add(notaToDelete);
        await context.SaveChangesAsync();

        var handler = new DeleteNotaCommandHandler(context);
        var command = new DeleteNotaCommand
        {
            NotaId = notaToDelete.Id,
            UsuarioId = usuarioId
        };

        // Act
        var result = await handler.Handle(command, CancellationToken.None);

        // Assert
        result.Should().BeTrue();

        var deletedNota = await context.Notas.FirstOrDefaultAsync(n => n.Id == notaToDelete.Id);
        deletedNota.Should().BeNull();
    }

    [Fact]
    public async Task DeleteNotaCommandHandler_ShouldReturnFalse_WhenNotaNotFound()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        
        var usuarioId = Guid.NewGuid();
        var nonExistentNotaId = Guid.NewGuid();

        var handler = new DeleteNotaCommandHandler(context);
        var command = new DeleteNotaCommand
        {
            NotaId = nonExistentNotaId,
            UsuarioId = usuarioId
        };

        // Act
        var result = await handler.Handle(command, CancellationToken.None);

        // Assert
        result.Should().BeFalse();
    }

    [Fact]
    public async Task DeleteNotaCommandHandler_ShouldReturnFalse_WhenNotaDoesNotBelongToUser()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        
        var usuarioId = Guid.NewGuid();
        var otroUsuarioId = Guid.NewGuid();
        
        var otroUsuario = new Usuario
        {
            Id = otroUsuarioId,
            NombreCompleto = "otheruser",
            CorreoElectronico = "other@example.com",
            ContrasenaHash = "hashedpassword",
            FechaCreacion = DateTime.UtcNow
        };

        var notaDeOtroUsuario = new Nota
        {
            Id = Guid.NewGuid(),
            Titulo = "Other User's Note",
            Contenido = "Protected Content",
            FechaCreacion = DateTime.UtcNow,
            FechaModificacion = DateTime.UtcNow,
            UsuarioId = otroUsuarioId,
            Usuario = otroUsuario
        };

        context.Usuarios.Add(otroUsuario);
        context.Notas.Add(notaDeOtroUsuario);
        await context.SaveChangesAsync();

        var handler = new DeleteNotaCommandHandler(context);
        var command = new DeleteNotaCommand
        {
            NotaId = notaDeOtroUsuario.Id,
            UsuarioId = usuarioId // Different user trying to delete
        };

        // Act
        var result = await handler.Handle(command, CancellationToken.None);

        // Assert
        result.Should().BeFalse();
        
        // Verify note still exists
        var stillExistingNota = await context.Notas.FirstOrDefaultAsync(n => n.Id == notaDeOtroUsuario.Id);
        stillExistingNota.Should().NotBeNull();
    }

    [Fact]
    public async Task GetNotaByIdQueryHandler_ShouldReturnNota_WhenValidDataProvided()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        
        var usuarioId = Guid.NewGuid();
        var usuario = new Usuario
        {
            Id = usuarioId,
            NombreCompleto = "testuser",
            CorreoElectronico = "test@example.com",
            ContrasenaHash = "hashedpassword",
            FechaCreacion = DateTime.UtcNow
        };

        var nota = new Nota
        {
            Id = Guid.NewGuid(),
            Titulo = "Test Note",
            Contenido = "Test Content",
            FechaCreacion = DateTime.UtcNow,
            FechaModificacion = DateTime.UtcNow,
            UsuarioId = usuarioId,
            Usuario = usuario
        };

        // Add some archivos adjuntos to test count
        var archivos = new List<ArchivoAdjunto>
        {
            new ArchivoAdjunto
            {
                Id = Guid.NewGuid(),
                NombreOriginal = "test1.jpg",
                TipoArchivo = Domain.Enums.TipoDeArchivo.Imagen,
                TipoMime = "image/jpeg",
                TamanoBytes = 1024,
                DatosArchivo = new byte[] { 1, 2, 3 },
                FechaSubida = DateTime.UtcNow,
                NotaId = nota.Id,
                Nota = nota
            },
            new ArchivoAdjunto
            {
                Id = Guid.NewGuid(),
                NombreOriginal = "test2.mp4",
                TipoArchivo = Domain.Enums.TipoDeArchivo.Video,
                TipoMime = "video/mp4",
                TamanoBytes = 2048,
                DatosArchivo = new byte[] { 4, 5, 6 },
                FechaSubida = DateTime.UtcNow,
                NotaId = nota.Id,
                Nota = nota
            }
        };

        context.Usuarios.Add(usuario);
        context.Notas.Add(nota);
        context.ArchivosAdjuntos.AddRange(archivos);
        await context.SaveChangesAsync();

        var handler = new GetNotaByIdQueryHandler(context);
        var query = new GetNotaByIdQuery
        {
            NotaId = nota.Id,
            UsuarioId = usuarioId
        };

        // Act
        var result = await handler.Handle(query, CancellationToken.None);

        // Assert
        result.Should().NotBeNull();
        result!.Id.Should().Be(nota.Id);
        result.Titulo.Should().Be("Test Note");
        result.Contenido.Should().Be("Test Content");
        result.TotalArchivosAdjuntos.Should().Be(2);
    }

    [Fact]
    public async Task GetNotaByIdQueryHandler_ShouldReturnNull_WhenNotaNotFound()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        
        var usuarioId = Guid.NewGuid();
        var nonExistentNotaId = Guid.NewGuid();

        var handler = new GetNotaByIdQueryHandler(context);
        var query = new GetNotaByIdQuery
        {
            NotaId = nonExistentNotaId,
            UsuarioId = usuarioId
        };

        // Act
        var result = await handler.Handle(query, CancellationToken.None);

        // Assert
        result.Should().BeNull();
    }

    [Fact]
    public async Task GetNotaByIdQueryHandler_ShouldReturnNull_WhenNotaDoesNotBelongToUser()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        
        var usuarioId = Guid.NewGuid();
        var otroUsuarioId = Guid.NewGuid();
        
        var otroUsuario = new Usuario
        {
            Id = otroUsuarioId,
            NombreCompleto = "otheruser",
            CorreoElectronico = "other@example.com",
            ContrasenaHash = "hashedpassword",
            FechaCreacion = DateTime.UtcNow
        };

        var notaDeOtroUsuario = new Nota
        {
            Id = Guid.NewGuid(),
            Titulo = "Other User's Note",
            Contenido = "Protected Content",
            FechaCreacion = DateTime.UtcNow,
            FechaModificacion = DateTime.UtcNow,
            UsuarioId = otroUsuarioId,
            Usuario = otroUsuario
        };

        context.Usuarios.Add(otroUsuario);
        context.Notas.Add(notaDeOtroUsuario);
        await context.SaveChangesAsync();

        var handler = new GetNotaByIdQueryHandler(context);
        var query = new GetNotaByIdQuery
        {
            NotaId = notaDeOtroUsuario.Id,
            UsuarioId = usuarioId // Different user trying to access
        };

        // Act
        var result = await handler.Handle(query, CancellationToken.None);

        // Assert
        result.Should().BeNull();
    }
}