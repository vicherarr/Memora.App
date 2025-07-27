using Application.Common.Interfaces;
using Domain.Enums;

namespace Infrastructure.Services;

public class FileProcessingService : IFileProcessingService
{
    private readonly string[] _allowedImageTypes = { "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp" };
    private readonly string[] _allowedVideoTypes = { "video/mp4", "video/mov", "video/avi", "video/wmv", "video/webm" };
    private const long MaxFileSize = 50 * 1024 * 1024; // 50MB

    private readonly Dictionary<string, byte[]> _fileSignatures = new()
    {
        { "image/jpeg", new byte[] { 0xFF, 0xD8, 0xFF } },
        { "image/png", new byte[] { 0x89, 0x50, 0x4E, 0x47 } },
        { "image/gif", new byte[] { 0x47, 0x49, 0x46 } },
        { "video/mp4", new byte[] { 0x00, 0x00, 0x00, 0x20, 0x66, 0x74, 0x79, 0x70 } }
    };

    public async Task<bool> ValidateFileAsync(byte[] fileData, string fileName, string mimeType)
    {
        // Validar tamaño
        if (!IsValidFileSize(fileData.Length))
            return false;

        // Validar tipo MIME
        if (!IsAllowedFileType(mimeType))
            return false;

        // Validar que el contenido coincida con el tipo MIME declarado
        var actualMimeType = GetValidMimeType(fileData, mimeType);
        if (actualMimeType != mimeType.ToLower())
            return false;

        return await Task.FromResult(true);
    }

    public TipoDeArchivo DetectFileType(string mimeType)
    {
        return mimeType.ToLower() switch
        {
            var type when _allowedImageTypes.Contains(type) => TipoDeArchivo.Imagen,
            var type when _allowedVideoTypes.Contains(type) => TipoDeArchivo.Video,
            _ => throw new ArgumentException($"Tipo de archivo no soportado: {mimeType}")
        };
    }

    public string GetValidMimeType(byte[] fileData, string originalMimeType)
    {
        if (fileData == null || fileData.Length == 0)
            return string.Empty;

        // Verificar signatures conocidas
        foreach (var signature in _fileSignatures)
        {
            if (fileData.Length >= signature.Value.Length)
            {
                var fileHeader = fileData.Take(signature.Value.Length).ToArray();
                if (signature.Value.SequenceEqual(fileHeader))
                {
                    return signature.Key;
                }
            }
        }

        // Detectar otros tipos basados en headers
        if (fileData.Length >= 4)
        {
            // WebP
            if (fileData[0] == 0x52 && fileData[1] == 0x49 && fileData[2] == 0x46 && fileData[3] == 0x46)
            {
                if (fileData.Length >= 12 && fileData[8] == 0x57 && fileData[9] == 0x45 && fileData[10] == 0x42 && fileData[11] == 0x50)
                    return "image/webp";
            }

            // AVI
            if (fileData[0] == 0x52 && fileData[1] == 0x49 && fileData[2] == 0x46 && fileData[3] == 0x46)
            {
                if (fileData.Length >= 11 && fileData[8] == 0x41 && fileData[9] == 0x56 && fileData[10] == 0x49)
                    return "video/avi";
            }
        }

        return originalMimeType.ToLower();
    }

    public bool IsValidFileSize(long sizeInBytes)
    {
        return sizeInBytes > 0 && sizeInBytes <= MaxFileSize;
    }

    public bool IsAllowedFileType(string mimeType)
    {
        var lowerMimeType = mimeType.ToLower();
        return _allowedImageTypes.Contains(lowerMimeType) || _allowedVideoTypes.Contains(lowerMimeType);
    }

    public async Task<byte[]> CompressImageAsync(byte[] imageData, string mimeType)
    {
        // Por ahora, retornamos los datos originales
        // En una implementación futura podríamos usar librerías como ImageSharp
        // para comprimir imágenes si exceden cierto tamaño
        return await Task.FromResult(imageData);
    }
}