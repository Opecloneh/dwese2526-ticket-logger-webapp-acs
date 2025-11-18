package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Province;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProvinceDAOImpl implements ProvinceDAO{

    private static final Logger logger = LoggerFactory.getLogger(ProvinceDAOImpl.class);
    private final JdbcTemplate jdbcTemplate;

    public ProvinceDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Province> provinceRowMapper = (rs, rowNum) -> {
        Province province = new Province();

        province.setId(rs.getLong("id"));
        province.setCode(rs.getString("code"));
        province.setName(rs.getString("name"));

        Region region = new Region();
        region.setId(rs.getLong("region_id"));
        region.setCode(rs.getString("region_code"));
        region.setName(rs.getString("region_name"));

        province.setRegion(region);

        return province;
    };

    @Override
    public List<Province> listALlProvinces() {
        logger.info("Listing all provinces with their regions from the database.");
        String sql = "SELECT p.id, p.code, p.name, " +
                        "r.id AS region_id, r.code AS region_code, r.name AS region_name " +
                        "FROM provinces p " +
                        "JOIN regions r ON p.region_id = r.id";
        List<Province> provinces = jdbcTemplate.query(sql, provinceRowMapper);
        logger.info("Retrieved {} provinces from the database. ", provinces.size());
        return provinces;
    };
    @Override
    public boolean existProvinceByCode(String code) {
        logger.info("Comprobando si la provincia con codigo: {} exsite", code);
        String sql = "SELECT COUNT(*) FROM provinces WHERE UPPER(code) = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, code.toUpperCase());
        boolean exists = count != null && count > 0;
        logger.info("Provincia con codigo: {} existe: {}", code, exists);
        return exists;
    }
    /**
     * Inserta una nueva región en la base de datos.
     *
     * @param province Objeto {@link Region} a insertar.
     * @ Si ocurre un error al acceder a la base de datos.
     */
    @Override
    public void insertProvince(Province province) {
        logger.info("Insertando provincia con codigo: {}, nombre {} y regionId {}", province.getCode(), province.getName(), province.getRegion());
        String sql = "INSERT INTO provinces (code, name, region_id) VALUES (?, ?, ?)";
        int rowsAffected = jdbcTemplate.update(sql,
                province.getCode(),
                province.getName(),
                province.getRegion() != null ? province.getRegion().getId() : null);
        logger.info("Region insertada. Filas afectadas: {}", rowsAffected);
    }
    @Override
    public Province getProvinceById (Long id) {
        logger.info("Recogiendo provincia por id: {}", id);
        String sql = "SELECT p.id, p.code, p.name, " +
                "r.id AS region_id, r.code AS region_code, r.name AS region_name " +
                "FROM provinces p " +
                "JOIN regions r ON p.region_id = r.id " +
                "WHERE p.id = ?";
        try {
            Province province = jdbcTemplate.queryForObject(sql, provinceRowMapper, id);
            if (province != null) {
                logger.info("Provincia recogida: {} - {}", province.getCode(), province.getName());
            }
            return province;
        }
        catch (Exception e){
            logger.warn("No se encontro provincia con id: {}", id);
            return null;
        }
    }
    @Override
    public boolean existProvinceByCodeAndNotId(String code, Long id) {
        logger.info("Comprobando si la provicia con codigo: {} existe excluyendo la id: {}", code, id);
        String sql = "SELECT COUNT(*) FROM provinces WHERE UPPER(code) = ? AND id != ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, code.toUpperCase(), id);
        boolean exists = count != null && count > 0;
        logger.info("Provincia con codigo: {} existe excluyendo la id {}: {}", code, id, exists);
        return exists;
    }
    @Override
    public void updateProvince(Province province) {
        logger.info("Actualizando provincia con id: {}", province.getId());
        String sql = "UPDATE provinces SET code = ?, name = ?, region_id = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
                province.getCode(),
                province.getName(),
                province.getRegion() != null ? province.getRegion().getId() : null,
                province.getId()
        );
        logger.info("Provincia actualizada. Filas afectadas: {}", rowsAffected);
    }
    @Override
    public void deleteProvince(Long id){
        logger.info("Deleting province with id: {}", id);
        String sql = "DELETE FROM provinces WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql,id);
        logger.info("Deleted province. Rows affected: {}", rowsAffected);
    }

}
