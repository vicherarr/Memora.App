using MediatR;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using System.Diagnostics;

namespace Application.Common.Behaviours;

public class PerformanceBehaviour<TRequest, TResponse> : IPipelineBehavior<TRequest, TResponse>
    where TRequest : IRequest<TResponse>
{
    private readonly ILogger<PerformanceBehaviour<TRequest, TResponse>> _logger;
    private readonly IConfiguration _configuration;

    public PerformanceBehaviour(ILogger<PerformanceBehaviour<TRequest, TResponse>> logger, IConfiguration configuration)
    {
        _logger = logger;
        _configuration = configuration;
    }

    public async Task<TResponse> Handle(TRequest request, RequestHandlerDelegate<TResponse> next, CancellationToken cancellationToken)
    {
        var stopwatch = Stopwatch.StartNew();
        var requestName = typeof(TRequest).Name;
        
        var response = await next();
        
        stopwatch.Stop();
        var elapsedMs = stopwatch.ElapsedMilliseconds;
        
        // Obtener umbrales de configuración con valores por defecto
        var warningThreshold = _configuration.GetValue<int>("Performance:WarningThresholdMs", 500);
        var criticalThreshold = _configuration.GetValue<int>("Performance:CriticalThresholdMs", 2000);
        var infoThreshold = _configuration.GetValue<int>("Performance:InfoThresholdMs", 100);

        // Loggear según el umbral correspondiente
        if (elapsedMs > criticalThreshold)
        {
            _logger.LogWarning("🔥 CRITICAL PERFORMANCE: {RequestName} took {ElapsedMs}ms (Critical Threshold: {CriticalThreshold}ms)", 
                requestName, elapsedMs, criticalThreshold);
        }
        else if (elapsedMs > warningThreshold)
        {
            _logger.LogWarning("⚠️ SLOW PERFORMANCE: {RequestName} took {ElapsedMs}ms (Warning Threshold: {WarningThreshold}ms)", 
                requestName, elapsedMs, warningThreshold);
        }
        else if (elapsedMs > infoThreshold)
        {
            _logger.LogInformation("📊 PERFORMANCE INFO: {RequestName} took {ElapsedMs}ms", 
                requestName, elapsedMs);
        }
        else
        {
            _logger.LogDebug("✅ FAST PERFORMANCE: {RequestName} took {ElapsedMs}ms", 
                requestName, elapsedMs);
        }

        // Agregar métricas adicionales para análisis
        using var activity = Activity.Current;
        activity?.SetTag("request.name", requestName);
        activity?.SetTag("request.duration_ms", elapsedMs);
        activity?.SetTag("request.performance_category", GetPerformanceCategory(elapsedMs, warningThreshold, criticalThreshold));

        return response;
    }

    private static string GetPerformanceCategory(long elapsedMs, int warningThreshold, int criticalThreshold)
    {
        if (elapsedMs > criticalThreshold) return "critical";
        if (elapsedMs > warningThreshold) return "slow";
        return "normal";
    }
}