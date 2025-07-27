using AutoMapper;
using Application.Features.Authentication.DTOs;
using Application.Features.Notas.DTOs;
using Domain.Entities;

namespace Application.Common.Mappings;

public class MappingProfile : Profile
{
    public MappingProfile()
    {
        // Usuario mappings
        CreateMap<Usuario, UsuarioDto>();
        CreateMap<RegisterUserDto, Usuario>();
        
        // Nota mappings
        CreateMap<Nota, NotaDto>();
        CreateMap<Nota, NotaDetailDto>()
            .ForMember(dest => dest.TotalArchivosAdjuntos, 
                      opt => opt.MapFrom(src => src.ArchivosAdjuntos.Count));
        CreateMap<CreateNotaDto, Nota>();
        CreateMap<UpdateNotaDto, Nota>();
        
        // ArchivoAdjunto mappings (commented until Archivos feature is implemented)
        // CreateMap<ArchivoAdjunto, Application.Features.Archivos.DTOs.ArchivoAdjuntoDto>();
    }
}