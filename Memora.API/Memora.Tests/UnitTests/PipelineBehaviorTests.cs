using Application.Common.Behaviours;
using FluentValidation;
using FluentValidation.Results;
using MediatR;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using Moq;
using Xunit;

namespace Memora.Tests.UnitTests;

public class PipelineBehaviorTests
{
    [Fact]
    public async Task LoggingBehaviour_ShouldLogRequestStart_WhenHandlingRequest()
    {
        // Arrange
        var mockLogger = new Mock<ILogger<LoggingBehaviour<TestRequest, TestResponse>>>();
        var behaviour = new LoggingBehaviour<TestRequest, TestResponse>(mockLogger.Object);
        var request = new TestRequest { Message = "Test" };
        var next = new Mock<RequestHandlerDelegate<TestResponse>>();
        next.Setup(x => x()).ReturnsAsync(new TestResponse { Result = "Success" });

        // Act
        await behaviour.Handle(request, next.Object, CancellationToken.None);

        // Assert
        mockLogger.Verify(
            x => x.Log(
                LogLevel.Information,
                It.IsAny<EventId>(),
                It.Is<It.IsAnyType>((v, t) => v.ToString()!.Contains("Memora Request:")),
                It.IsAny<Exception>(),
                It.IsAny<Func<It.IsAnyType, Exception?, string>>()),
            Times.AtLeastOnce);
    }

    [Fact]
    public async Task PerformanceBehaviour_ShouldLogWarning_WhenRequestIsSlow()
    {
        // Arrange
        var mockLogger = new Mock<ILogger<PerformanceBehaviour<TestRequest, TestResponse>>>();
        
        // Create a real configuration for this test
        var configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.AddInMemoryCollection(new Dictionary<string, string?>
        {
            {"Performance:WarningThresholdMs", "1"}, // Very low threshold
            {"Performance:CriticalThresholdMs", "5000"},
            {"Performance:InfoThresholdMs", "0"}
        });
        var configuration = configurationBuilder.Build();
        
        var behaviour = new PerformanceBehaviour<TestRequest, TestResponse>(mockLogger.Object, configuration);
        var request = new TestRequest { Message = "Test" };
        var next = new Mock<RequestHandlerDelegate<TestResponse>>();
        next.Setup(x => x()).Returns(async () =>
        {
            await Task.Delay(10); // Delay to trigger warning
            return new TestResponse { Result = "Success" };
        });

        // Act
        await behaviour.Handle(request, next.Object, CancellationToken.None);

        // Assert
        mockLogger.Verify(
            x => x.Log(
                LogLevel.Warning,
                It.IsAny<EventId>(),
                It.Is<It.IsAnyType>((v, t) => v.ToString()!.Contains("SLOW PERFORMANCE")),
                It.IsAny<Exception>(),
                It.IsAny<Func<It.IsAnyType, Exception?, string>>()),
            Times.Once);
    }

    [Fact]
    public async Task ValidationBehaviour_ShouldThrowValidationException_WhenValidationFails()
    {
        // Arrange
        var mockValidator = new Mock<IValidator<TestRequest>>();
        var validationResult = new ValidationResult(new[]
        {
            new ValidationFailure("Message", "Message is required")
        });
        mockValidator.Setup(x => x.ValidateAsync(It.IsAny<ValidationContext<TestRequest>>(), It.IsAny<CancellationToken>()))
                   .ReturnsAsync(validationResult);

        var behaviour = new ValidationBehaviour<TestRequest, TestResponse>(new[] { mockValidator.Object });
        var request = new TestRequest { Message = "" };
        var next = new Mock<RequestHandlerDelegate<TestResponse>>();

        // Act & Assert
        await Assert.ThrowsAsync<ValidationException>(() => 
            behaviour.Handle(request, next.Object, CancellationToken.None));
    }

    [Fact]
    public async Task ValidationBehaviour_ShouldContinue_WhenValidationPasses()
    {
        // Arrange
        var mockValidator = new Mock<IValidator<TestRequest>>();
        var validationResult = new ValidationResult(); // Empty = valid
        mockValidator.Setup(x => x.ValidateAsync(It.IsAny<ValidationContext<TestRequest>>(), It.IsAny<CancellationToken>()))
                   .ReturnsAsync(validationResult);

        var behaviour = new ValidationBehaviour<TestRequest, TestResponse>(new[] { mockValidator.Object });
        var request = new TestRequest { Message = "Valid" };
        var next = new Mock<RequestHandlerDelegate<TestResponse>>();
        next.Setup(x => x()).ReturnsAsync(new TestResponse { Result = "Success" });

        // Act
        var result = await behaviour.Handle(request, next.Object, CancellationToken.None);

        // Assert
        Assert.Equal("Success", result.Result);
        next.Verify(x => x(), Times.Once);
    }

    // Test request/response classes
    public class TestRequest : IRequest<TestResponse>
    {
        public string Message { get; set; } = string.Empty;
    }

    public class TestResponse
    {
        public string Result { get; set; } = string.Empty;
    }
}