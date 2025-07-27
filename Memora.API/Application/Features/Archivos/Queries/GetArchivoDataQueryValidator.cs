using FluentValidation;

namespace Application.Features.Archivos.Queries;

public class GetArchivoDataQueryValidator : AbstractValidator<GetArchivoDataQuery>
{
    public GetArchivoDataQueryValidator()
    {
        RuleFor(x => x.ArchivoId)
            .NotEmpty()
            .WithMessage("El ID del archivo es requerido.");

        RuleFor(x => x.UsuarioId)
            .NotEmpty()
            .WithMessage("El ID del usuario es requerido.");
    }
}