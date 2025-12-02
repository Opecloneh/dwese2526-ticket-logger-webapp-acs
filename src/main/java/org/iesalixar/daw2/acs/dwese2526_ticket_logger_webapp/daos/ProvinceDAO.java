package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.controllers.ProvinceController;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Province;

import java.util.List;

public interface ProvinceDAO {
    List<Province> listAllProvinces();
    List<Province> listProvincesPage(int page, int size, String sortField, String sortDir);
    long countProvinces();
    void insertProvince(Province province);
    void updateProvince(Province province);
    void deleteProvince(Long id);
    Province getProvinceById(Long id);
    boolean existProvinceByCode(String code);
    boolean existProvinceByCodeAndNotId(String code, Long id);

}
