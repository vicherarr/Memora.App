using FluentValidation;

namespace Application.Features.Notas.Queries;

public class GetNotaByIdQueryValidator : AbstractValidator<GetNotaByIdQuery>
{
    public GetNotaByIdQueryValidator()
    {
        RuleFor(x => x.NotaId)
            .NotEmpty()
            .WithMessage("El ID de la nota es requerido");

        RuleFor(x => x.UsuarioId)
            .NotEmpty()
            .WithMessage("El ID del usuario es requerido");
    }
}