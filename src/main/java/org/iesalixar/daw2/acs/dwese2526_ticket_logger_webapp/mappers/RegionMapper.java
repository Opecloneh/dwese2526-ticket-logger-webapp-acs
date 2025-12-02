package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.*;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Province;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;

import java.util.List;

public class RegionMapper {
    // Entity -> DTO (listado/tabla basico)
    public static RegionDTO toDTO(Region entity) {
        if (entity == null) return null;
        RegionDTO dto = new RegionDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        return dto;
    }
    public static List<RegionDTO> toDTOList(List<Region> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(RegionMapper::toDTO).toList();
    }
    //Entity -> DTO (detalle con provincias)
    public static RegionDetailDTO toDetailDTO(Region entity) {
        if (entity == null) return null;

        RegionDetailDTO dto = new RegionDetailDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setProvinces(toProvinceList(entity.getProvinces()));
        return dto;
    }
    public static ProvinceDTO toProvinceDTO(Province p){
        if (p == null) return null;
        ProvinceDTO dto = new ProvinceDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        return dto;
    }
    public static List<ProvinceDTO> toProvinceList(List<Province> provinces) {
        if (provinces == null) return List.of();
        return provinces.stream().map(RegionMapper::toProvinceDTO).toList();
    }
    public static RegionUpdateDTO toUpdateDTO(Region entity) {
        if (entity == null) return null;
        RegionUpdateDTO dto = new RegionUpdateDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        return dto;
    }
    //DTO (Create/Update) -> Entity
    public static Region toEntity(RegionCreateDTO dto){
        if (dto == null) return null;
        Region e = new Region();
        e.setCode(dto.getCode());
        e.setName(dto.getName());
        return e;
    }
    public static void copyToExistingEntity(RegionUpdateDTO dto, Region entity){
        if (dto == null || entity == null) return;
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
    }
}
