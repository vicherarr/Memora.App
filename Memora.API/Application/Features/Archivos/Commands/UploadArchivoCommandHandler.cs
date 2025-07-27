using Application.Common.Interfaces;
using Application.Features.Archivos.DTOs;
using Domain.Entities;
using Domain.Enums;
using Infrastructure.Data;
using MediatR;
using Microsoft.EntityFrameworkCore;
using System.IO;
using System.Net.Sockets;
using static System.Net.Mime.MediaTypeNames;

namespace Application.Features.Archivos.Commands;

public class UploadArchivoCommandHandler : IRequestHandler<UploadArchivoCommand, UploadArchivoResponseDto>
{
    private readonly MemoraDbContext _context;
    private readonly IFileProcessingService _fileProcessingService;

    public UploadArchivoCommandHandler(MemoraDbContext context, IFileProcessingService fileProcessingService)
    {
        _context = context;
        _fileProcessingService = fileProcessingService;
    }

    public async Task<UploadArchivoResponseDto> Handle(UploadArchivoCommand request, CancellationToken cancellationToken)
    {
        // Verificar que la nota existe y pertenece al usuario
        var nota = await _context.Notas
            .FirstOrDefaultAsync(n => n.Id == request.NotaId && n.UsuarioId == request.UsuarioId, cancellationToken);

        if (nota == null)
        {
            throw new UnauthorizedAccessException("No tienes permisos para subir archivos a esta nota.");
        }

        // Validar archivo usando el servicio de procesamiento
        var isValid = await _fileProcessingService.ValidateFileAsync(request.FileData, request.FileName, request.ContentType);
        if (!isValid)
        {
            throw new ArgumentException("El archivo no cumple con los requisitos de validación.");
        }

        // Obtener el MIME type validado y el tipo de archivo
        var validMimeType = _fileProcessingService.GetValidMimeType(request.FileData, request.ContentType);
        var tipoArchivo = _fileProcessingService.DetectFileType(validMimeType);

        // Comprimir imagen si es necesario
        var processedData = tipoArchivo == TipoDeArchivo.Imagen 
            ? await _fileProcessingService.CompressImageAsync(request.FileData, validMimeType)
            : request.FileData;

        // Crear archivo adjunto
        var archivo = new ArchivoAdjunto
        {
            Id = Guid.NewGuid(),
            DatosArchivo = processedData,
            NombreOriginal = request.FileName,
            TipoArchivo = tipoArchivo,
            TipoMime = validMimeType,
            TamanoBytes = processedData.Length,
            FechaSubida = DateTime.UtcNow,
            NotaId = request.NotaId
        };

        _context.ArchivosAdjuntos.Add(archivo);
        await _context.SaveChangesAsync(cancellationToken);

        return new UploadArchivoResponseDto
        {
            ArchivoId = archivo.Id,
            NombreOriginal = archivo.NombreOriginal,
            TamanoBytes = archivo.TamanoBytes,
            TipoMime = archivo.TipoMime,
            Mensaje = "Archivo subido exitosamente."
        };
    }

}