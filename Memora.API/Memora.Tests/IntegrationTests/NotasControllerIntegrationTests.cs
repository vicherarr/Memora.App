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

public class NotasControllerIntegrationTests : IClassFixture<TestWebApplicationFactory<Program>>
{
    private readonly TestWebApplicationFactory<Program> _factory;
    private readonly HttpClient _client;

    public NotasControllerIntegrationTests(TestWebApplicationFactory<Program> factory)
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
        
        // If registration fails for any reason, let's check the error
        if (!registerResponse.IsSuccessStatusCode)
        {
            var errorContent = await registerResponse.Content.ReadAsStringAsync();
            throw new Exception($"Registration failed with status {registerResponse.StatusCode}: {errorContent}");
        }

        var responseContent = await registerResponse.Content.ReadAsStringAsync();
        var result = JsonSerializer.Deserialize<JsonElement>(responseContent);
        return result.GetProperty("token").GetString()!;
    }

    [Fact]
    public async Task Register_ShouldReturnToken_WhenValidDataProvided()
    {
        // Arrange
        var uniqueId = Guid.NewGuid().ToString()[..8];
        var registerRequest = new
        {
            nombreCompleto = "Juan Pérez",
            correoElectronico = $"test_{uniqueId}@example.com",
            contrasena = "SecureP@ssw0rd2024!"
        };

        var registerJson = JsonSerializer.Serialize(registerRequest);
        var registerContent = new StringContent(registerJson, Encoding.UTF8, "application/json");

        // Act
        var registerResponse = await _client.PostAsync("/api/autenticacion/registrar", registerContent);

        // Assert
        if (!registerResponse.IsSuccessStatusCode)
        {
            var errorContent = await registerResponse.Content.ReadAsStringAsync();
            throw new Exception($"Registration failed with status {registerResponse.StatusCode}: {errorContent}");
        }

        registerResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var responseContent = await registerResponse.Content.ReadAsStringAsync();
        var result = JsonSerializer.Deserialize<JsonElement>(responseContent);
        result.GetProperty("token").GetString().Should().NotBeNullOrEmpty();
    }

    [Fact]
    public async Task CreateNota_ShouldReturnCreated_WhenValidDataProvided()
    {
        // Arrange
        var token = await GetJwtTokenAsync();
        _client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

        var notaRequest = new
        {
            titulo = "Test Nota",
            contenido = "Este es el contenido de prueba"
        };

        var json = JsonSerializer.Serialize(notaRequest);
        var content = new StringContent(json, Encoding.UTF8, "application/json");

        // Act
        var response = await _client.PostAsync("/api/notas", content);

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.Created);
        
        var responseContent = await response.Content.ReadAsStringAsync();
        var nota = JsonSerializer.Deserialize<JsonElement>(responseContent);
        
        nota.GetProperty("titulo").GetString().Should().Be("Test Nota");
        nota.GetProperty("contenido").GetString().Should().Be("Este es el contenido de prueba");
        nota.GetProperty("id").GetString().Should().NotBeNullOrEmpty();
    }

    [Fact]
    public async Task GetNotas_ShouldReturnOk_WhenUserHasNotas()
    {
        // Arrange
        var token = await GetJwtTokenAsync();
        _client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

        // Create a test nota first
        var notaRequest = new
        {
            titulo = "Test Nota for Get",
            contenido = "Contenido para obtener"
        };

        var json = JsonSerializer.Serialize(notaRequest);
        var content = new StringContent(json, Encoding.UTF8, "application/json");
        await _client.PostAsync("/api/notas", content);

        // Act
        var response = await _client.GetAsync("/api/notas?pageNumber=1&pageSize=10");

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.OK);
        
        var responseContent = await response.Content.ReadAsStringAsync();
        var result = JsonSerializer.Deserialize<JsonElement>(responseContent);
        
        result.GetProperty("notas").GetArrayLength().Should().BeGreaterThan(0);
        result.GetProperty("totalCount").GetInt32().Should().BeGreaterThan(0);
        result.GetProperty("pageNumber").GetInt32().Should().Be(1);
        result.GetProperty("pageSize").GetInt32().Should().Be(10);
    }

    [Fact]
    public async Task GetNotaById_ShouldReturnOk_WhenNotaExists()
    {
        // Arrange
        var token = await GetJwtTokenAsync();
        _client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

        // Create a test nota first
        var notaRequest = new
        {
            titulo = "Test Nota for GetById",
            contenido = "Contenido para obtener por ID"
        };

        var json = JsonSerializer.Serialize(notaRequest);
        var content = new StringContent(json, Encoding.UTF8, "application/json");
        var createResponse = await _client.PostAsync("/api/notas", content);
        
        var createResponseContent = await createResponse.Content.ReadAsStringAsync();
        var createdNota = JsonSerializer.Deserialize<JsonElement>(createResponseContent);
        var notaId = createdNota.GetProperty("id").GetString();

        // Act
        var response = await _client.GetAsync($"/api/notas/{notaId}");

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.OK);
        
        var responseContent = await response.Content.ReadAsStringAsync();
        var nota = JsonSerializer.Deserialize<JsonElement>(responseContent);
        
        nota.GetProperty("id").GetString().Should().Be(notaId);
        nota.GetProperty("titulo").GetString().Should().Be("Test Nota for GetById");
        nota.GetProperty("contenido").GetString().Should().Be("Contenido para obtener por ID");
    }

    [Fact]
    public async Task UpdateNota_ShouldReturnOk_WhenValidDataProvided()
    {
        // Arrange
        var token = await GetJwtTokenAsync();
        _client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

        // Create a test nota first
        var createRequest = new
        {
            titulo = "Original Title",
            contenido = "Original Content"
        };

        var createJson = JsonSerializer.Serialize(createRequest);
        var createContent = new StringContent(createJson, Encoding.UTF8, "application/json");
        var createResponse = await _client.PostAsync("/api/notas", createContent);
        
        var createResponseContent = await createResponse.Content.ReadAsStringAsync();
        var createdNota = JsonSerializer.Deserialize<JsonElement>(createResponseContent);
        var notaId = createdNota.GetProperty("id").GetString();

        // Prepare update request
        var updateRequest = new
        {
            titulo = "Updated Title",
            contenido = "Updated Content"
        };

        var updateJson = JsonSerializer.Serialize(updateRequest);
        var updateContent = new StringContent(updateJson, Encoding.UTF8, "application/json");

        // Act
        var response = await _client.PutAsync($"/api/notas/{notaId}", updateContent);

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.OK);
        
        var responseContent = await response.Content.ReadAsStringAsync();
        var updatedNota = JsonSerializer.Deserialize<JsonElement>(responseContent);
        
        updatedNota.GetProperty("id").GetString().Should().Be(notaId);
        updatedNota.GetProperty("titulo").GetString().Should().Be("Updated Title");
        updatedNota.GetProperty("contenido").GetString().Should().Be("Updated Content");
    }

    [Fact]
    public async Task DeleteNota_ShouldReturnNoContent_WhenNotaExists()
    {
        // Arrange
        var token = await GetJwtTokenAsync();
        _client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

        // Create a test nota first
        var notaRequest = new
        {
            titulo = "Nota to Delete",
            contenido = "This will be deleted"
        };

        var json = JsonSerializer.Serialize(notaRequest);
        var content = new StringContent(json, Encoding.UTF8, "application/json");
        var createResponse = await _client.PostAsync("/api/notas", content);
        
        var createResponseContent = await createResponse.Content.ReadAsStringAsync();
        var createdNota = JsonSerializer.Deserialize<JsonElement>(createResponseContent);
        var notaId = createdNota.GetProperty("id").GetString();

        // Act
        var response = await _client.DeleteAsync($"/api/notas/{notaId}");

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.NoContent);
        
        // Verify the nota was actually deleted
        var getResponse = await _client.GetAsync($"/api/notas/{notaId}");
        getResponse.StatusCode.Should().Be(HttpStatusCode.NotFound);
    }

    [Fact]
    public async Task GetNotas_ShouldReturnUnauthorized_WhenNoTokenProvided()
    {
        // Act
        var response = await _client.GetAsync("/api/notas");

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.Unauthorized);
    }
}