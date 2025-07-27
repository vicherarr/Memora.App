using FluentValidation;

namespace Application.Features.Archivos.Commands;

public class DeleteArchivoCommandValidator : AbstractValidator<DeleteArchivoCommand>
{
    public DeleteArchivoCommandValidator()
    {
        RuleFor(x => x.ArchivoId)
            .NotEmpty()
            .WithMessage("El ID del archivo es requerido.");

        RuleFor(x => x.UsuarioId)
            .NotEmpty()
            .WithMessage("El ID del usuario es requerido.");
    }
}