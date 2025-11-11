package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.User;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz para operaciones CRUD sobre la entidad {@link User}.
 * Define los métodos necesarios para listar, insertar, actualizar, eliminar y consultar usuarios.
 */
public interface UserDAO {

    /**
     * Lista todos los usuarios registrados en la base de datos.
     *
     * @return Lista de objetos {@link User}.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos.
     */
    List<User> listAllUsers() throws SQLException;

    /**
     * Inserta un nuevo usuario en la base de datos.
     *
     * @param user Usuario a insertar.
     * @throws SQLException Si ocurre un error al insertar el usuario.
     */
    void insertUser(User user) throws SQLException;

    /**
     * Actualiza la información de un usuario existente.
     *
     * @param user Usuario con los datos actualizados.
     * @throws SQLException Si ocurre un error al actualizar el usuario.
     */
    void updateUser(User user) throws SQLException;

    /**
     * Elimina un usuario de la base de datos según su ID.
     *
     * @param id ID del usuario a eliminar.
     * @throws SQLException Si ocurre un error al eliminar el usuario.
     */
    void deleteUser(Long id) throws SQLException;

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id ID del usuario.
     * @return Objeto {@link User} si existe, null si no se encuentra.
     * @throws SQLException Si ocurre un error al consultar la base de datos.
     */
    User getUserById(Long id) throws SQLException;

    /**
     * Verifica si existe un usuario con un nombre de usuario determinado.
     *
     * @param username Nombre de usuario a verificar.
     * @return true si existe un usuario con ese nombre, false en caso contrario.
     * @throws SQLException Si ocurre un error al consultar la base de datos.
     */
    boolean existsUserByName(String username) throws SQLException;

    /**
     * Verifica si existe un usuario con un nombre dado distinto de un ID específico.
     *
     * @param username Nombre de usuario a verificar.
     * @param id ID que se debe excluir de la verificación.
     * @return true si existe otro usuario con ese nombre, false en caso contrario.
     * @throws SQLException Si ocurre un error al consultar la base de datos.
     */
    boolean existUserByNameAndNotId(String username, Long id) throws SQLException;

}
