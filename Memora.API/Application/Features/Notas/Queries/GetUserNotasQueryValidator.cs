using FluentValidation;

namespace Application.Features.Notas.Queries;

public class GetUserNotasQueryValidator : AbstractValidator<GetUserNotasQuery>
{
    public GetUserNotasQueryValidator()
    {
        RuleFor(x => x.UsuarioId)
            .NotEmpty()
            .WithMessage("El ID del usuario es requerido");

        RuleFor(x => x.PageNumber)
            .GreaterThan(0)
            .WithMessage("El número de página debe ser mayor a 0");

        RuleFor(x => x.PageSize)
            .GreaterThan(0)
            .WithMessage("El tamaño de página debe ser mayor a 0")
            .LessThanOrEqualTo(100)
            .WithMessage("El tamaño de página no puede exceder 100");
    }
}