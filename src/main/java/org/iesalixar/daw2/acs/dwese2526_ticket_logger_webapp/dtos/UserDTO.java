package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.UserProfile;

import java.time.LocalDateTime;
import java.util.Set;



/**
 * DTO genérico de lectura para User.
 * Se puede usar tanto para listados como para vistas de detalle simples.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {


    private Long id;


    private String email;


    // En este CRUD inicial tratamos la contraseña como texto plano,
    // mapeando directamente el campo passwordHash de la entidad.
    private String passwordHash;


    private boolean active;


    private boolean accountNonLocked;


    private LocalDateTime lastPasswordChange;


    private LocalDateTime passwordExpiresAt;


    private Integer failedLoginAttempts;


    private boolean emailVerified;


    private boolean mustChangePassword;


    // Roles asociados al usuario (nombres técnicos: ROLE_ADMIN, ROLE_USER, etc.)
    private Set<String> roles;
}
