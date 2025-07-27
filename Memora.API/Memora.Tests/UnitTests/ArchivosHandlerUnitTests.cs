using Application.Common.Interfaces;
using Application.Features.Archivos.Commands;
using Application.Features.Archivos.Queries;
using Domain.Entities;
using Domain.Enums;
using FluentAssertions;
using Infrastructure.Data;
using Microsoft.EntityFrameworkCore;
using Moq;
using Xunit;

namespace Memora.Tests.UnitTests;

public class ArchivosHandlerUnitTests
{
    private MemoraDbContext GetInMemoryDbContext()
    {
        var options = new DbContextOptionsBuilder<MemoraDbContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
            .Options;

        return new MemoraDbContext(options);
    }

    [Fact]
    public async Task UploadArchivoCommandHandler_ShouldUploadFile_WhenValidDataProvided()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        var mockFileProcessingService = new Mock<IFileProcessingService>();

        var usuarioId = Guid.NewGuid();
        var notaId = Guid.NewGuid();
        
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
            Id = notaId,
            Titulo = "Test Note",
            Contenido = "Test Content",
            FechaCreacion = DateTime.UtcNow,
            FechaModificacion = DateTime.UtcNow,
            UsuarioId = usuarioId,
            Usuario = usuario
        };

        context.Usuarios.Add(usuario);
        context.Notas.Add(nota);
        await context.SaveChangesAsync();

        var fileContent = new byte[] { 1, 2, 3, 4, 5 };

        mockFileProcessingService.Setup(x => x.ValidateFileAsync(It.IsAny<byte[]>(), It.IsAny<string>(), It.IsAny<string>()))
            .ReturnsAsync(true);
        mockFileProcessingService.Setup(x => x.DetectFileType(It.IsAny<string>()))
            .Returns(TipoDeArchivo.Imagen);
        mockFileProcessingService
            .Setup(x => x.CompressImageAsync(It.IsAny<byte[]>(), It.IsAny<string>()))
            .ReturnsAsync(fileContent); // <-- Devuelve el contenido original
        mockFileProcessingService
            .Setup(x => x.GetValidMimeType(It.IsAny<byte[]>(), It.IsAny<string>()))
            .Returns("image/jpeg");

        var handler = new UploadArchivoCommandHandler(context, mockFileProcessingService.Object);
        var command = new UploadArchivoCommand
        {
            NotaId = notaId,
            UsuarioId = usuarioId,
            FileData = fileContent,
            FileName = "test.jpg",
            ContentType = "image/jpeg",
        };

        // Act
        var result = await handler.Handle(command, CancellationToken.None);

        // Assert
        result.Should().NotBeNull();
        result.NombreOriginal.Should().Be("test.jpg");
        result.TipoMime.Should().Be("image/jpeg");
        result.TamanoBytes.Should().Be(fileContent.Length);
        result.ArchivoId.Should().NotBeEmpty();

        var archivoInDb = await context.ArchivosAdjuntos.FirstOrDefaultAsync(a => a.Id == result.ArchivoId);
        archivoInDb.Should().NotBeNull();
        archivoInDb!.DatosArchivo.Should().BeEquivalentTo(fileContent);
    }

    [Fact]
    public async Task UploadArchivoCommandHandler_ShouldThrowException_WhenFileValidationFails()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        var mockFileProcessingService = new Mock<IFileProcessingService>();

        var usuarioId = Guid.NewGuid();
        var notaId = Guid.NewGuid();

        mockFileProcessingService.Setup(x => x.ValidateFileAsync(It.IsAny<byte[]>(), It.IsAny<string>(), It.IsAny<string>()))
            .ReturnsAsync(false);

        var handler = new UploadArchivoCommandHandler(context, mockFileProcessingService.Object);
        var command = new UploadArchivoCommand
        {
            NotaId = notaId,
            UsuarioId = usuarioId,
            FileData = new byte[] { 1, 2, 3 },
            FileName = "malicious.exe",
            ContentType = "application/octet-stream"
        };

        // Act & Assert
        await FluentActions.Invoking(() => handler.Handle(command, CancellationToken.None))
            .Should().ThrowAsync<UnauthorizedAccessException>();
    }


    [Fact]
    public async Task DeleteArchivoCommandHandler_ShouldDeleteFile_WhenValidDataProvided()
    {
        // Arrange
        using var context = GetInMemoryDbContext();

        var usuarioId = Guid.NewGuid();
        var notaId = Guid.NewGuid();
        var archivoId = Guid.NewGuid();
        
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
            Id = notaId,
            Titulo = "Test Note",
            Contenido = "Test Content",
            FechaCreacion = DateTime.UtcNow,
            FechaModificacion = DateTime.UtcNow,
            UsuarioId = usuarioId,
            Usuario = usuario
        };

        var archivo = new ArchivoAdjunto
        {
            Id = archivoId,
            NombreOriginal = "test.jpg",
            TipoArchivo = TipoDeArchivo.Imagen,
            TipoMime = "image/jpeg",
            TamanoBytes = 1024,
            DatosArchivo = new byte[] { 1, 2, 3, 4, 5 },
            FechaSubida = DateTime.UtcNow,
            NotaId = notaId,
            Nota = nota
        };

        context.Usuarios.Add(usuario);
        context.Notas.Add(nota);
        context.ArchivosAdjuntos.Add(archivo);
        await context.SaveChangesAsync();

        var handler = new DeleteArchivoCommandHandler(context);
        var command = new DeleteArchivoCommand
        {
            ArchivoId = archivoId,
            UsuarioId = usuarioId
        };

        // Act
        var result = await handler.Handle(command, CancellationToken.None);

        // Assert
        result.Should().BeTrue();

        var deletedArchivo = await context.ArchivosAdjuntos.FirstOrDefaultAsync(a => a.Id == archivoId);
        deletedArchivo.Should().BeNull();
    }


    [Fact]
    public async Task GetArchivoByIdQueryHandler_ShouldReturnArchivo_WhenValidDataProvided()
    {
        // Arrange
        using var context = GetInMemoryDbContext();

        var usuarioId = Guid.NewGuid();
        var notaId = Guid.NewGuid();
        var archivoId = Guid.NewGuid();
        
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
            Id = notaId,
            Titulo = "Test Note",
            Contenido = "Test Content",
            FechaCreacion = DateTime.UtcNow,
            FechaModificacion = DateTime.UtcNow,
            UsuarioId = usuarioId,
            Usuario = usuario
        };

        var archivo = new ArchivoAdjunto
        {
            Id = archivoId,
            NombreOriginal = "test.jpg",
            TipoArchivo = TipoDeArchivo.Imagen,
            TipoMime = "image/jpeg",
            TamanoBytes = 1024,
            DatosArchivo = new byte[] { 1, 2, 3, 4, 5 },
            FechaSubida = DateTime.UtcNow,
            NotaId = notaId,
            Nota = nota
        };

        context.Usuarios.Add(usuario);
        context.Notas.Add(nota);
        context.ArchivosAdjuntos.Add(archivo);
        await context.SaveChangesAsync();

        var handler = new GetArchivoByIdQueryHandler(context);
        var query = new GetArchivoByIdQuery
        {
            ArchivoId = archivoId,
            UsuarioId = usuarioId
        };

        // Act
        var result = await handler.Handle(query, CancellationToken.None);

        // Assert
        result.Should().NotBeNull();
        result!.Id.Should().Be(archivoId);
        result.NombreOriginal.Should().Be("test.jpg");
        result.TipoMime.Should().Be("image/jpeg");
        result.TamanoBytes.Should().Be(1024);
        result.TipoArchivo.Should().Be(TipoDeArchivo.Imagen);
    }

    [Fact]
    public async Task GetArchivoByIdQueryHandler_ShouldReturnNull_WhenFileNotFound()
    {
        // Arrange
        using var context = GetInMemoryDbContext();

        var usuarioId = Guid.NewGuid();
        var nonExistentArchivoId = Guid.NewGuid();

        var handler = new GetArchivoByIdQueryHandler(context);
        var query = new GetArchivoByIdQuery
        {
            ArchivoId = nonExistentArchivoId,
            UsuarioId = usuarioId
        };

        // Act
        var result = await handler.Handle(query, CancellationToken.None);

        // Assert
        result.Should().BeNull();
    }

    [Fact]
    public async Task GetArchivoByIdQueryHandler_ShouldReturnNull_WhenFileDoesNotBelongToUser()
    {
        // Arrange
        using var context = GetInMemoryDbContext();

        var usuarioId = Guid.NewGuid();
        var otroUsuarioId = Guid.NewGuid();
        var notaId = Guid.NewGuid();
        var archivoId = Guid.NewGuid();

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
            Id = notaId,
            Titulo = "Other User's Note",
            Contenido = "Protected Content",
            FechaCreacion = DateTime.UtcNow,
            FechaModificacion = DateTime.UtcNow,
            UsuarioId = otroUsuarioId,
            Usuario = otroUsuario
        };

        var archivoDeOtroUsuario = new ArchivoAdjunto
        {
            Id = archivoId,
            NombreOriginal = "protected.jpg",
            TipoArchivo = TipoDeArchivo.Imagen,
            TipoMime = "image/jpeg",
            TamanoBytes = 1024,
            DatosArchivo = new byte[] { 1, 2, 3, 4, 5 },
            FechaSubida = DateTime.UtcNow,
            NotaId = notaId,
            Nota = notaDeOtroUsuario
        };

        context.Usuarios.Add(otroUsuario);
        context.Notas.Add(notaDeOtroUsuario);
        context.ArchivosAdjuntos.Add(archivoDeOtroUsuario);
        await context.SaveChangesAsync();

        var handler = new GetArchivoByIdQueryHandler(context);
        var query = new GetArchivoByIdQuery
        {
            ArchivoId = archivoId,
            UsuarioId = usuarioId // Different user trying to access
        };

        // Act
        var result = await handler.Handle(query, CancellationToken.None);

        // Assert
        result.Should().BeNull();
    }

    [Fact]
    public async Task GetArchivoDataQueryHandler_ShouldReturnArchivoData_WhenValidDataProvided()
    {
        // Arrange
        using var context = GetInMemoryDbContext();

        var usuarioId = Guid.NewGuid();
        var notaId = Guid.NewGuid();
        var archivoId = Guid.NewGuid();
        var fileData = new byte[] { 1, 2, 3, 4, 5 };
        
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
            Id = notaId,
            Titulo = "Test Note",
            Contenido = "Test Content",
            FechaCreacion = DateTime.UtcNow,
            FechaModificacion = DateTime.UtcNow,
            UsuarioId = usuarioId,
            Usuario = usuario
        };

        var archivo = new ArchivoAdjunto
        {
            Id = archivoId,
            NombreOriginal = "test.jpg",
            TipoArchivo = TipoDeArchivo.Imagen,
            TipoMime = "image/jpeg",
            TamanoBytes = fileData.Length,
            DatosArchivo = fileData,
            FechaSubida = DateTime.UtcNow,
            NotaId = notaId,
            Nota = nota
        };

        context.Usuarios.Add(usuario);
        context.Notas.Add(nota);
        context.ArchivosAdjuntos.Add(archivo);
        await context.SaveChangesAsync();

        var handler = new GetArchivoDataQueryHandler(context);
        var query = new GetArchivoDataQuery
        {
            ArchivoId = archivoId,
            UsuarioId = usuarioId
        };

        // Act
        var result = await handler.Handle(query, CancellationToken.None);

        // Assert
        result.Should().NotBeNull();
        result!.FileName.Should().Be("test.jpg");
        result.ContentType.Should().Be("image/jpeg");
        result.Data.Should().BeEquivalentTo(fileData);
    }

    [Fact]
    public async Task GetArchivoDataQueryHandler_ShouldReturnNull_WhenFileNotFound()
    {
        // Arrange
        using var context = GetInMemoryDbContext();

        var usuarioId = Guid.NewGuid();
        var nonExistentArchivoId = Guid.NewGuid();

        var handler = new GetArchivoDataQueryHandler(context);
        var query = new GetArchivoDataQuery
        {
            ArchivoId = nonExistentArchivoId,
            UsuarioId = usuarioId
        };

        // Act
        var result = await handler.Handle(query, CancellationToken.None);

        // Assert
        result.Should().BeNull();
    }

    [Fact]
    public async Task GetArchivoDataQueryHandler_ShouldReturnNull_WhenFileDoesNotBelongToUser()
    {
        // Arrange
        using var context = GetInMemoryDbContext();

        var usuarioId = Guid.NewGuid();
        var otroUsuarioId = Guid.NewGuid();
        var notaId = Guid.NewGuid();
        var archivoId = Guid.NewGuid();

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
            Id = notaId,
            Titulo = "Other User's Note",
            Contenido = "Protected Content",
            FechaCreacion = DateTime.UtcNow,
            FechaModificacion = DateTime.UtcNow,
            UsuarioId = otroUsuarioId,
            Usuario = otroUsuario
        };

        var archivoDeOtroUsuario = new ArchivoAdjunto
        {
            Id = archivoId,
            NombreOriginal = "protected.jpg",
            TipoArchivo = TipoDeArchivo.Imagen,
            TipoMime = "image/jpeg",
            TamanoBytes = 1024,
            DatosArchivo = new byte[] { 1, 2, 3, 4, 5 },
            FechaSubida = DateTime.UtcNow,
            NotaId = notaId,
            Nota = notaDeOtroUsuario
        };

        context.Usuarios.Add(otroUsuario);
        context.Notas.Add(notaDeOtroUsuario);
        context.ArchivosAdjuntos.Add(archivoDeOtroUsuario);
        await context.SaveChangesAsync();

        var handler = new GetArchivoDataQueryHandler(context);
        var query = new GetArchivoDataQuery
        {
            ArchivoId = archivoId,
            UsuarioId = usuarioId // Different user trying to access
        };

        // Act
        var result = await handler.Handle(query, CancellationToken.None);

        // Assert
        result.Should().BeNull();
    }

    [Fact]
    public async Task UploadArchivoCommandHandler_ShouldThrowException_WhenNotaDoesNotBelongToUser()
    {
        // Arrange
        using var context = GetInMemoryDbContext();
        var mockFileProcessingService = new Mock<IFileProcessingService>();

        var usuarioId = Guid.NewGuid();
        var otroUsuarioId = Guid.NewGuid();
        var notaId = Guid.NewGuid();

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
            Id = notaId,
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

        mockFileProcessingService.Setup(x => x.ValidateFileAsync(It.IsAny<byte[]>(), It.IsAny<string>(), It.IsAny<string>()))
            .ReturnsAsync(true);

        var handler = new UploadArchivoCommandHandler(context, mockFileProcessingService.Object);
        var command = new UploadArchivoCommand
        {
            NotaId = notaId,
            UsuarioId = usuarioId, // Different user trying to upload
            FileData = new byte[] { 1, 2, 3, 4, 5 },
            FileName = "test.jpg",
            ContentType = "image/jpeg",
        };

        // Act & Assert
        await FluentActions.Invoking(() => handler.Handle(command, CancellationToken.None))
            .Should().ThrowAsync<UnauthorizedAccessException>();
    }
}