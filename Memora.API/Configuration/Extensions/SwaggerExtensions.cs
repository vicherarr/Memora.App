using Microsoft.OpenApi.Models;
using System.Reflection;
using Microsoft.Extensions.Options;
using Swashbuckle.AspNetCore.SwaggerGen;

namespace Configuration.Extensions;

public static class SwaggerExtensions
{
    public static IServiceCollection AddSwaggerConfiguration(this IServiceCollection services)
    {
        services.AddSwaggerGen(options =>
        {
            options.SwaggerDoc("v1", new OpenApiInfo
            {
                Title = "📱 Memora API",
                Version = "v1.0",
                Description = @"**API RESTful para la aplicación móvil Memora**

**🎯 Características principales:**
- 👤 Autenticación JWT segura
- 📝 Gestión completa de notas personales
- 📎 Archivos adjuntos (imágenes y videos)
- 🔍 Búsqueda por contenido
- 📊 Paginación eficiente
- 🛡️ Validación exhaustiva

**🔧 Tecnologías:**
- ASP.NET Core 8.0
- Entity Framework Core
- JWT Authentication
- MediatR + CQRS
- FluentValidation

**📖 Guía rápida:**
1. Regístrate o inicia sesión en `/api/autenticacion`
2. Usa el token JWT en el botón 🔐 Authorize
3. Explora los endpoints de notas y archivos

**⚠️ Importante:** Todos los endpoints (excepto autenticación) requieren token JWT válido.",
                Contact = new OpenApiContact
                {
                    Name = "Equipo Memora",
                    Email = "dev@memora.com",
                    Url = new Uri("https://github.com/memora/memora-api")
                },
                License = new OpenApiLicense
                {
                    Name = "MIT License",
                    Url = new Uri("https://opensource.org/licenses/MIT")
                }
            });

            // Include XML comments for comprehensive documentation
            var xmlFile = $"{Assembly.GetExecutingAssembly().GetName().Name}.xml";
            var xmlPath = Path.Combine(AppContext.BaseDirectory, xmlFile);
            if (File.Exists(xmlPath))
            {
                options.IncludeXmlComments(xmlPath, includeControllerXmlComments: true);
            }

            // Add custom schema filters for better examples
            options.SchemaFilter<ExampleSchemaFilter>();
            options.OperationFilter<AuthResponseExamplesFilter>();
            options.OperationFilter<FileUploadOperationFilter>();
            
            // Custom ordering of endpoints
            options.OrderActionsBy(apiDesc => $"{apiDesc.ActionDescriptor.RouteValues["controller"]}_{apiDesc.HttpMethod}");

            // Configure OAuth2 Password Flow for seamless authentication
            options.AddSecurityDefinition("OAuth2", new OpenApiSecurityScheme
            {
                Type = SecuritySchemeType.OAuth2,
                Flows = new OpenApiOAuthFlows
                {
                    Password = new OpenApiOAuthFlow
                    {
                        TokenUrl = new Uri("/api/autenticacion/swagger-auth", UriKind.Relative),
                        Scopes = new Dictionary<string, string>
                        {
                            {"api", "Acceso completo a la API Memora"}
                        }
                    }
                },
                Description = @"🔐 **Autenticación OAuth2 Password Flow**

**📋 GUÍA PASO A PASO:**

**1️⃣ Preparación:**
- Si no tienes cuenta: Usa `/api/autenticacion/registrar` primero
- Si ya tienes cuenta: Continúa al paso 2

**2️⃣ Autenticación rápida:**
- Haz clic en el botón **🔐 Authorize** arriba
- **Username:** Tu email (ej: `usuario@email.com`)
- **Password:** Tu contraseña
- **Client ID/Secret:** Déjalos VACÍOS
- Marca ✅ `api` scope
- Clic en **Authorize**

**3️⃣ Verificación:**
- ✅ Éxito: El candado se cierra automáticamente
- ❌ Error: Revisa credenciales y vuelve a intentar

**👥 Usuarios de prueba disponibles:**
```
📧 Email: test@test.com
🔑 Password: Test123456

📧 Email: dockertest@test.com  
🔑 Password: Test123456
```

**⏰ Token válido por:** 1 hora"
            });
            
            // Alternative Bearer token authentication for manual token input
            options.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
            {
                Type = SecuritySchemeType.Http,
                Scheme = "bearer",
                BearerFormat = "JWT",
                In = ParameterLocation.Header,
                Description = @"🎫 **Autenticación manual con token JWT**

**📝 Si ya tienes un token JWT:**
1. Obtén tu token desde `/api/autenticacion/login`
2. Copia el valor del campo `token`
3. Pégalo aquí (sin agregar 'Bearer ')
4. Haz clic en Authorize

**⚠️ Nota:** Es más fácil usar la opción OAuth2 de arriba."
            });

            // Security requirements with multiple options
            options.AddSecurityRequirement(new OpenApiSecurityRequirement
            {
                {
                    new OpenApiSecurityScheme
                    {
                        Reference = new OpenApiReference
                        {
                            Type = ReferenceType.SecurityScheme,
                            Id = "OAuth2"
                        }
                    },
                    new string[] { "api" }
                },
                {
                    new OpenApiSecurityScheme
                    {
                        Reference = new OpenApiReference
                        {
                            Type = ReferenceType.SecurityScheme,
                            Id = "Bearer"
                        }
                    },
                    Array.Empty<string>()
                }
            });
        });

        return services;
    }
}