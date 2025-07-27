namespace Application.Common.Exceptions;

/// <summary>
/// Exception thrown when file processing operations fail
/// </summary>
public class FileProcessingException : Exception
{
    public string FileName { get; }
    public string Operation { get; }
    public long? FileSize { get; }

    public FileProcessingException(string fileName, string operation, string message) 
        : base(message)
    {
        FileName = fileName;
        Operation = operation;
    }

    public FileProcessingException(string fileName, string operation, string message, long fileSize) 
        : base(message)
    {
        FileName = fileName;
        Operation = operation;
        FileSize = fileSize;
    }

    public FileProcessingException(string fileName, string operation, string message, Exception innerException) 
        : base(message, innerException)
    {
        FileName = fileName;
        Operation = operation;
    }

    public FileProcessingException(string fileName, string operation, string message, long fileSize, Exception innerException) 
        : base(message, innerException)
    {
        FileName = fileName;
        Operation = operation;
        FileSize = fileSize;
    }
}