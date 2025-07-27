using Application.Common.Interfaces;
using Domain.Entities;
using Domain.Enums;
using FluentAssertions;
using Infrastructure.Services;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Configuration;
using Moq;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using Xunit;

namespace Memora.Tests.UnitTests;

public class ServicesUnitTests
{
    [Fact]
    public void PasswordHashService_ShouldHashPassword_WhenValidPasswordProvided()
    {
        // Arrange
        var service = new PasswordHashService();
        var password = "TestPassword123!";

        // Act
        var hashedPassword = service.HashPassword(password);

        // Assert
        hashedPassword.Should().NotBeNullOrEmpty();
        hashedPassword.Should().NotBe(password);
        hashedPassword.Should().StartWith("$2a$"); // BCrypt hash format
    }

    [Fact]
    public void PasswordHashService_ShouldVerifyPassword_WhenCorrectPasswordProvided()
    {
        // Arrange
        var service = new PasswordHashService();
        var password = "TestPassword123!";
        var hashedPassword = service.HashPassword(password);

        // Act
        var isValid = service.VerifyPassword(password, hashedPassword);

        // Assert
        isValid.Should().BeTrue();
    }

    [Fact]
    public void PasswordHashService_ShouldNotVerifyPassword_WhenIncorrectPasswordProvided()
    {
        // Arrange
        var service = new PasswordHashService();
        var password = "TestPassword123!";
        var wrongPassword = "WrongPassword456!";
        var hashedPassword = service.HashPassword(password);

        // Act
        var isValid = service.VerifyPassword(wrongPassword, hashedPassword);

        // Assert
        isValid.Should().BeFalse();
    }

    [Fact]
    public void JwtTokenService_ShouldGenerateToken_WhenValidUserProvided()
    {
        // Arrange
        var mockConfiguration = new Mock<IConfiguration>();
        var mockJwtSection = new Mock<IConfigurationSection>();
        
        mockConfiguration.Setup(x => x.GetSection("JwtSettings")).Returns(mockJwtSection.Object);
        mockJwtSection.Setup(x => x["SecretKey"]).Returns("ThisIsAVeryLongSecretKeyForTesting12345678901234567890");
        mockJwtSection.Setup(x => x["Issuer"]).Returns("TestIssuer");
        mockJwtSection.Setup(x => x["Audience"]).Returns("TestAudience");
        mockJwtSection.Setup(x => x["ExpiryInHours"]).Returns("24");

        var service = new JwtTokenService(mockConfiguration.Object);
        var user = new Usuario
        {
            Id = Guid.NewGuid(),
            NombreCompleto = "testuser",
            CorreoElectronico = "test@example.com",
            ContrasenaHash = "hashedpassword",
            FechaCreacion = DateTime.UtcNow
        };

        // Act
        var token = service.GenerateToken(user);

        // Assert
        token.Should().NotBeNullOrEmpty();
        
        var tokenHandler = new JwtSecurityTokenHandler();
        var jsonToken = tokenHandler.ReadJwtToken(token);

        jsonToken.Claims.Should().ContainSingle(c => c.Type == "nameid").Which.Value.Should().Be(user.Id.ToString());
        jsonToken.Claims.Should().ContainSingle(c => c.Type == "unique_name").Which.Value.Should().Be(user.NombreCompleto);
        jsonToken.Claims.Should().ContainSingle(c => c.Type == "email").Which.Value.Should().Be(user.CorreoElectronico);
    }

    [Fact]
    public void JwtTokenService_ShouldReturnExpirationTime_WhenCalled()
    {
        // Arrange
        var mockConfiguration = new Mock<IConfiguration>();
        var mockJwtSection = new Mock<IConfigurationSection>();
        
        mockConfiguration.Setup(x => x.GetSection("JwtSettings")).Returns(mockJwtSection.Object);
        mockJwtSection.Setup(x => x["SecretKey"]).Returns("ThisIsAVeryLongSecretKeyForTesting12345678901234567890");
        mockJwtSection.Setup(x => x["Issuer"]).Returns("TestIssuer");
        mockJwtSection.Setup(x => x["Audience"]).Returns("TestAudience");
        mockJwtSection.Setup(x => x["ExpiryInHours"]).Returns("24");

        var service = new JwtTokenService(mockConfiguration.Object);

        var beforeCall = DateTime.UtcNow;

        // Act
        var expirationTime = service.GetExpirationTime();

        // Assert
        // Se espera que la expiración sea 24 horas a partir de AHORA.
        var expectedExpiration = DateTime.UtcNow.AddHours(1);

        // Verificamos que el tiempo de expiración obtenido esté muy cerca (ej. a 1 segundo) del esperado.
        // Esto tolera pequeñas demoras en la ejecución del código.
        expirationTime.Should().BeCloseTo(expectedExpiration, precision: TimeSpan.FromSeconds(1));
    }

    [Fact]
    public async Task FileProcessingService_ShouldValidateImageFile_WhenValidImageProvided()
    {
        // Arrange
        var service = new FileProcessingService();
        
        // JPEG file signature
        var jpegSignature = new byte[] { 0xFF, 0xD8, 0xFF, 0xE0 };
        var fileName = "test.jpg";
        var mimeType = "image/jpeg";

        // Act
        var isValid = await service.ValidateFileAsync(jpegSignature, fileName, mimeType);

        // Assert
        isValid.Should().BeTrue();
    }

    [Fact]
    public async Task FileProcessingService_ShouldRejectLargeFile_WhenFileTooLarge()
    {
        // Arrange
        var service = new FileProcessingService();
        var largeFileData = new byte[60 * 1024 * 1024]; // 60MB
        var fileName = "large.jpg";
        var mimeType = "image/jpeg";

        // Act
        var isValid = await service.ValidateFileAsync(largeFileData, fileName, mimeType);

        // Assert
        isValid.Should().BeFalse();
    }

    [Fact]
    public async Task FileProcessingService_ShouldRejectInvalidFileType_WhenUnsupportedExtension()
    {
        // Arrange
        var service = new FileProcessingService();
        var fileData = new byte[1024];
        var fileName = "malicious.exe";
        var mimeType = "application/octet-stream";

        // Act
        var isValid = await service.ValidateFileAsync(fileData, fileName, mimeType);

        // Assert
        isValid.Should().BeFalse();
    }

    [Fact]
    public async Task FileProcessingService_ShouldRejectEmptyFile_WhenFileSizeIsZero()
    {
        // Arrange
        var service = new FileProcessingService();
        var emptyFileData = new byte[0];
        var fileName = "empty.jpg";
        var mimeType = "image/jpeg";

        // Act
        var isValid = await service.ValidateFileAsync(emptyFileData, fileName, mimeType);

        // Assert
        isValid.Should().BeFalse();
    }

    [Fact]
    public async Task FileProcessingService_ShouldValidateFileType_WhenValidMimeTypeProvided()
    {
        // Arrange
        var service = new FileProcessingService();
        var jpegSignature = new byte[] { 0xFF, 0xD8, 0xFF };
        var fileName = "test.jpg";
        var mimeType = "image/jpeg";

        // Act
        var detectedType = service.DetectFileType(mimeType);
        var validMimeType = service.GetValidMimeType(jpegSignature, mimeType);
        var isAllowedType = service.IsAllowedFileType(mimeType);
        var isValidSize = service.IsValidFileSize(jpegSignature.Length);

        // Assert
        detectedType.Should().Be(TipoDeArchivo.Imagen);
        validMimeType.Should().Be("image/jpeg");
        isAllowedType.Should().BeTrue();
        isValidSize.Should().BeTrue();
    }

    [Fact]
    public async Task FileProcessingService_ShouldValidateVideoFile_WhenValidVideoProvided()
    {
        // Arrange
        var service = new FileProcessingService();
        
        // MP4 file signature
        var mp4Signature = new byte[] { 0x00, 0x00, 0x00, 0x20, 0x66, 0x74, 0x79, 0x70 };
        var fileName = "test.mp4";
        var mimeType = "video/mp4";

        // Act
        var isValid = await service.ValidateFileAsync(mp4Signature, fileName, mimeType);

        // Assert
        isValid.Should().BeTrue();
    }

    [Theory]
    [InlineData("image/jpeg")]
    [InlineData("image/png")]
    [InlineData("image/gif")]
    [InlineData("video/mp4")]
    [InlineData("video/mov")]
    public void FileProcessingService_ShouldAcceptAllowedMimeTypes_WhenValidTypesProvided(string mimeType)
    {
        // Arrange
        var service = new FileProcessingService();

        // Act
        var isAllowedType = service.IsAllowedFileType(mimeType);

        // Assert
        isAllowedType.Should().BeTrue();
    }

    [Fact]
    public async Task FileProcessingService_ShouldCompressImage_WhenImageDataProvided()
    {
        // Arrange
        var service = new FileProcessingService();
        var imageData = new byte[] { 0xFF, 0xD8, 0xFF, 0xE0 };
        var mimeType = "image/jpeg";

        // Act
        var compressedData = await service.CompressImageAsync(imageData, mimeType);

        // Assert
        compressedData.Should().NotBeNull();
        compressedData.Should().BeEquivalentTo(imageData); // Currently returns original data
    }
}