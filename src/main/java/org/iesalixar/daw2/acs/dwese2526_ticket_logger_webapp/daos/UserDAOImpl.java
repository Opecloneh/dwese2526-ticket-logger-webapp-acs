package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Implementación de {@link UserDAO} utilizando Spring JdbcTemplate.
 * Proporciona operaciones CRUD sobre la entidad {@link User}.
 */
@Repository
@Transactional
public class UserDAOImpl implements UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    @PersistenceContext
    private EntityManager entityManager;
    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> listAllUsers()  {
        logger.info("Listando los usuarios de la base de datos.");
        String hql = "SELECT u FROM User u";
        List<User> users = entityManager.createQuery(hql, User.class).getResultList();
        logger.info("Recuperados {} usuarios de la base de datos.", users.size());
        return users;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertUser(User user)  {
        logger.info("Insertando usuario con id: {} y nombre {}", user.getId(), user.getEmail());

        if (user.getLastPasswordChange() != null) {
            user.setPasswordExpiresAt(user.getLastPasswordChange().plusMonths(3));
        }
        entityManager.persist(user);
        logger.info("Usuario con id {} insertado con exito", user.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUser(User user)  {
        logger.info("Actualizando usuario con id: {}", user.getId());

        if (user.getLastPasswordChange() != null) {
            user.setPasswordExpiresAt(user.getLastPasswordChange().plusMonths(3));
        }
        entityManager.merge(user);
        logger.info("Usuario con id {} actualizado", user.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUser(Long id)  {
        logger.info("Borrando usuario con id: {}", id);
        User user = entityManager.find(User.class, id);
        if (user != null) {
            entityManager.remove(user);
            logger.info("Usuario eliminado con sesion: {}", id);
        }
        logger.info("Usuario con id {} eliminado", user.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserById(Long id)  {
        logger.info("Recuperando usuario por id: {}", id);
        User user = entityManager.find(User.class, id);
        if (user != null) {
            logger.info("Usuario recuperada: {} - {}", id, user.getEmail());
        }
        else {
            logger.warn("Usuario con id {} no encontrado", id);
        }
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsUserByEmail(String email) {
        logger.info("Comprobando si el usuario con email '{}' existe", email);
        String hql = "SELECT COUNT(u) FROM User u WHERE UPPER(u.email) = :email";
        Long count = entityManager.createQuery(hql, Long.class)
                .setParameter("email", email.toUpperCase())
                .getSingleResult();
        boolean exists = count != null && count > 0;
        logger.info("Usuario con email '{}' existe: {}", email, exists);
        return exists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existUserByEmailAndNotId(String email, Long id)  {
        logger.info("Comprobando si el usuario con email '{}' existe excluyendo la id: {}", email, id);
        String hql = "SELECT COUNT(u) FROM User u WHERE UPPER(u.email) = :email AND id != :id";
       Long count = entityManager.createQuery(hql, Long.class)
               .setParameter("email", email.toUpperCase())
               .setParameter("id", id)
               .getSingleResult();
        boolean exists = count != null && count > 0;
        logger.info("Usuario con nombre '{}' existe excluyendo id {}: {}", email, id, exists);
        return exists;
    }
    @Override
    public List<User> listUserPage(int page, int size, String sortField, String sortDir) {
        logger.info("Listing users page={}, size={} from the database.", page, size);

        int offset = page * size;

        String hql  = "SELECT u FROM User u ORDER BY u.email";
        return entityManager.createQuery(hql, User.class)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }
    @Override
    public long countUsers() {
        String hql = "SELECT COUNT(u) FROM User u";
        Long total = entityManager.createQuery(hql, Long.class).getSingleResult();
        return (total != null) ? total : 0L;
    }
    @Override
    public User getUserByEmail(String email) {
        if (email == null) return null;

        String jpql = "SELECT u FROM User u WHERE u.email = :email";
        return entityManager.createQuery(jpql, User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}
