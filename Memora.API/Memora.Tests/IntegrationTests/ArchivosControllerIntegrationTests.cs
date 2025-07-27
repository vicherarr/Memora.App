
using System.Net;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using Application.Features.Notas.DTOs;
using Domain.Entities;
using FluentAssertions;
using Infrastructure.Data;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Xunit;

namespace Memora.Tests.IntegrationTests;

public class ArchivosControllerIntegrationTests : IClassFixture<TestWebApplicationFactory<Program>>
{
    private readonly TestWebApplicationFactory<Program> _factory;
    private readonly HttpClient _client;

    public ArchivosControllerIntegrationTests(TestWebApplicationFactory<Program> factory)
    {
        _factory = factory;
        _client = _factory.CreateClient();
    }

    private async Task<string> GetJwtTokenAsync()
    {
        // Create unique user for each test
        var uniqueId = Guid.NewGuid().ToString()[..8];
        var registerRequest = new
        {
            nombreCompleto = "Juan Pérez",
            correoElectronico = $"test_{uniqueId}@example.com",
            contrasena = "SecureP@ssw0rd2024!"
        };

        var registerJson = JsonSerializer.Serialize(registerRequest);
        var registerContent = new StringContent(registerJson, Encoding.UTF8, "application/json");

        var registerResponse = await _client.PostAsync("/api/autenticacion/registrar", registerContent);
        
        if (!registerResponse.IsSuccessStatusCode)
        {
            var errorContent = await registerResponse.Content.ReadAsStringAsync();
            throw new Exception($"Registration failed with status {registerResponse.StatusCode}: {errorContent}");
        }

        var responseContent = await registerResponse.Content.ReadAsStringAsync();
        var result = JsonSerializer.Deserialize<JsonElement>(responseContent);
        return result.GetProperty("token").GetString()!;
    }

    private async Task<string> CreateTestNotaAsync(string token)
    {
        _client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);
        var notaRequest = new
        {
            titulo = "Test Nota for Archivo",
            contenido = "Contenido de prueba para archivo"
        };

        var json = JsonSerializer.Serialize(notaRequest);
        var content = new StringContent(json, Encoding.UTF8, "application/json");
        var response = await _client.PostAsync("/api/notas", content);
        response.EnsureSuccessStatusCode();
        
        var responseContent = await response.Content.ReadAsStringAsync();
        var nota = JsonSerializer.Deserialize<JsonElement>(responseContent);
        return nota.GetProperty("id").GetString()!;
    }

    private byte[] CreateTestImage() => new byte[] { 0xFF, 0xD8, 0xFF, 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01, 0x01, 0x01, 0x00, 0x48, 0x00, 0x48, 0x00, 0x00, 0xFF, 0xDB, 0x00, 0x43, 0x00, 0x03, 0x02, 0x02, 0x02, 0x02, 0x02, 0x03, 0x02, 0x02, 0x02, 0x03, 0x03, 0x03, 0x03, 0x04, 0x06, 0x04, 0x04, 0x04, 0x04, 0x04, 0x08, 0x06, 0x06, 0x05, 0x06, 0x09, 0x08, 0x0A, 0x0A, 0x09, 0x08, 0x09, 0x09, 0x0A, 0x0C, 0x0F, 0x0C, 0x0A, 0x0B, 0x0E, 0x0B, 0x09, 0x09, 0x0D, 0x11, 0x0D, 0x0E, 0x0F, 0x10, 0x10, 0x11, 0x10, 0x0A, 0x0C, 0x12, 0x13, 0x12, 0x10, 0x13, 0x0F, 0x10, 0x10, 0x10, 0xFF, 0xC9, 0x00, 0x0B, 0x08, 0x00, 0x01, 0x00, 0x01, 0x01, 0x01, 0x11, 0x00, 0xFF, 0xCC, 0x00, 0x06, 0x00, 0x10, 0x10, 0x05, 0xFF, 0xDA, 0x00, 0x08, 0x01, 0x01, 0x00, 0x00, 0x3F, 0x00, 0xD2, 0xCF, 0x20, 0xFF, 0xD9 };

    [Fact]
    public async Task UploadArchivo_ShouldReturnCreated_WhenValidFileProvided()
    {
        // Arrange
        var token = await GetJwtTokenAsync();
        var notaId = await CreateTestNotaAsync(token);
        _client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

        var multipartContent = new MultipartFormDataContent();
        var fileContent = new ByteArrayContent(CreateTestImage());
        fileContent.Headers.ContentType = new MediaTypeHeaderValue("image/jpeg");
        multipartContent.Add(fileContent, "files", "test.jpg");

        // Act
        var response = await _client.PostAsync($"/api/notas/{notaId}/archivos", multipartContent);

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.Created);
        var responseContent = await response.Content.ReadAsStringAsync();
        var result = JsonSerializer.Deserialize<List<JsonElement>>(responseContent);
        result.Should().NotBeNull();
        result.Should().HaveCount(1);
        result[0].GetProperty("nombreOriginal").GetString().Should().Be("test.jpg");
    }

    [Fact]
    public async Task GetArchivo_ShouldReturnOk_WhenArchivoExists()
    {
        // Arrange
        var token = await GetJwtTokenAsync();
        var notaId = await CreateTestNotaAsync(token);
        _client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

        var multipartContent = new MultipartFormDataContent();
        var fileContent = new ByteArrayContent(CreateTestImage());
        fileContent.Headers.ContentType = new MediaTypeHeaderValue("image/jpeg");
        multipartContent.Add(fileContent, "files", "test.jpg");

        var uploadResponse = await _client.PostAsync($"/api/notas/{notaId}/archivos", multipartContent);
        var uploadContent = await uploadResponse.Content.ReadAsStringAsync();
        var uploadedArchivos = JsonSerializer.Deserialize<List<JsonElement>>(uploadContent);
        var archivoId = uploadedArchivos[0].GetProperty("archivoId").GetString();

        // Act
        var response = await _client.GetAsync($"/api/archivos/{archivoId}");

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.OK);
        var responseContent = await response.Content.ReadAsStringAsync();
        var result = JsonSerializer.Deserialize<JsonElement>(responseContent);
        result.GetProperty("nombreOriginal").GetString().Should().Be("test.jpg");
    }

    [Fact]
    public async Task DownloadArchivo_ShouldReturnOk_WhenArchivoExists()
    {
        // Arrange
        var token = await GetJwtTokenAsync();
        var notaId = await CreateTestNotaAsync(token);
        _client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

        var multipartContent = new MultipartFormDataContent();
        var fileContent = new ByteArrayContent(CreateTestImage());
        fileContent.Headers.ContentType = new MediaTypeHeaderValue("image/jpeg");
        multipartContent.Add(fileContent, "files", "test.jpg");

        var uploadResponse = await _client.PostAsync($"/api/notas/{notaId}/archivos", multipartContent);
        var uploadContent = await uploadResponse.Content.ReadAsStringAsync();
        var uploadedArchivos = JsonSerializer.Deserialize<List<JsonElement>>(uploadContent);
        var archivoId = uploadedArchivos[0].GetProperty("archivoId").GetString();

        // Act
        var response = await _client.GetAsync($"/api/archivos/{archivoId}/download");

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.OK);
        response.Content.Headers.ContentType?.ToString().Should().Be("image/jpeg");
        var downloadedContent = await response.Content.ReadAsByteArrayAsync();
        downloadedContent.Should().BeEquivalentTo(CreateTestImage());
    }

    [Fact]
    public async Task DeleteArchivo_ShouldReturnNoContent_WhenArchivoExists()
    {
        // Arrange
        var token = await GetJwtTokenAsync();
        var notaId = await CreateTestNotaAsync(token);
        _client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

        var multipartContent = new MultipartFormDataContent();
        var fileContent = new ByteArrayContent(CreateTestImage());
        fileContent.Headers.ContentType = new MediaTypeHeaderValue("image/jpeg");
        multipartContent.Add(fileContent, "files", "test.jpg");

        var uploadResponse = await _client.PostAsync($"/api/notas/{notaId}/archivos", multipartContent);
        var uploadContent = await uploadResponse.Content.ReadAsStringAsync();
        var uploadedArchivos = JsonSerializer.Deserialize<List<JsonElement>>(uploadContent);
        var archivoId = uploadedArchivos[0].GetProperty("archivoId").GetString();

        // Act
        var response = await _client.DeleteAsync($"/api/archivos/{archivoId}");

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.NoContent);

        // Verify the archivo was actually deleted
        var getResponse = await _client.GetAsync($"/api/archivos/{archivoId}");
        getResponse.StatusCode.Should().Be(HttpStatusCode.NotFound);
    }
}
