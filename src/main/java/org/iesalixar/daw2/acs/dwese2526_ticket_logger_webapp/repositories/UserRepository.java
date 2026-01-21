package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.repositories;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz para operaciones CRUD sobre la entidad {@link User}.
 * Define los métodos necesarios para listar, insertar, actualizar, eliminar y consultar usuarios.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

}
