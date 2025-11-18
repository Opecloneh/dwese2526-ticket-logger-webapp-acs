package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.controllers.ProvinceController;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Province;

import java.util.List;

public interface ProvinceDAO {
    List<Province> listALlProvinces();
    void insertProvince(Province province);
    void updateProvince(Province province);
    void deleteProvince(Long id);
    Province getProvinceById(Long id);
    boolean existProvinceByCode(String code);
    boolean existProvinceByCodeAndNotId(String code, Long id);

}
