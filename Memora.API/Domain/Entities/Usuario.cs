using System.ComponentModel.DataAnnotations;

namespace Domain.Entities;

public class Usuario
{
    public Guid Id { get; set; }
    
    [Required]
    [MaxLength(100)]
    public string NombreCompleto { get; set; } = string.Empty;
    
    [Required]
    [MaxLength(255)]
    [EmailAddress]
    public string CorreoElectronico { get; set; } = string.Empty;
    
    [Required]
    public string ContrasenaHash { get; set; } = string.Empty;
    
    [Required]
    public DateTime FechaCreacion { get; set; }
    
    public ICollection<Nota> Notas { get; set; } = new List<Nota>();
}