package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.User;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz para operaciones CRUD sobre la entidad {@link User}.
 * Define los métodos necesarios para listar, insertar, actualizar, eliminar y consultar usuarios.
 */
public interface UserDAO {

    List<User> listAllUsers();
    List<User> listUserPage(int page, int size, String sortField, String sortDir);
    long countUsers();
    void insertUser(User user);
    void updateUser(User user);
    void deleteUser(Long id);
    User getUserById(Long id);
    boolean existsUserByEmail(String email);
    boolean existUserByEmailAndNotId(String email, Long id);
    User getUserByEmail(String email);
}
