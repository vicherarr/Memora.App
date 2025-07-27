using System.Text;
using Application.Features.Authentication.Commands;
using MediatR;

namespace API.Middleware;

public class BasicAuthToJwtMiddleware
{
    private readonly RequestDelegate _next;
    private readonly IServiceProvider _serviceProvider;

    public BasicAuthToJwtMiddleware(RequestDelegate next, IServiceProvider serviceProvider)
    {
        _next = next;
        _serviceProvider = serviceProvider;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        // Skip middleware for authentication endpoints (register, login)
        var path = context.Request.Path.Value?.ToLower();
        if (path != null && (path.Contains("/autenticacion/registrar") || path.Contains("/autenticacion/login") || path.Contains("/autenticacion/swagger-auth")))
        {
            await _next(context);
            return;
        }

        // Only process requests that have Basic authentication for Swagger
        if (context.Request.Headers.ContainsKey("Authorization"))
        {
            var authHeader = context.Request.Headers["Authorization"].ToString();
            
            if (authHeader.StartsWith("Basic ", StringComparison.OrdinalIgnoreCase))
            {
                try
                {
                    // Extract credentials from Basic Auth header
                    var encodedCredentials = authHeader.Substring("Basic ".Length).Trim();
                    var decodedCredentials = Encoding.UTF8.GetString(Convert.FromBase64String(encodedCredentials));
                    var credentials = decodedCredentials.Split(':', 2);

                    if (credentials.Length == 2)
                    {
                        var email = credentials[0];
                        var password = credentials[1];

                        // Create a scope to get required services
                        using var scope = _serviceProvider.CreateScope();
                        var mediator = scope.ServiceProvider.GetRequiredService<IMediator>();

                        // Authenticate user and get JWT token
                        var loginCommand = new LoginUserCommand
                        {
                            CorreoElectronico = email,
                            Contrasena = password
                        };

                        try
                        {
                            var loginResult = await mediator.Send(loginCommand);

                            if (loginResult?.Token != null)
                            {
                                // Replace Authorization header with Bearer token
                                context.Request.Headers["Authorization"] = $"Bearer {loginResult.Token}";
                            }
                            else
                            {
                                // Authentication failed, return 401 immediately
                                context.Response.StatusCode = 401;
                                context.Response.Headers["WWW-Authenticate"] = "Basic realm=\"Memora API\"";
                                await context.Response.WriteAsync("{\"message\":\"Invalid email or password\"}");
                                return;
                            }
                        }
                        catch
                        {
                            // Authentication failed, return 401 immediately
                            context.Response.StatusCode = 401;
                            context.Response.Headers["WWW-Authenticate"] = "Basic realm=\"Memora API\"";
                            await context.Response.WriteAsync("{\"message\":\"Invalid email or password\"}");
                            return;
                        }
                    }
                }
                catch
                {
                    // If parsing fails, return 401 immediately
                    context.Response.StatusCode = 401;
                    context.Response.Headers["WWW-Authenticate"] = "Basic realm=\"Memora API\"";
                    await context.Response.WriteAsync("{\"message\":\"Invalid credentials format\"}");
                    return;
                }
            }
        }

        await _next(context);
    }
}