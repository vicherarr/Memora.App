using Microsoft.OpenApi.Models;
using Swashbuckle.AspNetCore.SwaggerGen;
using Microsoft.OpenApi.Any;
using System.Text.Json;

namespace Configuration.Extensions;

/// <summary>
/// Filter to add comprehensive examples to API schemas
/// </summary>
public class ExampleSchemaFilter : ISchemaFilter
{
    public void Apply(OpenApiSchema schema, SchemaFilterContext context)
    {
        var type = context.Type;

        // Authentication DTOs examples
        if (type.Name == "RegisterUserDto")
        {
            schema.Example = new OpenApiString(@"{
  ""nombreCompleto"": ""María García López"",
  ""correoElectronico"": ""maria.garcia@email.com"",
  ""contrasena"": ""MiPassword123""
}");
        }
        else if (type.Name == "LoginUserDto")
        {
            schema.Example = new OpenApiString(@"{
  ""correoElectronico"": ""maria.garcia@email.com"",
  ""contrasena"": ""MiPassword123""
}");
        }
        // Notes DTOs examples
        else if (type.Name == "CreateNotaDto")
        {
            schema.Example = new OpenApiString(@"{
  ""titulo"": ""Lista de compras"",
  ""contenido"": ""- Leche desnatada\n- Pan integral\n- Frutas frescas\n- Yogur natural\n- Queso fresco""
}");
        }
        else if (type.Name == "UpdateNotaDto")
        {
            schema.Example = new OpenApiString(@"{
  ""titulo"": ""Lista de compras actualizada"",
  ""contenido"": ""- Leche desnatada ✓\n- Pan integral ✓\n- Frutas frescas\n- Yogur natural\n- Queso fresco\n- Aceite de oliva (nuevo)""
}");
        }
    }
}

/// <summary>
/// Filter to add authentication response examples
/// </summary>
public class AuthResponseExamplesFilter : IOperationFilter
{
    public void Apply(OpenApiOperation operation, OperationFilterContext context)
    {
        var actionName = context.MethodInfo.Name;

        if (actionName == "Register")
        {
            if (operation.Responses.ContainsKey("400"))
            {
                if (!operation.Responses["400"].Content.ContainsKey("application/json"))
                {
                    operation.Responses["400"].Content.Add("application/json", new OpenApiMediaType());
                }

                operation.Responses["400"].Content["application/json"].Examples = new Dictionary<string, OpenApiExample>
                {
                    ["email-already-exists"] = new OpenApiExample
                    {
                        Summary = "Email ya registrado",
                        Description = "El email proporcionado ya está en uso por otro usuario",
                        Value = new OpenApiString(@"{""message"": ""El email 'usuario@email.com' ya está registrado en el sistema""}")
                    },
                    ["weak-password"] = new OpenApiExample
                    {
                        Summary = "Contraseña débil",
                        Description = "La contraseña no cumple con los requisitos de seguridad",
                        Value = new OpenApiString(@"{""message"": ""La contraseña debe contener al menos 8 caracteres, una mayúscula, una minúscula y un número""}")
                    }
                };
            }
        }
        else if (actionName == "Login")
        {
            if (operation.Responses.ContainsKey("401"))
            {
                if (!operation.Responses["401"].Content.ContainsKey("application/json"))
                {
                    operation.Responses["401"].Content.Add("application/json", new OpenApiMediaType());
                }

                operation.Responses["401"].Content["application/json"].Examples = new Dictionary<string, OpenApiExample>
                {
                    ["invalid-credentials"] = new OpenApiExample
                    {
                        Summary = "Credenciales incorrectas",
                        Description = "Email o contraseña incorrectos",
                        Value = new OpenApiString(@"{""message"": ""Email o contraseña incorrectos""}")
                    }
                };
            }
        }
    }
}

/// <summary>
/// Filter to enhance file upload operations documentation
/// </summary>
public class FileUploadOperationFilter : IOperationFilter
{
    public void Apply(OpenApiOperation operation, OperationFilterContext context)
    {
        var actionName = context.MethodInfo.Name;

        if (actionName == "UploadArchivos")
        {
            operation.Summary = "📎 Subir archivos multimedia a una nota";
            operation.Description = @"**Endpoint especializado para subir archivos multimedia a notas específicas**

**🔧 Configuración técnica:**
- Content-Type: `multipart/form-data`
- Límite por archivo: 50MB
- Múltiples archivos soportados
- Validación exhaustiva de contenido

**📁 Formatos soportados:**
```
🖼️ Imágenes:
  • JPEG (.jpg, .jpeg)
  • PNG (.png)
  • GIF (.gif)
  • WebP (.webp)

🎥 Videos:
  • MP4 (.mp4)
  • MOV (.mov)
  • AVI (.avi)
  • WMV (.wmv)
  • WebM (.webm)
```

**🛡️ Validaciones aplicadas:**
- Verificación de MIME type vs extensión
- Análisis de headers binarios
- Detección de archivos corruptos
- Filtrado de nombres de archivo reservados
- Límites de tamaño por archivo

**💡 Ejemplo con cURL:**
```bash
curl -X POST 'https://api.memora.com/api/notas/{notaId}/archivos' \
  -H 'Authorization: Bearer {tu-jwt-token}' \
  -F 'files=@imagen1.jpg' \
  -F 'files=@video1.mp4'
```

**⚠️ Notas importantes:**
- Solo puedes subir archivos a tus propias notas
- Los archivos se almacenan como BLOB en base de datos
- El token JWT debe ser válido y no estar expirado";

            // Add custom parameter documentation
            if (operation.Parameters != null)
            {
                foreach (var param in operation.Parameters)
                {
                    if (param.Name == "notaId")
                    {
                        param.Description = "ID único de la nota donde adjuntar los archivos. Debe ser una nota que pertenezca al usuario autenticado.";
                        param.Example = new OpenApiString("7ba85f64-5717-4562-b3fc-2c963f66afa6");
                    }
                    else if (param.Name == "files")
                    {
                        param.Description = "Archivos a subir. Soporta múltiples archivos simultáneamente. Cada archivo será validado individualmente.";
                    }
                }
            }

            // Add response examples for successful uploads
            if (operation.Responses.ContainsKey("201"))
            {
                if (operation.Responses["201"].Content.ContainsKey("application/json"))
                {
                    operation.Responses["201"].Content["application/json"].Examples = new Dictionary<string, OpenApiExample>
                    {
                        ["single-image"] = new OpenApiExample
                        {
                            Summary = "Subida de imagen única",
                            Description = "Respuesta al subir una sola imagen",
                            Value = new OpenApiString(@"[{
  ""archivoId"": ""1fa85f64-5717-4562-b3fc-2c963f66afa6"",
  ""nombreOriginal"": ""paisaje_montañas.jpg"",
  ""tamanoBytes"": 2048576,
  ""tipoMime"": ""image/jpeg"",
  ""mensaje"": ""Archivo subido exitosamente""
}]")
                        },
                        ["multiple-files"] = new OpenApiExample
                        {
                            Summary = "Subida múltiple (imagen + video)",
                            Description = "Respuesta al subir varios archivos simultáneamente",
                            Value = new OpenApiString(@"[{
  ""archivoId"": ""1fa85f64-5717-4562-b3fc-2c963f66afa6"",
  ""nombreOriginal"": ""foto_familia.jpg"",
  ""tamanoBytes"": 1536000,
  ""tipoMime"": ""image/jpeg"",
  ""mensaje"": ""Archivo subido exitosamente""
}, {
  ""archivoId"": ""2fb85f64-5717-4562-b3fc-2c963f66afa6"",
  ""nombreOriginal"": ""video_cumpleanos.mp4"",
  ""tamanoBytes"": 25165824,
  ""tipoMime"": ""video/mp4"",
  ""mensaje"": ""Archivo subido exitosamente""
}]")
                        }
                    };
                }
            }

            // Add error response examples
            if (operation.Responses.ContainsKey("415"))
            {
                if (!operation.Responses["415"].Content.ContainsKey("application/json"))
                {
                    operation.Responses["415"].Content.Add("application/json", new OpenApiMediaType());
                }
                
                operation.Responses["415"].Content["application/json"].Examples = new Dictionary<string, OpenApiExample>
                {
                    ["unsupported-type"] = new OpenApiExample
                    {
                        Summary = "Tipo de archivo no soportado",
                        Description = "El archivo tiene un tipo MIME no permitido",
                        Value = new OpenApiString(@"{
  ""message"": ""Tipo de archivo no soportado: application/exe. Solo se permiten imágenes y videos."",
  ""allowedTypes"": [""image/jpeg"", ""image/png"", ""image/gif"", ""image/webp"", ""video/mp4"", ""video/mov"", ""video/avi"", ""video/wmv"", ""video/webm""]
}")
                    }
                };
            }

            if (operation.Responses.ContainsKey("413"))
            {
                if (!operation.Responses["413"].Content.ContainsKey("application/json"))
                {
                    operation.Responses["413"].Content.Add("application/json", new OpenApiMediaType());
                }
                
                operation.Responses["413"].Content["application/json"].Examples = new Dictionary<string, OpenApiExample>
                {
                    ["file-too-large"] = new OpenApiExample
                    {
                        Summary = "Archivo demasiado grande",
                        Description = "El archivo excede el límite de tamaño permitido",
                        Value = new OpenApiString(@"{
  ""message"": ""El archivo 'video_grande.mp4' (75.5 MB) excede el límite máximo de 50 MB"",
  ""maxSizeBytes"": 52428800,
  ""maxSizeMB"": 50
}")
                    }
                };
            }
        }
    }
}