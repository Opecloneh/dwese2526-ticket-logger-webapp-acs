package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.User;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz para operaciones CRUD sobre la entidad {@link User}.
 * Define los métodos necesarios para listar, insertar, actualizar, eliminar y consultar usuarios.
 */
public interface UserDAO {

    List<User> listAllUsers() throws SQLException;
    List<User> listUserPage(int page, int size);
    long countUsers();
    void insertUser(User user) throws SQLException;
    void updateUser(User user) throws SQLException;
    void deleteUser(Long id) throws SQLException;
    User getUserById(Long id) throws SQLException;
    boolean existsUserByName(String username) throws SQLException;
    boolean existUserByNameAndNotId(String username, Long id) throws SQLException;

}
