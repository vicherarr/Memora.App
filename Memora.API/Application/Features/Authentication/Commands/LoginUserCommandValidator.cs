using FluentValidation;

namespace Application.Features.Authentication.Commands;

public class LoginUserCommandValidator : AbstractValidator<LoginUserCommand>
{
    public LoginUserCommandValidator()
    {
        RuleFor(x => x.CorreoElectronico)
            .NotEmpty().WithMessage("Email is required")
            .EmailAddress().WithMessage("A valid email address is required");

        RuleFor(x => x.Contrasena)
            .NotEmpty().WithMessage("Password is required");
    }
}