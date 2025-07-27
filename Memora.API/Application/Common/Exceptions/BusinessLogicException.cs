namespace Application.Common.Exceptions;

/// <summary>
/// Exception for business logic violations that should be handled gracefully
/// </summary>
public class BusinessLogicException : Exception
{
    public string Code { get; }
    public Dictionary<string, object>? AdditionalData { get; }

    public BusinessLogicException(string code, string message) : base(message)
    {
        Code = code;
    }

    public BusinessLogicException(string code, string message, Dictionary<string, object> additionalData) : base(message)
    {
        Code = code;
        AdditionalData = additionalData;
    }

    public BusinessLogicException(string code, string message, Exception innerException) : base(message, innerException)
    {
        Code = code;
    }

    public BusinessLogicException(string code, string message, Dictionary<string, object> additionalData, Exception innerException) 
        : base(message, innerException)
    {
        Code = code;
        AdditionalData = additionalData;
    }
}