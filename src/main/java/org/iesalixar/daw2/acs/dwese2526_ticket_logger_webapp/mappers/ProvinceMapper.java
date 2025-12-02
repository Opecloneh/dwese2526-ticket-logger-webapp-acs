package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.*;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Province;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Province;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;

import java.util.List;

public class ProvinceMapper {
    // Entity -> DTO (listado/tabla basico)
    public static ProvinceDTO toDTO(Province entity) {
        if (entity == null) return null;
        ProvinceDTO dto = new ProvinceDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        //Establecemos solo el nombre de la region que es lo unico que mostramos en los listados
        dto.setRegionName(entity.getRegion().getName());
        return dto;
    }
    public static List<ProvinceDTO> toDTOList(List<Province> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(ProvinceMapper::toDTO).toList();
    }
    //Entity -> DTO (detalle con provincias)
    public static ProvinceDetailDTO toDetailDTO(Province entity) {
        if (entity == null) return null;
        ProvinceDetailDTO dto = new ProvinceDetailDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setRegion(RegionMapper.toDTO(entity.getRegion()));
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
        return provinces.stream().map(ProvinceMapper::toProvinceDTO).toList();
    }
    public static ProvinceUpdateDTO toUpdateDTO(Province entity) {
        if (entity == null) return null;
        ProvinceUpdateDTO dto = new ProvinceUpdateDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        return dto;
    }
    //DTO (Create/Update) -> Entity
    public static Province toEntity(ProvinceUpdateDTO dto){
        if (dto == null) return null;
        Province e = new Province();
        e.setId(dto.getId());
        e.setCode(dto.getCode());
        e.setName(dto.getName());
        Region region = new Region();
        region.setId(dto.getRegionId());
        e.setRegion(region);
        return e;
    }
    public static Province toEntity(ProvinceCreateDTO dto){
        if (dto == null) return null;
        Province e = new Province();
        e.setId(dto.getId());
        e.setCode(dto.getCode());
        e.setName(dto.getName());
        Region region = new Region();
        region.setId(dto.getRegionId());
        e.setRegion(region);
        return e;
    }
    public void copyToExistingEntity(ProvinceUpdateDTO dto, Province entity){
        if (dto == null || entity == null) return;
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        Region region = new Region();
        region.setId(dto.getRegionId());
        entity.setRegion(region);
    }
}
