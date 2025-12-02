package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;
import org.springframework.jdbc.object.SqlQuery;

import java.sql.SQLException;
import java.util.List;

public interface RegionDAO {
    List<Region> listAllRegions();
    List<Region> listRegionsPage(int page, int size, String sortField, String sortDir);
    long countRegions();
    void insertRegion(Region region);
    void updateRegion(Region region);
    void deleteRegion(Long id);
    Region getRegionById(Long id);
    boolean existRegionByCode(String code);
    boolean existRegionByCodeAndNotId(String code, Long id);

}
