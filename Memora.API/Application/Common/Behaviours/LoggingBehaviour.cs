using MediatR;
using Microsoft.Extensions.Logging;
using System.Diagnostics;
using System.Reflection;
using System.Text.Json;

namespace Application.Common.Behaviours;

public class LoggingBehaviour<TRequest, TResponse> : IPipelineBehavior<TRequest, TResponse>
    where TRequest : IRequest<TResponse>
{
    private readonly ILogger<LoggingBehaviour<TRequest, TResponse>> _logger;

    public LoggingBehaviour(ILogger<LoggingBehaviour<TRequest, TResponse>> logger)
    {
        _logger = logger;
    }

    public async Task<TResponse> Handle(TRequest request, RequestHandlerDelegate<TResponse> next, CancellationToken cancellationToken)
    {
        var requestName = typeof(TRequest).Name;
        var requestId = Guid.NewGuid();
        
        _logger.LogInformation("Memora Request: {RequestId} {RequestName} {@Request}", 
            requestId, requestName, SanitizeRequest(request));

        var stopwatch = Stopwatch.StartNew();
        
        try
        {
            var response = await next();
            stopwatch.Stop();
            
            _logger.LogInformation("Memora Request Completed: {RequestId} {RequestName} - {ElapsedMs}ms", 
                requestId, requestName, stopwatch.ElapsedMilliseconds);
                
            return response;
        }
        catch (Exception ex)
        {
            stopwatch.Stop();
            _logger.LogError(ex, "Memora Request Failed: {RequestId} {RequestName} - {ElapsedMs}ms", 
                requestId, requestName, stopwatch.ElapsedMilliseconds);
            throw;
        }
    }

    private object SanitizeRequest(TRequest request)
    {
        try
        {
            // Crear una copia del objeto sin campos sensibles
            var requestType = typeof(TRequest);
            var sanitizedObject = new Dictionary<string, object?>();

            // Lista de campos sensibles que no deben loggearse
            var sensitiveFields = new HashSet<string>(StringComparer.OrdinalIgnoreCase)
            {
                "password", "contrasena", "token", "secret", "key", "authorization",
                "creditcard", "ssn", "taxid", "bankaccount"
            };

            foreach (var property in requestType.GetProperties(BindingFlags.Public | BindingFlags.Instance))
            {
                if (property.CanRead)
                {
                    var propertyName = property.Name;
                    var propertyValue = property.GetValue(request);

                    // Verificar si es un campo sensible
                    if (sensitiveFields.Any(field => propertyName.Contains(field, StringComparison.OrdinalIgnoreCase)))
                    {
                        sanitizedObject[propertyName] = "[REDACTED]";
                    }
                    // Verificar si es un array de bytes (archivo)
                    else if (propertyValue is byte[] byteArray)
                    {
                        sanitizedObject[propertyName] = $"[BINARY_DATA_{byteArray.Length}_BYTES]";
                    }
                    else
                    {
                        sanitizedObject[propertyName] = propertyValue;
                    }
                }
            }

            return sanitizedObject;
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Failed to sanitize request for logging");
            return "[SANITIZATION_FAILED]";
        }
    }
}