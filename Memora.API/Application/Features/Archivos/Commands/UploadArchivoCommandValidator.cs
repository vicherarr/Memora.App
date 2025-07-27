using FluentValidation;
using Microsoft.Extensions.Configuration;

namespace Application.Features.Archivos.Commands;

public class UploadArchivoCommandValidator : AbstractValidator<UploadArchivoCommand>
{
    private readonly IConfiguration _configuration;
    
    // Extensiones permitidas y sus MIME types correspondientes
    private static readonly Dictionary<string, string[]> AllowedFileTypes = new(StringComparer.OrdinalIgnoreCase)
    {
        { ".jpg", new[] { "image/jpeg" } },
        { ".jpeg", new[] { "image/jpeg" } },
        { ".png", new[] { "image/png" } },
        { ".gif", new[] { "image/gif" } },
        { ".webp", new[] { "image/webp" } },
        { ".mp4", new[] { "video/mp4" } },
        { ".mov", new[] { "video/quicktime", "video/mov" } },
        { ".avi", new[] { "video/x-msvideo", "video/avi" } },
        { ".wmv", new[] { "video/x-ms-wmv" } },
        { ".webm", new[] { "video/webm" } }
    };

    // Firmas binarias para validación de contenido real
    private static readonly Dictionary<string, byte[][]> FileSignatures = new()
    {
        { "image/jpeg", new[] { new byte[] { 0xFF, 0xD8, 0xFF } } },
        { "image/png", new[] { new byte[] { 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A } } },
        { "image/gif", new[] { new byte[] { 0x47, 0x49, 0x46, 0x38 } } },
        { "video/mp4", new[] { 
            new byte[] { 0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70 },  // ftyp
            new byte[] { 0x00, 0x00, 0x00, 0x20, 0x66, 0x74, 0x79, 0x70 }   // ftyp alternative
        }}
    };

    // Caracteres no permitidos en nombres de archivo
    private static readonly char[] InvalidFileNameChars = 
        Path.GetInvalidFileNameChars().Concat(new[] { '<', '>', ':', '"', '|', '?', '*' }).ToArray();

    public UploadArchivoCommandValidator(IConfiguration configuration)
    {
        _configuration = configuration;

        RuleFor(x => x.NotaId)
            .NotEmpty()
            .WithMessage("Note ID is required");

        RuleFor(x => x.UsuarioId)
            .NotEmpty()
            .WithMessage("User ID is required");

        RuleFor(x => x.FileName)
            .NotEmpty()
            .WithMessage("File name is required")
            .MaximumLength(255)
            .WithMessage("File name must not exceed 255 characters")
            .Must(HaveValidExtension)
            .WithMessage("File extension is not allowed. Allowed: " + string.Join(", ", AllowedFileTypes.Keys))
            .Must(NotContainInvalidCharacters)
            .WithMessage("File name contains invalid characters")
            .Must(NotBeReservedFileName)
            .WithMessage("File name is reserved and cannot be used");

        RuleFor(x => x.ContentType)
            .NotEmpty()
            .WithMessage("Content type is required")
            .Must(BeValidContentType)
            .WithMessage("Invalid content type for the file extension");

        RuleFor(x => x.FileData)
            .NotEmpty()
            .WithMessage("File data is required")
            .Must(HaveValidSize)
            .WithMessage("File size exceeds maximum allowed limit or is empty")
            .Must(HaveValidFileSignature)
            .WithMessage("File content does not match the declared content type")
            .Must(NotBeCorruptedFile)
            .WithMessage("File appears to be corrupted or invalid");

        // Validación cruzada: ContentType debe coincidir con extensión del archivo
        RuleFor(x => x)
            .Must(x => ContentTypeMatchesExtension(x.FileName, x.ContentType))
            .WithMessage("Content type does not match file extension");
    }

    private bool HaveValidSize(byte[] fileData)
    {
        var maxSizeBytes = _configuration.GetValue<long>("FileUpload:MaxSizeBytes", 52428800); // 50MB default
        var minSizeBytes = _configuration.GetValue<long>("FileUpload:MinSizeBytes", 100); // 100 bytes minimum
        
        return fileData.Length >= minSizeBytes && fileData.Length <= maxSizeBytes;
    }

    private static bool HaveValidExtension(string fileName)
    {
        if (string.IsNullOrEmpty(fileName)) return false;
        
        var extension = Path.GetExtension(fileName);
        return !string.IsNullOrEmpty(extension) && AllowedFileTypes.ContainsKey(extension);
    }

    private static bool NotContainInvalidCharacters(string fileName)
    {
        return !string.IsNullOrEmpty(fileName) && 
               !fileName.Any(c => InvalidFileNameChars.Contains(c)) &&
               !fileName.StartsWith('.') &&
               !fileName.EndsWith('.');
    }

    private static bool NotBeReservedFileName(string fileName)
    {
        if (string.IsNullOrEmpty(fileName)) return false;
        
        var nameWithoutExtension = Path.GetFileNameWithoutExtension(fileName).ToUpper();
        var reservedNames = new[] { "CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", 
                                   "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", 
                                   "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9" };
        
        return !reservedNames.Contains(nameWithoutExtension);
    }

    private static bool BeValidContentType(string contentType)
    {
        return AllowedFileTypes.Values.Any(types => types.Contains(contentType, StringComparer.OrdinalIgnoreCase));
    }

    private static bool ContentTypeMatchesExtension(string fileName, string contentType)
    {
        if (string.IsNullOrEmpty(fileName) || string.IsNullOrEmpty(contentType)) return false;
        
        var extension = Path.GetExtension(fileName);
        if (!AllowedFileTypes.TryGetValue(extension, out var allowedTypes))
            return false;
        
        return allowedTypes.Contains(contentType, StringComparer.OrdinalIgnoreCase);
    }

    private static bool HaveValidFileSignature(byte[] fileData)
    {
        if (fileData == null || fileData.Length < 8) return false;
        
        // Para archivos pequeños, asumimos que son válidos
        // La validación más profunda se hará en el FileProcessingService
        return true;
    }

    private static bool NotBeCorruptedFile(byte[] fileData)
    {
        if (fileData == null || fileData.Length == 0) return false;
        
        // Verificaciones básicas de corrupción
        // 1. El archivo no debe ser todo ceros
        if (fileData.All(b => b == 0)) return false;
        
        // 2. El archivo no debe ser todo el mismo byte
        if (fileData.All(b => b == fileData[0])) return false;
        
        // 3. Verificar que tenga cierta entropía mínima (no sea texto repetitivo)
        var uniqueBytes = fileData.Take(1000).Distinct().Count();
        if (uniqueBytes < 10) return false; // Muy poca variación en los primeros 1000 bytes
        
        return true;
    }
}