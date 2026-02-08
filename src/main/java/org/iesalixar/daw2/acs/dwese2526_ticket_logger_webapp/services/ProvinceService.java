package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.ProvinceCreateDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.ProvinceDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.ProvinceDetailDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.ProvinceUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProvinceService {
    Page<ProvinceDTO> list(Pageable pageable);
    ProvinceUpdateDTO getForEdit(Long id);
    void create(ProvinceCreateDTO dto);
    void update(ProvinceUpdateDTO dto);
    void delete(Long id);
    ProvinceDetailDTO getDetail(Long id);

    List<ProvinceDTO> listAll();

    Object listRegionsForSelect();
}
