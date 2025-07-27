using System.ComponentModel.DataAnnotations;

namespace Domain.Entities;

public class Nota
{
    public Guid Id { get; set; }
    
    [MaxLength(200)]
    public string? Titulo { get; set; }
    
    [Required]
    public string Contenido { get; set; } = string.Empty;
    
    [Required]
    public DateTime FechaCreacion { get; set; }
    
    [Required]
    public DateTime FechaModificacion { get; set; }
    
    [Required]
    public Guid UsuarioId { get; set; }
    
    public Usuario Usuario { get; set; } = null!;
    
    public ICollection<ArchivoAdjunto> ArchivosAdjuntos { get; set; } = new List<ArchivoAdjunto>();
}