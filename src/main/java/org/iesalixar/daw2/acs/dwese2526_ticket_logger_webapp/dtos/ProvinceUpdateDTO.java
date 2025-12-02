package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvinceUpdateDTO {

    @NotNull(message = "{msg.province.id.notEmpty}")
    private Long id;

    @NotBlank(message = "{msg.province.code.notEmpty}")
    @Size(max = 2, message = "{msg.province.code.size}")
    private String code;

    @NotBlank(message = "{msg.province.name.notEmpty}")
    @Size(max = 100, message = "{msg.province.name.size}")
    private String name;

    @NotNull(message = "{msg.province.regionId.notEmpty}")
    private Long regionId;
}
