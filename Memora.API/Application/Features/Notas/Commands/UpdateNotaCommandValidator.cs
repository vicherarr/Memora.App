using FluentValidation;

namespace Application.Features.Notas.Commands;

public class UpdateNotaCommandValidator : AbstractValidator<UpdateNotaCommand>
{
    public UpdateNotaCommandValidator()
    {
        RuleFor(x => x.NotaId)
            .NotEmpty()
            .WithMessage("El ID de la nota es requerido");

        RuleFor(x => x.Contenido)
            .NotEmpty()
            .WithMessage("El contenido de la nota es requerido")
            .MaximumLength(10000)
            .WithMessage("El contenido no puede exceder 10,000 caracteres");

        RuleFor(x => x.Titulo)
            .MaximumLength(200)
            .WithMessage("El título no puede exceder 200 caracteres")
            .When(x => !string.IsNullOrWhiteSpace(x.Titulo));

        RuleFor(x => x.UsuarioId)
            .NotEmpty()
            .WithMessage("El ID del usuario es requerido");
    }
}