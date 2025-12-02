package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvinceDTO {
    private Long id;
    private String code;
    private String name;
    private String regionName;
}
