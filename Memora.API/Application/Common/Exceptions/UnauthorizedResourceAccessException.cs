namespace Application.Common.Exceptions;

/// <summary>
/// Exception thrown when a user attempts to access a resource they don't have permission for
/// </summary>
public class UnauthorizedResourceAccessException : Exception
{
    public string ResourceType { get; }
    public string? ResourceId { get; }
    public string UserId { get; }

    public UnauthorizedResourceAccessException(string message) : base(message) 
    {
        ResourceType = "Unknown";
        UserId = "Unknown";
    }

    public UnauthorizedResourceAccessException(string resourceType, string? resourceId, string userId) 
        : base($"User '{userId}' is not authorized to access {resourceType}" + 
               (resourceId != null ? $" with ID '{resourceId}'" : ""))
    {
        ResourceType = resourceType;
        ResourceId = resourceId;
        UserId = userId;
    }

    public UnauthorizedResourceAccessException(string resourceType, string? resourceId, string userId, string message) 
        : base(message)
    {
        ResourceType = resourceType;
        ResourceId = resourceId;
        UserId = userId;
    }

    public UnauthorizedResourceAccessException(string message, Exception innerException) : base(message, innerException)
    {
        ResourceType = "Unknown";
        UserId = "Unknown";
    }
}