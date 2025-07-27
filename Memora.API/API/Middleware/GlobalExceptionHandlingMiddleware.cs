using Application.Common.Exceptions;
using FluentValidation;
using Microsoft.AspNetCore.Mvc;
using System.Net;
using System.Text.Json;

namespace API.Middleware;

/// <summary>
/// Global exception handling middleware that converts exceptions to RFC 7807 Problem Details responses
/// </summary>
public class GlobalExceptionHandlingMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ILogger<GlobalExceptionHandlingMiddleware> _logger;
    private readonly IWebHostEnvironment _environment;

    public GlobalExceptionHandlingMiddleware(
        RequestDelegate next, 
        ILogger<GlobalExceptionHandlingMiddleware> logger,
        IWebHostEnvironment environment)
    {
        _next = next;
        _logger = logger;
        _environment = environment;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        try
        {
            await _next(context);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "An unhandled exception occurred. Request: {Method} {Path}", 
                context.Request.Method, context.Request.Path);
            
            await HandleExceptionAsync(context, ex);
        }
    }

    private async Task HandleExceptionAsync(HttpContext context, Exception exception)
    {
        var problemDetails = CreateProblemDetailsFromException(exception);
        
        context.Response.StatusCode = problemDetails.Status ?? (int)HttpStatusCode.InternalServerError;
        context.Response.ContentType = "application/problem+json";

        var options = new JsonSerializerOptions
        {
            PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
            WriteIndented = _environment.IsDevelopment()
        };

        var json = JsonSerializer.Serialize(problemDetails, options);
        await context.Response.WriteAsync(json);
    }

    private ProblemDetails CreateProblemDetailsFromException(Exception exception)
    {
        return exception switch
        {
            ValidationException validationEx => CreateValidationProblemDetails(validationEx),
            UnauthorizedAccessException unauthorizedEx => CreateProblemDetails(
                (int)HttpStatusCode.Unauthorized, 
                "Unauthorized", 
                unauthorizedEx.Message ?? "Authentication required",
                "https://tools.ietf.org/html/rfc7235#section-3.1"),
            
            UnauthorizedResourceAccessException resourceAccessEx => CreateProblemDetails(
                (int)HttpStatusCode.Forbidden, 
                "Forbidden", 
                resourceAccessEx.Message,
                "https://tools.ietf.org/html/rfc7231#section-6.5.3",
                new Dictionary<string, object>
                {
                    ["resourceType"] = resourceAccessEx.ResourceType,
                    ["resourceId"] = resourceAccessEx.ResourceId ?? "N/A",
                    ["userId"] = resourceAccessEx.UserId
                }),
            
            ResourceNotFoundException notFoundEx => CreateProblemDetails(
                (int)HttpStatusCode.NotFound, 
                "Resource Not Found", 
                notFoundEx.Message,
                "https://tools.ietf.org/html/rfc7231#section-6.5.4",
                new Dictionary<string, object>
                {
                    ["resourceType"] = notFoundEx.ResourceType,
                    ["resourceId"] = notFoundEx.ResourceId
                }),
            
            BusinessLogicException businessEx => CreateProblemDetails(
                (int)HttpStatusCode.BadRequest, 
                "Business Logic Error", 
                businessEx.Message,
                "https://tools.ietf.org/html/rfc7231#section-6.5.1",
                new Dictionary<string, object>
                {
                    ["errorCode"] = businessEx.Code,
                    ["additionalData"] = businessEx.AdditionalData ?? new Dictionary<string, object>()
                }),
            
            FileProcessingException fileEx => CreateProblemDetails(
                (int)HttpStatusCode.BadRequest, 
                "File Processing Error", 
                fileEx.Message,
                "https://tools.ietf.org/html/rfc7231#section-6.5.1",
                new Dictionary<string, object>
                {
                    ["fileName"] = fileEx.FileName,
                    ["operation"] = fileEx.Operation,
                    ["fileSize"] = fileEx.FileSize?.ToString() ?? "Unknown"
                }),
            
            ArgumentException argEx => CreateProblemDetails(
                (int)HttpStatusCode.BadRequest, 
                "Invalid Argument", 
                argEx.Message,
                "https://tools.ietf.org/html/rfc7231#section-6.5.1"),
            
            FileNotFoundException => CreateProblemDetails(
                (int)HttpStatusCode.NotFound, 
                "File Not Found", 
                "The requested file was not found",
                "https://tools.ietf.org/html/rfc7231#section-6.5.4"),
            
            NotSupportedException notSupportedEx => CreateProblemDetails(
                (int)HttpStatusCode.BadRequest, 
                "Not Supported", 
                notSupportedEx.Message,
                "https://tools.ietf.org/html/rfc7231#section-6.5.1"),
            
            _ => CreateProblemDetails(
                (int)HttpStatusCode.InternalServerError, 
                "Internal Server Error", 
                _environment.IsDevelopment() 
                    ? $"{exception.Message}\n\nStackTrace:\n{exception.StackTrace}" 
                    : "An internal server error occurred",
                "https://tools.ietf.org/html/rfc7231#section-6.6.1")
        };
    }

    private static ProblemDetails CreateValidationProblemDetails(ValidationException validationException)
    {
        var problemDetails = new ValidationProblemDetails
        {
            Type = "https://tools.ietf.org/html/rfc7231#section-6.5.1",
            Title = "Validation Error",
            Status = (int)HttpStatusCode.BadRequest,
            Detail = "One or more validation errors occurred"
        };

        foreach (var error in validationException.Errors)
        {
            var propertyName = error.PropertyName;
            if (!problemDetails.Errors.ContainsKey(propertyName))
            {
                problemDetails.Errors[propertyName] = new string[] { };
            }

            var existingErrors = problemDetails.Errors[propertyName].ToList();
            existingErrors.Add(error.ErrorMessage);
            problemDetails.Errors[propertyName] = existingErrors.ToArray();
        }

        return problemDetails;
    }

    private static ProblemDetails CreateProblemDetails(
        int status, 
        string title, 
        string detail, 
        string? type = null, 
        Dictionary<string, object>? extensions = null)
    {
        var problemDetails = new ProblemDetails
        {
            Status = status,
            Title = title,
            Detail = detail,
            Type = type ?? "https://tools.ietf.org/html/rfc7231"
        };

        if (extensions != null)
        {
            foreach (var extension in extensions)
            {
                problemDetails.Extensions[extension.Key] = extension.Value;
            }
        }

        return problemDetails;
    }
}