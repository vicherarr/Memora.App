using Xunit;

namespace Memora.Tests;

public class SimpleTest
{
    [Fact]
    public void SimpleTest_ShouldPass()
    {
        // Arrange
        var result = 2 + 2;
        
        // Act & Assert
        Assert.Equal(4, result);
    }
}