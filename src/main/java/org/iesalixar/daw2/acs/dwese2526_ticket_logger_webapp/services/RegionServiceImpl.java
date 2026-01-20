package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services;

import jakarta.transaction.Transactional;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.RegionCreateDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.RegionDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.RegionDetailDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.RegionUpdateDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers.RegionMapper;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.repositories.RegionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class RegionServiceImpl implements RegionService{

    @Autowired
    private RegionRepository regionRepository;

    @Override
    public Page<RegionDTO> list(Pageable pageable) {
        return regionRepository.findAll(pageable).map(RegionMapper::toDTO);
    }

    @Override
    public RegionUpdateDTO getForEdit(Long id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("region", "id", id));
        return RegionMapper.toUpdateDTO(region);
    }

    @Override
    public void create(RegionCreateDTO dto) {
        if (regionRepository.existsByCode(dto.getCode())) {
            throw new DuplicateResourceException("region", "code", dto.getCode());
        }
        Region region = RegionMapper.toEntity(dto);
        regionRepository.save(region);
    }

    @Override
    public void update(RegionUpdateDTO dto) {
        if (regionRepository.existsByCodeAndIdNot(dto.getCode(), dto.getId())){
            throw new DuplicateResourceException("region", "code", dto.getCode());
        }
        Region region = regionRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("region", "id", dto.getId()));

        RegionMapper.copyToExistingEntity(dto, region);
        regionRepository.save(region);
    }

    @Override
    public void delete(Long id) {
        if (!regionRepository.existsById(id)) {
            throw new ResourceNotFoundException("region", "id", id);
        }
        regionRepository.deleteById(id);
    }

    @Override
    public RegionDetailDTO getDetail(Long id) {
        Region region = regionRepository.findByIdWithProvinces(id)
                .orElseThrow(() -> new ResourceNotFoundException("region", "id", id));
        return RegionMapper.toDetailDTO(region);
    }

    @Override
    public List<RegionDTO> listAll() {
        return regionRepository.findAll()
                .stream()
                .map(RegionMapper::toDTO)
                .toList();
    }

    @Override
    public Region findById(Long id) {
        return regionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("region", "id", id));
    }

}
