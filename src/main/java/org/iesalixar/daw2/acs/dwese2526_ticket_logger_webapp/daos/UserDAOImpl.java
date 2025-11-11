package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos;

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
public class UserDAOImpl implements UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor que recibe el {@link JdbcTemplate} inyectado por Spring.
     *
     * @param jdbcTemplate objeto JdbcTemplate para ejecutar consultas SQL.
     */
    public UserDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> listAllUsers() throws SQLException {
        logger.info("Listando los usuarios de la base de datos.");
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class));
        logger.info("Recuperados {} usuarios de la base de datos.", users.size());
        return users;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertUser(User user) throws SQLException {
        logger.info("Insertando usuario con id: {} y nombre {}", user.getId(), user.getUsername());

        if (user.getLastPasswordChange() != null) {
            user.setPasswordExpiresAt(user.getLastPasswordChange().plusMonths(3));
        }

        String sql = "INSERT INTO users (username, passwordHash, active, accountNonLocked, lastPasswordChange, " +
                "passwordExpiresAt, failedLoginAttempts, emailVerified, mustChangePassword) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int rowsAffected = jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPasswordHash(),
                user.isActive(),
                user.isAccountNonLocked(),
                user.getLastPasswordChange(),
                user.getPasswordExpiresAt(),
                user.getFailedLoginAttempts(),
                user.isEmailVerified(),
                user.isMustChangePassword()
        );
        logger.info("Usuario insertado. Filas afectadas: {}", rowsAffected);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUser(User user) throws SQLException {
        logger.info("Actualizando usuario con id: {}", user.getId());

        if (user.getLastPasswordChange() != null) {
            user.setPasswordExpiresAt(user.getLastPasswordChange().plusMonths(3));
        }

        String sql = "UPDATE users SET username = ?, passwordHash = ?, active = ?, accountNonLocked = ?, " +
                "lastPasswordChange = ?, passwordExpiresAt = ?, failedLoginAttempts = ?, " +
                "emailVerified = ?, mustChangePassword = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPasswordHash(),
                user.isActive(),
                user.isAccountNonLocked(),
                user.getLastPasswordChange(),
                user.getPasswordExpiresAt(),
                user.getFailedLoginAttempts(),
                user.isEmailVerified(),
                user.isMustChangePassword(),
                user.getId()
        );
        logger.info("Usuario actualizado. Filas afectadas: {}", rowsAffected);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUser(Long id) throws SQLException {
        logger.info("Borrando usuario con id: {}", id);
        String sql = "DELETE FROM users WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        logger.info("Usuario eliminado. Filas afectadas: {}", rowsAffected);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserById(Long id) throws SQLException {
        logger.info("Recuperando usuario por id: {}", id);
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), id);
            logger.info("Usuario recuperado: {} - {}", user.getId(), user.getUsername());
            return user;
        } catch (Exception e) {
            logger.warn("No se encontró usuario con id: {}", id);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsUserByName(String username) throws SQLException {
        logger.info("Comprobando si el usuario con nombre '{}' existe", username);
        String sql = "SELECT COUNT(*) FROM users WHERE UPPER(username) = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username.toUpperCase());
        boolean exists = count != null && count > 0;
        logger.info("Usuario con nombre '{}' existe: {}", username, exists);
        return exists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existUserByNameAndNotId(String username, Long id) throws SQLException {
        logger.info("Comprobando si el usuario con nombre '{}' existe excluyendo la id: {}", username, id);
        String sql = "SELECT COUNT(*) FROM users WHERE UPPER(username) = ? AND id != ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username.toUpperCase(), id);
        boolean exists = count != null && count > 0;
        logger.info("Usuario con nombre '{}' existe excluyendo id {}: {}", username, id, exists);
        return exists;
    }
}
