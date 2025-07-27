using System.ComponentModel.DataAnnotations;
using Domain.Enums;

namespace Domain.Entities;

public class ArchivoAdjunto
{
    public Guid Id { get; set; }
    
    [Required]
    public byte[] DatosArchivo { get; set; } = Array.Empty<byte>();
    
    [Required]
    [MaxLength(255)]
    public string NombreOriginal { get; set; } = string.Empty;
    
    [Required]
    public TipoDeArchivo TipoArchivo { get; set; }
    
    [Required]
    [MaxLength(100)]
    public string TipoMime { get; set; } = string.Empty;
    
    [Required]
    public long TamanoBytes { get; set; }
    
    [Required]
    public DateTime FechaSubida { get; set; }
    
    [Required]
    public Guid NotaId { get; set; }
    
    public Nota Nota { get; set; } = null!;
}