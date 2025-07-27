using FluentValidation;

namespace Application.Features.Authentication.Commands;

public class RegisterUserCommandValidator : AbstractValidator<RegisterUserCommand>
{
    // Lista de dominios de email desechables bloqueados
    private static readonly HashSet<string> BlockedEmailDomains = new(StringComparer.OrdinalIgnoreCase)
    {
        "temp-mail.org", "10minutemail.com", "guerrillamail.com", "mailinator.com",
        "throwaway.email", "yopmail.com", "tempmail.org", "disposable.com"
    };

    public RegisterUserCommandValidator()
    {
        RuleFor(x => x.NombreCompleto)
            .NotEmpty().WithMessage("Full name is required")
            .Length(2, 100).WithMessage("Full name must be between 2 and 100 characters")
            .Matches(@"^[a-zA-ZñÑáéíóúÁÉÍÓÚüÜ\s.-]+$").WithMessage("Full name can only contain letters, spaces, dots, and dashes")
            .Must(NotContainConsecutiveSpaces).WithMessage("Full name cannot contain consecutive spaces")
            .Must(NotStartOrEndWithSpace).WithMessage("Full name cannot start or end with spaces")
            .Must(ContainAtLeastTwoWords).WithMessage("Full name must contain at least two words");

        RuleFor(x => x.CorreoElectronico)
            .NotEmpty().WithMessage("Email is required")
            .EmailAddress().WithMessage("A valid email address is required")
            .MaximumLength(255).WithMessage("Email must not exceed 255 characters")
            .Must(NotBeDisposableEmail).WithMessage("Disposable email addresses are not allowed")
            .Must(HaveValidEmailFormat).WithMessage("Email format is not valid");

        RuleFor(x => x.Contrasena)
            .NotEmpty().WithMessage("Password is required")
            .MinimumLength(8).WithMessage("Password must be at least 8 characters long")
            .MaximumLength(128).WithMessage("Password must not exceed 128 characters")
            .Matches(@"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]")
            .WithMessage("Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character (@$!%*?&)")
            .Must(NotContainCommonPatterns).WithMessage("Password contains common patterns that are not secure")
            .Must(NotContainUserInfo).WithMessage("Password cannot contain parts of your name or email");
    }

    private static bool NotContainConsecutiveSpaces(string name) => !name.Contains("  ");
    
    private static bool NotStartOrEndWithSpace(string name) => !name.StartsWith(" ") && !name.EndsWith(" ");
    
    private static bool ContainAtLeastTwoWords(string name) => name.Trim().Split(' ', StringSplitOptions.RemoveEmptyEntries).Length >= 2;
    
    private static bool NotBeDisposableEmail(string email)
    {
        if (string.IsNullOrEmpty(email)) return true;
        
        var domain = email.Split('@').LastOrDefault();
        return domain == null || !BlockedEmailDomains.Contains(domain);
    }
    
    private static bool HaveValidEmailFormat(string email)
    {
        if (string.IsNullOrEmpty(email)) return true;
        
        // Validaciones adicionales más estrictas que EmailAddress()
        var parts = email.Split('@');
        if (parts.Length != 2) return false;
        
        var localPart = parts[0];
        var domain = parts[1];
        
        // El local part no puede estar vacío o ser muy largo
        if (string.IsNullOrEmpty(localPart) || localPart.Length > 64) return false;
        
        // El dominio debe tener al menos un punto y no puede empezar/terminar con punto o guión
        if (!domain.Contains('.') || domain.StartsWith('.') || domain.EndsWith('.') || 
            domain.StartsWith('-') || domain.EndsWith('-')) return false;
        
        return true;
    }
    
    private static bool NotContainCommonPatterns(string password)
    {
        if (string.IsNullOrEmpty(password)) return true;
        
        var commonPatterns = new[]
        {
            "123456", "password", "qwerty", "abc123", "admin", "letmein",
            "welcome", "monkey", "dragon", "master", "hello", "freedom"
        };
        
        var lowerPassword = password.ToLower();
        return !commonPatterns.Any(pattern => lowerPassword.Contains(pattern));
    }
    
    private bool NotContainUserInfo(RegisterUserCommand command, string password)
    {
        if (string.IsNullOrEmpty(password)) return true;
        
        var lowerPassword = password.ToLower();
        
        // Verificar que no contenga partes del nombre
        if (!string.IsNullOrEmpty(command.NombreCompleto))
        {
            var nameParts = command.NombreCompleto.ToLower().Split(' ', StringSplitOptions.RemoveEmptyEntries);
            foreach (var part in nameParts)
            {
                if (part.Length >= 3 && lowerPassword.Contains(part))
                    return false;
            }
        }
        
        // Verificar que no contenga partes del email
        if (!string.IsNullOrEmpty(command.CorreoElectronico))
        {
            var emailLocalPart = command.CorreoElectronico.Split('@')[0].ToLower();
            if (emailLocalPart.Length >= 3 && lowerPassword.Contains(emailLocalPart))
                return false;
        }
        
        return true;
    }
}