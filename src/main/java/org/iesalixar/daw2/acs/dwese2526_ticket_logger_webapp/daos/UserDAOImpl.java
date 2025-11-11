package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Repository //Anotacion que marca esta clase como un componente que gestiona la persistencia
public class UserDAOImpl implements UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(RegionDAOImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> listAllUsers() throws SQLException {
        logger.info("Listando los usuarios de la base de datos.");
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class));
        logger.info("Retrieved {} users from the database.", users.size());
        return users;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertUser(User user) throws SQLException {
        logger.info("Insertando usuario con id: {} y nombre {}", user.getId(), user.getUsername());
        String sql = "INSERT INTO users (username, passwordHash, active, accountNonLocked, lastPasswordChange, " +
                "passwordExpiresAt, failedLoginAttempts, emailVerified, mustChangePassword) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int rowsAffected = jdbcTemplate.update(sql,
                user.getUsername(),                // username
                user.getPasswordHash(),            // passwordHash
                user.isActive(),                   // active
                user.isAccountNonLocked(),         // accountNonLocked
                user.getLastPasswordChange(),      // lastPasswordChange
                user.getPasswordExpiresAt(),       // passwordExpiresAt
                user.getFailedLoginAttempts(),     // failedLoginAttempts
                user.isEmailVerified(),            // emailVerified
                user.isMustChangePassword()        // mustChangePassword
        );
        logger.info("Usuario insertada. Filas afectadas: {}", rowsAffected);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUser(User user) throws SQLException {
        logger.info("Actualizando usuario por id: {}", user.getId());
        String sql = "UPDATE users SET username = ?, passwordHash = ?, active = ?, accountNonLocked = ?, " +
                "lastPasswordChange = ?, passwordExpiresAt = ?, failedLoginAttempts = ?, " +
                "emailVerified = ?, mustChangePassword = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
                user.getUsername(),                // username
                user.getPasswordHash(),            // passwordHash
                user.isActive(),                   // active
                user.isAccountNonLocked(),         // accountNonLocked
                user.getLastPasswordChange(),      // lastPasswordChange
                user.getPasswordExpiresAt(),       // passwordExpiresAt
                user.getFailedLoginAttempts(),     // failedLoginAttempts
                user.isEmailVerified(),            // emailVerified
                user.isMustChangePassword(),       // mustChangePassword
                user.getId()
        );
        logger.info("Usuario actualizado. Filas afectadas: {}", rowsAffected);
    }


    @Override
    public void deleteUser(Long id) throws SQLException {
        logger.info("Borrando regiones con id: {}", id);
        String sql = "DELETE FROM users WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        logger.info("Usuario eliminado. Filas afectadas: {}", rowsAffected);
    }


    @Override
    public User getUserById(Long id) throws SQLException {
        logger.info("Recogiendo region por id: {}", id);
        String sql = "SELECT * FROM users WHERE id = ?";
        try  {
            User user = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), id);
            logger.info("Usuario recogido: {} - {}", user.getId(),user.getUsername());
            return user;
        }
        catch (Exception e) {
            logger.warn("No se encontro usuario con id: {}", id);
            return null;
        }
    }


    @Override
    public boolean existsUserByName(String username) throws SQLException {
       logger.info("Comprobando si el usuario con nombre: {} existe", username);
       String sql = "SELECT COUNT(*) FROM users WHERE UPPER(username) = ?";
       Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username.toUpperCase());
       boolean exists = count != null && count > 0;
       logger.info("Usuario con codigo: {} exiiste: {}", username, exists);
       return exists;
    }


    @Override
    public boolean existUserByNameAndNotId(String username, Long id) throws SQLException {
        logger.info("Comprobando si el usuario con nombre: {} existe excluyendo la id: {}", username, id);
        String sql = "SELECT COUNT(*) FROM users WHERE UPPER(username) = ? AND ID != ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username.toUpperCase(), id);
        boolean exists = count != null && count > 0;
        logger.info("Usuario con codigo: {} existe excluyendo al id {}: {}", username, id, exists);
        return exists;
    }
}
