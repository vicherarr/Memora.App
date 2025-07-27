using MediatR;
using FluentValidation;
using FluentValidation.AspNetCore;
using AutoMapper;
using Application.Common.Interfaces;
using Infrastructure.Services;
using Application.Features.Authentication.Commands;
using Application.Common.Behaviours;

namespace Configuration.Extensions;

public static class ServiceCollectionExtensions
{
    public static IServiceCollection AddApplicationServices(this IServiceCollection services)
    {
        // Add MediatR
        services.AddMediatR(typeof(RegisterUserCommand).Assembly);

        // Add MediatR Pipeline Behaviors (order matters - they execute in registration order)
        services.AddTransient(typeof(IPipelineBehavior<,>), typeof(LoggingBehaviour<,>));
        services.AddTransient(typeof(IPipelineBehavior<,>), typeof(PerformanceBehaviour<,>));
        services.AddTransient(typeof(IPipelineBehavior<,>), typeof(ValidationBehaviour<,>));

        // Add AutoMapper
        services.AddAutoMapper(typeof(RegisterUserCommand).Assembly);

        // Add FluentValidation
        services.AddFluentValidationAutoValidation();
        services.AddValidatorsFromAssembly(typeof(RegisterUserCommand).Assembly);

        // Add Application Services
        services.AddScoped<IJwtTokenService, JwtTokenService>();
        services.AddScoped<IPasswordHashService, PasswordHashService>();
        services.AddScoped<IFileProcessingService, FileProcessingService>();

        services.AddControllers();
        services.AddEndpointsApiExplorer();

        return services;
    }
}