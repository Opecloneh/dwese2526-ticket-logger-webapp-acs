package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Normalized;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionDetailDTO {
    private Long id;
    private String code;
    private String name;
    private List<ProvinceDTO> provinces;
}
