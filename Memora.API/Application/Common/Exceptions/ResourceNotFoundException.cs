namespace Application.Common.Exceptions;

/// <summary>
/// Exception thrown when a requested resource is not found
/// </summary>
public class ResourceNotFoundException : Exception
{
    public string ResourceType { get; }
    public string ResourceId { get; }

    public ResourceNotFoundException(string resourceType, string resourceId) 
        : base($"{resourceType} with ID '{resourceId}' was not found")
    {
        ResourceType = resourceType;
        ResourceId = resourceId;
    }

    public ResourceNotFoundException(string resourceType, string resourceId, string message) 
        : base(message)
    {
        ResourceType = resourceType;
        ResourceId = resourceId;
    }

    public ResourceNotFoundException(string resourceType, string resourceId, Exception innerException) 
        : base($"{resourceType} with ID '{resourceId}' was not found", innerException)
    {
        ResourceType = resourceType;
        ResourceId = resourceId;
    }
}