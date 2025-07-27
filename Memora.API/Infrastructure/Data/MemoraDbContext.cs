using Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace Infrastructure.Data;

public class MemoraDbContext : DbContext
{
    public MemoraDbContext(DbContextOptions<MemoraDbContext> options) : base(options)
    {
    }

    public DbSet<Usuario> Usuarios { get; set; }
    public DbSet<Nota> Notas { get; set; }
    public DbSet<ArchivoAdjunto> ArchivosAdjuntos { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        // Usuario entity configuration
        modelBuilder.Entity<Usuario>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.HasIndex(e => e.CorreoElectronico).IsUnique();
            
            entity.Property(e => e.Id).ValueGeneratedOnAdd();
            entity.Property(e => e.FechaCreacion).HasDefaultValueSql("DATETIME('now')");
        });

        // Nota entity configuration
        modelBuilder.Entity<Nota>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.HasIndex(e => e.UsuarioId);
            entity.HasIndex(e => e.FechaCreacion);
            
            entity.Property(e => e.Id).ValueGeneratedOnAdd();
            entity.Property(e => e.FechaCreacion).HasDefaultValueSql("DATETIME('now')");
            entity.Property(e => e.FechaModificacion).HasDefaultValueSql("DATETIME('now')");

            entity.HasOne(e => e.Usuario)
                .WithMany(u => u.Notas)
                .HasForeignKey(e => e.UsuarioId)
                .OnDelete(DeleteBehavior.Cascade);
        });

        // ArchivoAdjunto entity configuration
        modelBuilder.Entity<ArchivoAdjunto>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.HasIndex(e => e.NotaId);
            
            entity.Property(e => e.Id).ValueGeneratedOnAdd();
            entity.Property(e => e.FechaSubida).HasDefaultValueSql("DATETIME('now')");

            entity.HasOne(e => e.Nota)
                .WithMany(n => n.ArchivosAdjuntos)
                .HasForeignKey(e => e.NotaId)
                .OnDelete(DeleteBehavior.Cascade);
        });
    }
}