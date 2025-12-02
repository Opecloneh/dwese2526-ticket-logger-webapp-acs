package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
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
@Transactional
public class ProvinceDAOImpl implements ProvinceDAO{

    private static final Logger logger = LoggerFactory.getLogger(ProvinceDAOImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Province> listAllProvinces() {
        logger.info("Listing all provinces with their regions from the database.");
        String hql = "SELECT p FROM Province p JOIN FETCH p.region";
        List<Province> provinces = entityManager.createQuery(hql, Province.class).getResultList();
        logger.info("Retrieved {} provinces from the database. ", provinces.size());
        return provinces;
    };
    /**
     * Inserta una nueva región en la base de datos.
     *
     * @param province Objeto {@link Region} a insertar.
     * @ Si ocurre un error al acceder a la base de datos.
     */
    @Override
    public void insertProvince(Province province) {
        logger.info("Insertando provincia con codigo: {}, nombre {} y regionId {}",
                province.getCode(), province.getName(),
                province.getRegion() != null ? province.getRegion().getId() : null
        );
        entityManager.persist(province);
        logger.info("Provincia insertada con codigo: {}", province.getId());
    }
    @Override
    public void updateProvince(Province province) {
        logger.info("Actualizando provincia con id: {}", province.getId());
        entityManager.merge(province);
        logger.info("Provincia actualizada con ID {}", province.getId());
    }
    @Override
    public void deleteProvince(Long id){
        logger.info("Deleting province with id: {}", id);
        Province province = entityManager.find(Province.class, id);
        if (province != null) {
            entityManager.remove(province);
            logger.info("Provincia eliminada con id: {}", id);
        }
        else{
            logger.info("No se encontro la provincia con id: {} ", id);
        }
    }
    @Override
    public Province getProvinceById (Long id) {
        logger.info("Recogiendo provincia por id: {}", id);
        Province province = entityManager.find(Province.class, id);
        if (province != null) {
            logger.info("Provincia encontrada: {} - {}", province.getCode(), province.getName());
        }
        else {
            logger.warn("No se ha encontrado la provincia con id: {}", id);
        }
        return province;
    }
    @Override
    public boolean existProvinceByCode(String code) {
        logger.info("Comprobando si la provincia con codigo: {} exsite", code);
        String hql = "SELECT COUNT(p) FROM Province p WHERE UPPER(p.code) = :code";
        Long count = entityManager.createQuery(hql, Long.class)
                .setParameter("code", code.toUpperCase())
                .getSingleResult();
        boolean exists = count != null && count > 0;
        logger.info("Provincia con codigo: {} existe: {}", code, exists);
        return exists;
    }
    @Override
    public boolean existProvinceByCodeAndNotId(String code, Long id) {
        logger.info("Comprobando si la provicia con codigo: {} existe excluyendo la id: {}", code, id);
        String hql = "SELECT COUNT(p) FROM Province p WHERE UPPER(p.code) = :code AND p.id != :id";
        Long count = entityManager.createQuery(hql, Long.class)
                .setParameter("code", code)
                .setParameter("id", id)
                .getSingleResult();
        boolean exists = count != null && count > 0;
        logger.info("Provincia con codigo: {} existe excluyendo la id {}: {}", code, id, exists);
        return exists;
    }
    @Override
    public List<Province> listProvincesPage(int page, int size, String sortField, String sortDir) {
        logger.info("Listing provinces page={}, size{}, sortField={}, sortDir={} from the database.",
                page, size, sortField, sortDir);

        int offset = page * size;
        //1. Construccion de criteria
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Province> cq = cb.createQuery(Province.class);
        Root<Province> root = cq.from(Province.class);
        //Fetch de regiones para que venga cargada
        root.fetch("region", JoinType.INNER);
        //Join separado para poder usar region.name en ORDER BY
        Join<Province, Region> regionJoin = root.join("region", JoinType.INNER);
        //2. Determinar el campo de ordenacion permitido
        Path<?> sortPath;
        switch (sortField) {
            case "id" -> sortPath = root.get("id");
            case "code" -> sortPath = root.get("code");
            case "name" -> sortPath = root.get("name");
            case "regionName" -> sortPath = regionJoin.get("name");
            default -> {
                logger.warn("Unknown sortField '{}', defaulting to 'name'.", sortField);
                sortPath = root.get("name");
            }
        }
        //3. Direccion de ordenacion
        boolean descending = "desc".equalsIgnoreCase(sortDir);
        //cb.desc y cb.asc sn funciones predefinidas de criteria para las ordenaciones
        Order order = descending ? cb.desc(sortPath) : cb.asc(sortPath);
        //4. Aplicar ordenacion a la query
        cq.select(root).orderBy(order);
        //5. Crear TypedQuery, aplicar paginacion y ejecutar
        return entityManager.createQuery(cq)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    };

    public long countProvinces() {
        String hql = "SELECT COUNT(p) FROM Province p";
        Long total = entityManager.createQuery(hql, Long.class).getSingleResult();
        return (total!=null) ? total: 0L;
    }
}
