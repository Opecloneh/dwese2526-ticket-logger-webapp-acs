package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@Transactional
public class RoleDAOImpl implements RoleDAO{

    private static final Logger logger = LoggerFactory.getLogger(RoleDAOImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Role> listAllRoles() {
        logger.info("Listando los roles de la base de datos.");
        String hql = "SELECT r FROM Role r ORDER BY r.name";
        List<Role> roles = entityManager.createQuery(hql, Role.class).getResultList();
        logger.info("Retrieved {} roles form the database.", roles.size());
        return roles;
    }

    @Override
    public List<Role> findAllByIds(Set<Long> ids) {
       if (ids == null || ids.isEmpty()) {
           logger.info("findAllByIds llamada con null o id vacia. Devolviendo lista vacia");
           return List.of();
       }
       logger.info("Buscando roles con id: {}", ids);
       String hql = "SELECT r FROM Role r WHERE r.id IN :ids";
       List<Role> roles = entityManager.createQuery(hql, Role.class)
               .setParameter("ids", ids)
               .getResultList();
       logger.info("Encontrados {} errores que coinciden con la id dada", roles.size());
       return roles;
    }
}
