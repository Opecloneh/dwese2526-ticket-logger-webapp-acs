package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.List;


@Repository //Anotacion que marca esta clase como un componente que gestiona la persistencia
public class RegionDAOImpl implements RegionDAO {
    private static final Logger logger = LoggerFactory.getLogger(RegionDAOImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public RegionDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Obtiene una lista con todas las regiones almacenadas en la base de datos.
     *
     * @return Lista de objetos {@link Region}.
     * @ Si ocurre un error al acceder a la base de datos.
     */
    public List<Region> listAllRegions() {
        logger.info("Listando las regiones de la base de datos.");
        String sql = "SELECT * FROM regions";
        List<Region> regions = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Region.class));
        logger.info("Retrieved {} regions from the database.", regions.size());
        return regions;
    }

    /**
     * Inserta una nueva región en la base de datos.
     *
     * @param region Objeto {@link Region} a insertar.
     * @ Si ocurre un error al acceder a la base de datos.
     */
    @Override
    public void insertRegion(Region region) {
        logger.info("Insertando region con codigo: {} y nombre {}", region.getCode(), region.getName());
        String sql = "INSERT INTO regions (code, name) VALUES (?, ?)";
        int rowsAffected = jdbcTemplate.update(sql, region.getCode(), region.getName());
        logger.info("Region insertada. Filas afectadas: {}", rowsAffected);
    }

    /**
     * Actualiza los datos de una región existente en la base de datos.
     *
     * @param region Objeto {@link Region} con los datos actualizados.
     * @ Si ocurre un error al acceder a la base de datos.
     */
    public void updateRegion(Region region) {
        logger.info("Actualizando region por id: {}", region.getId());
        String sql = "UPDATE regions SET code = ?, name = ? WHERE ID = ?";
        int rowsAffected = jdbcTemplate.update(sql, region.getCode(), region.getName(), region.getId());
        logger.info("Region actualizada. Filas afectadas: {}", rowsAffected);
    }
    /**
     * Elimina datos de una región existente en la base de datos.
     *
     * @param id Objeto {@link Region} con los datos actualizados.
     * @ Si ocurre un error al acceder a la base de datos.
     */
    public void deleteRegion(Long id) {
        logger.info("Borrando regiones con id: {}", id);
        String sql = "DELETE FROM regions WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        logger.info("Region borrada. Filas afectadas: {}", rowsAffected);
    }

    /**
     * Obtiene una región por su identificador.
     *
     * @param id Identificador de la región.
     * @return Objeto {@link Region} si existe, null si no se encuentra.
     * @ Si ocurre un error al acceder a la base de datos.
     */
    public Region getRegionById (Long id) {
        logger.info("Recogiendo region por id: {}", id);
        String sql = "SELECT * FROM regions WHERE id = ?";
        try {
            Region region = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Region.class), id);
            logger.info("Region recogida: {} - {}", region.getCode(), region.getName());
            return region;
        }
        catch (Exception e){
            logger.warn("No se encontro region con id: {}", id);
            return null;
        }

    }

    /**
     * Comprueba si existe una región en la base de datos con el código indicado.
     * La búsqueda no distingue entre mayúsculas y minúsculas.
     *
     * @param code Código de la región a buscar.
     * @return true si existe al menos una región con ese código, false en caso contrario.
     * @ Si ocurre un error al acceder a la base de datos.
     */
    @Override
    public boolean existRegionByCode(String code) {
        logger.info("Comprobando si la region con codigo: {} exsite", code);
        String sql = "SELECT COUNT(*) FROM regions WHERE UPPER(code) = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, code.toUpperCase());
        boolean exists = count != null && count > 0;
        logger.info("Region con codigo: {} existe: {}", code, exists);
        return exists;
    }

    /**
     * Comprueba si existe otra región con el mismo código pero diferente ID.
     * Útil para validar la unicidad del código antes de actualizar un registro.
     *
     * @param code Código de la región a comprobar.
     * @param id   ID de la región que se va a excluir de la búsqueda.
     * @return true si existe otra región con el mismo código, false en caso contrario.
     * @ Si ocurre un error al acceder a la base de datos.
     */
    @Override
    public boolean existRegionByCodeAndNotId(String code, Long id) {
        logger.info("Comprobando si la region con codigo: {} existe excluyendo la id: {}", code, id);
        String sql = "SELECT COUNT(*) FROM regions WHERE UPPER(code) = ? AND ID != ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, code.toUpperCase(), id);
        boolean exists  = count != null && count > 0;
        logger.info("Region with code: {} exists excluding id {}: {}", code, id, exists);
        return exists;
    }
}
