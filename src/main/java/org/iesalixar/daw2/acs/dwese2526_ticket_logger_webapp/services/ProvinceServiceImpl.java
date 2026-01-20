package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services;

import jakarta.transaction.Transactional;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.ProvinceCreateDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.ProvinceDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.ProvinceDetailDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.ProvinceUpdateDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Province;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers.ProvinceMapper;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.repositories.ProvinceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ProvinceServiceImpl implements ProvinceService {

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private RegionService regionService;

    @Override
    public Page<ProvinceDTO> list(Pageable pageable) {
        return provinceRepository.findAll(pageable).map(ProvinceMapper::toDTO);
    }

    @Override
    public ProvinceUpdateDTO getForEdit(Long id) {
        Province province = provinceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("province", "id", id));
        return ProvinceMapper.toUpdateDTO(province);
    }

    @Override
    public void create(ProvinceCreateDTO dto) {
        if (provinceRepository.existsByCode(dto.getCode())) {
            throw new DuplicateResourceException("province", "code", dto.getCode());
        }

        Region region = regionService.findById(dto.getRegionId());
        Province province = new Province(dto.getCode(), dto.getName(), region);
        provinceRepository.save(province);
    }

    @Override
    public void update(ProvinceUpdateDTO dto) {
        Province province = provinceRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("province", "id", dto.getId()));

        if (provinceRepository.existsByCodeAndIdNot(dto.getCode(), dto.getId())) {
            throw new DuplicateResourceException("province", "code", dto.getCode());
        }

        Region region = regionService.findById(dto.getRegionId());
        province.setCode(dto.getCode());
        province.setName(dto.getName());
        province.setRegion(region);

        provinceRepository.save(province);
    }

    @Override
    public void delete(Long id) {
        if (!provinceRepository.existsById(id)) {
            throw new ResourceNotFoundException("province", "id", id);
        }
        provinceRepository.deleteById(id);
    }

    @Override
    public ProvinceDetailDTO getDetail(Long id) {
        Province province = provinceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("province", "id", id));
        return ProvinceMapper.toDetailDTO(province);
    }

    @Override
    public List<ProvinceDTO> listAll() {
        return provinceRepository.findAll().stream()
                .map(ProvinceMapper::toDTO)
                .toList();
    }
}
