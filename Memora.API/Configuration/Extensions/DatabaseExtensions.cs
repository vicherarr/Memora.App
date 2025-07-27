using Microsoft.EntityFrameworkCore;
using Infrastructure.Data;

namespace Configuration.Extensions;

public static class DatabaseExtensions
{
    public static IServiceCollection AddDatabaseConfiguration(this IServiceCollection services, 
        IConfiguration configuration, IWebHostEnvironment environment)
    {
        // Add Entity Framework DbContext
        if (environment.EnvironmentName == "Testing")
        {
            var connectionString = "Data Source=TestMemoria.db;Cache=Shared";
            services.AddDbContext<MemoraDbContext>(options =>
                options.UseSqlite(connectionString));
        }
        else
        {
            services.AddDbContext<MemoraDbContext>(options =>
                options.UseSqlite(configuration.GetConnectionString("DefaultConnection")));
        }

        return services;
    }

    public static WebApplication EnsureDatabaseCreated(this WebApplication app)
    {
        // Initialize database for testing environment
        if (app.Environment.EnvironmentName == "Testing")
        {
            using (var scope = app.Services.CreateScope())
            {
                var context = scope.ServiceProvider.GetRequiredService<MemoraDbContext>();
                context.Database.EnsureCreated();
            }
        }

        return app;
    }
}