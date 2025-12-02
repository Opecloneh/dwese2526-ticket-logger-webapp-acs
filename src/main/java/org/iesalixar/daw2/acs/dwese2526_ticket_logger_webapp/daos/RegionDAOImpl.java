package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.transaction.TransactionScoped;
import jakarta.transaction.Transactional;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.List;


@Repository //Anotacion que marca esta clase como un componente que gestiona la persistencia
@Transactional
public class RegionDAOImpl implements RegionDAO {
    private static final Logger logger = LoggerFactory.getLogger(RegionDAOImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    public List<Region> listAllRegions() {
        logger.info("Listando las regiones de la base de datos.");
        String hql = "SELECT r FROM Region r";
        List<Region> regions = entityManager.createQuery(hql, Region.class).getResultList();
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
        entityManager.persist(region);
        logger.info("Region insertada.");
    }

    /**
     * Actualiza los datos de una región existente en la base de datos.
     *
     * @param region Objeto {@link Region} con los datos actualizados.
     * @ Si ocurre un error al acceder a la base de datos.
     */
    public void updateRegion(Region region) {
        logger.info("Actualizando region por id: {}", region.getId());
        entityManager.merge(region);
        logger.info("Region actualizada. Filas afectadas: {}", region.getId());
    }
    /**
     * Elimina datos de una región existente en la base de datos.
     *
     * @param id Objeto {@link Region} con los datos actualizados.
     * @ Si ocurre un error al acceder a la base de datos.
     */
    public void deleteRegion(Long id) {
        logger.info("Borrando regiones con id: {}", id);
        Region region = entityManager.find(Region.class, id);
        if(region != null) {
            entityManager.remove(region);
            logger.info("Region eliminada con sesion: {}", id);
        }
        logger.info("Region borrada. Filas afectadas: {}", id);
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
        Region region = entityManager.find(Region.class, id);
        if (region != null) {
            logger.info("Region retrievevd: {} - {}", region.getCode(), region.getName());
        }
        else {
            logger.warn("No region found with id: {}", id);
        }
        return region;
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
        String hql = "SELECT COUNT(r) FROM Region r WHERE UPPER(r.code) = :code";
        Long count = entityManager.createQuery(hql, Long.class)
                .setParameter("code", code.toUpperCase())
                .getSingleResult();
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
        String hql = "SELECT COUNT(r) FROM Region r WHERE UPPER(r.code) = :code AND r.id != :id";
        Long count = entityManager.createQuery(hql, Long.class).
                setParameter("code", code.toUpperCase()).
                setParameter("id", id).
                getSingleResult();
        boolean exists  = count != null && count > 0;
        logger.info("Region with code: {} exists excluding id {}: {}", code, id, exists);
        return exists;
    }

    public List<Region> listRegionsPage(int page, int size, String sortField, String sortDir) {
        logger.info("Listing regions page={}, size={}, sortField={}, sortDir={} from the database.",
                page, size, sortField, sortDir);

        int offset = page * size;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Region> cq = cb.createQuery(Region.class);
        Root<Region> root = cq.from(Region.class);

        Path<?> sortPath;
        switch (sortField) {
            case "id" -> sortPath = root.get("id");
            case "code" -> sortPath = root.get("code");
            case "name" -> sortPath = root.get("name");
            default -> {
                logger.warn("Unknown sortField '{}', defaulting to 'name'.", sortField);
                sortPath = root.get("name");
            }
        }
        //Direccion de ordenacion
        boolean descending = "desc".equalsIgnoreCase(sortDir);
        //cb.desc y cb.asc sn funciones predefinidas de criteria para las ordenaciones
        Order order = descending ? cb.desc(sortPath) : cb.asc(sortPath);
        //Aplicar ordenacion a la query
        cq.select(root).orderBy(order);
        //Crear TypedQuery, aplicar paginacion y ejecutar
        return entityManager.createQuery(cq)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    public long countRegions() {
        String hql = "SELECT COUNT(r) FROM Region r";
        Long total = entityManager.createQuery(hql, Long.class).getSingleResult();
        return (total != null) ? total : 0L;
    }
}
