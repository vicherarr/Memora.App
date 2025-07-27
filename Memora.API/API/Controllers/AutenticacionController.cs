using Application.Features.Authentication.Commands;
using Application.Features.Authentication.DTOs;
using MediatR;
using Microsoft.AspNetCore.Mvc;

namespace API.Controllers;

/// <summary>
/// Controlador para la autenticación de usuarios en la API Memora
/// </summary>
/// <remarks>
/// Este controlador maneja el registro de nuevos usuarios y el inicio de sesión.
/// Todos los endpoints devuelven tokens JWT para autenticación en endpoints protegidos.
/// </remarks>
[ApiController]
[Route("api/[controller]")]
[Produces("application/json")]
public class AutenticacionController : ControllerBase
{
    private readonly IMediator _mediator;

    public AutenticacionController(IMediator mediator)
    {
        _mediator = mediator;
    }

    /// <summary>
    /// Registra un nuevo usuario en el sistema
    /// </summary>
    /// <remarks>
    /// Crea una nueva cuenta de usuario con validación completa:
    /// 
    /// **Validaciones aplicadas:**
    /// - Email único en el sistema
    /// - Contraseña segura (8+ caracteres, mayúscula, minúscula, número)
    /// - Nombre completo válido (2-100 caracteres)
    /// - Bloqueo de emails desechables
    /// 
    /// **Ejemplo de request:**
    /// ```json
    /// {
    ///   "nombreCompleto": "Juan Pérez García",
    ///   "correoElectronico": "juan.perez@email.com",
    ///   "contrasena": "MiPassword123"
    /// }
    /// ```
    /// 
    /// **Respuesta exitosa:**
    /// Devuelve un token JWT válido por 1 hora que puede usarse inmediatamente.
    /// </remarks>
    /// <param name="request">Datos del nuevo usuario</param>
    /// <returns>Token JWT y información del usuario registrado</returns>
    /// <response code="200">Usuario registrado exitosamente</response>
    /// <response code="400">Datos de entrada inválidos o email ya existe</response>
    /// <response code="500">Error interno del servidor</response>
    [HttpPost("registrar")]
    [ProducesResponseType(typeof(RegisterResponseDto), 200)]
    [ProducesResponseType(typeof(object), 400)]
    [ProducesResponseType(typeof(object), 500)]
    public async Task<ActionResult<RegisterResponseDto>> Register(RegisterUserDto request)
    {
        try
        {
            var command = new RegisterUserCommand
            {
                NombreCompleto = request.NombreCompleto,
                CorreoElectronico = request.CorreoElectronico,
                Contrasena = request.Contrasena
            };

            var result = await _mediator.Send(command);
            return Ok(result);
        }
        catch (ArgumentException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Registration error: {ex.Message}");
            Console.WriteLine($"Stack trace: {ex.StackTrace}");
            return StatusCode(500, new { message = "An internal server error occurred", details = ex.Message });
        }
    }

    /// <summary>
    /// Inicia sesión de un usuario existente
    /// </summary>
    /// <remarks>
    /// Autentica las credenciales del usuario y devuelve un token JWT:
    /// 
    /// **Ejemplo de request:**
    /// ```json
    /// {
    ///   "correoElectronico": "juan.perez@email.com",
    ///   "contrasena": "MiPassword123"
    /// }
    /// ```
    /// 
    /// **Respuesta exitosa:**
    /// ```json
    /// {
    ///   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    ///   "usuario": {
    ///     "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    ///     "nombreCompleto": "Juan Pérez García",
    ///     "correoElectronico": "juan.perez@email.com"
    ///   }
    /// }
    /// ```
    /// 
    /// **Uso del token:**
    /// Incluye el token en el header Authorization: `Bearer {token}`
    /// </remarks>
    /// <param name="request">Credenciales de acceso</param>
    /// <returns>Token JWT y información del usuario</returns>
    /// <response code="200">Inicio de sesión exitoso</response>
    /// <response code="401">Credenciales inválidas</response>
    /// <response code="500">Error interno del servidor</response>
    [HttpPost("login")]
    [ProducesResponseType(typeof(LoginResponseDto), 200)]
    [ProducesResponseType(typeof(object), 401)]
    [ProducesResponseType(typeof(object), 500)]
    public async Task<ActionResult<LoginResponseDto>> Login(LoginUserDto request)
    {
        try
        {
            var command = new LoginUserCommand
            {
                CorreoElectronico = request.CorreoElectronico,
                Contrasena = request.Contrasena
            };

            var result = await _mediator.Send(command);
            return Ok(result);
        }
        catch (UnauthorizedAccessException ex)
        {
            return Unauthorized(new { message = ex.Message });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "An internal server error occurred" });
        }
    }

    /// <summary>
    /// 🔐 Endpoint de autenticación OAuth2 para Swagger UI
    /// </summary>
    /// <remarks>
    /// **GUÍA PASO A PASO PARA AUTENTICARTE EN SWAGGER:**
    /// 
    /// **Opción 1 - Botón Authorize (Recomendado):**
    /// 1. Haz clic en el botón "🔓 Authorize" en la parte superior
    /// 2. En "OAuth2 (OAuth2, password)" introduce:
    ///    - **username:** tu email (ej: test@test.com)
    ///    - **password:** tu contraseña (ej: Test123456)
    /// 3. Haz clic en "Authorize"
    /// 4. ¡Listo! Ya puedes usar todos los endpoints protegidos
    /// 
    /// **Opción 2 - Endpoint manual:**
    /// 1. Usa este endpoint con tus credenciales
    /// 2. Copia el `access_token` de la respuesta
    /// 3. Haz clic en "🔓 Authorize" arriba
    /// 4. En "Bearer (http, Bearer)" pega el token
    /// 5. Haz clic en "Authorize"
    /// 
    /// **🧪 Usuarios de prueba disponibles:**
    /// - Email: `test@test.com` / Contraseña: `Test123456`
    /// - Email: `dockertest@test.com` / Contraseña: `Test123456`
    /// 
    /// **⚠️ Nota importante:**
    /// Los tokens JWT expiran en 1 hora. Si obtienes errores 401, vuelve a autenticarte.
    /// </remarks>
    /// <param name="username">Email del usuario</param>
    /// <param name="password">Contraseña del usuario</param>
    /// <param name="grant_type">Tipo de autorización OAuth2 (debe ser 'password')</param>
    /// <returns>Token de acceso OAuth2 estándar</returns>
    /// <response code="200">Autenticación exitosa - token generado</response>
    /// <response code="400">Credenciales inválidas o tipo de grant no soportado</response>
    /// <response code="500">Error interno del servidor</response>
    [HttpPost("swagger-auth")]
    [ProducesResponseType(typeof(object), 200)]
    [ProducesResponseType(typeof(object), 400)]
    [ProducesResponseType(typeof(object), 500)]
    public async Task<ActionResult> SwaggerAuth([FromForm] string username, [FromForm] string password, [FromForm] string grant_type = "password")
    {
        try
        {
            if (grant_type != "password")
            {
                return BadRequest(new { error = "unsupported_grant_type" });
            }

            var command = new LoginUserCommand
            {
                CorreoElectronico = username, // OAuth2 usa 'username' pero nosotros esperamos email
                Contrasena = password
            };

            var result = await _mediator.Send(command);
            
            // Respuesta en formato OAuth2 Token Response
            return Ok(new 
            { 
                access_token = result.Token,
                token_type = "Bearer",
                expires_in = 3600 // 1 hora en segundos
            });
        }
        catch (UnauthorizedAccessException)
        {
            return BadRequest(new { 
                error = "invalid_grant",
                error_description = "Invalid email or password" 
            });
        }
        catch (Exception)
        {
            return StatusCode(500, new { 
                error = "server_error",
                error_description = "An internal server error occurred" 
            });
        }
    }
}