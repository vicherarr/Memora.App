using Configuration.Extensions;

var builder = WebApplication.CreateBuilder(args);

// Add services using extension methods
builder.Services.AddApplicationServices();
builder.Services.AddDatabaseConfiguration(builder.Configuration, builder.Environment);
builder.Services.AddJwtAuthentication(builder.Configuration);

// Add Swagger in Development and Docker environments
if (builder.Environment.IsDevelopment() || Environment.GetEnvironmentVariable("ASPNETCORE_ENVIRONMENT") == "Docker")
{
    builder.Services.AddSwaggerConfiguration();
}

var app = builder.Build();

// Configure middleware pipeline and ensure database is ready
app.EnsureDatabaseCreated()
   .ConfigureMiddlewarePipeline();

app.Run();

// Make Program class accessible for integration tests
public partial class Program { }
