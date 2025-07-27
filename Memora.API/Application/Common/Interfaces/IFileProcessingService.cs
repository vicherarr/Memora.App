using Domain.Enums;

namespace Application.Common.Interfaces;

public interface IFileProcessingService
{
    Task<bool> ValidateFileAsync(byte[] fileData, string fileName, string mimeType);
    TipoDeArchivo DetectFileType(string mimeType);
    string GetValidMimeType(byte[] fileData, string originalMimeType);
    bool IsValidFileSize(long sizeInBytes);
    bool IsAllowedFileType(string mimeType);
    Task<byte[]> CompressImageAsync(byte[] imageData, string mimeType);
}

public class FileValidationResult
{
    public bool IsValid { get; set; }
    public string ErrorMessage { get; set; } = string.Empty;
    public TipoDeArchivo FileType { get; set; }
    public string ActualMimeType { get; set; } = string.Empty;
}