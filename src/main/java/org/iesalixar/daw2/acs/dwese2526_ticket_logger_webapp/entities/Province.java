package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Province {
    private Long id;

    @NotEmpty(message = "{msg.province.code.notEmpty}")
    @Size(max = 10, message = "{msg.province.code.size}")
    private String code;

    @NotEmpty(message = "{msg.province.name.notEmpty}")
    @Size(max = 100, message = "{msg.province.name.size}")
    private String name;

    private Region region;

    @AssertTrue(message = "{msg.province.region.notNull}")
    public boolean isRegionSelected() {
        return region != null && region.getId() != null;
    }

    public Province(String code, String name, Region region){
        this.code = code;
        this.name = name;
        this.region = region;
    }
}
