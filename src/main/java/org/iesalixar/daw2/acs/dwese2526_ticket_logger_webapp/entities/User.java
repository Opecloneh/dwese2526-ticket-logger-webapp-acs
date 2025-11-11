package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Clase que representa un usuario del sistema.
 * Contiene información de autenticación, estado de cuenta y control de contraseñas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /** Identificador único del usuario. */
    private Long id;

    /** Nombre de usuario único para autenticación. */
    private String username;

    /** Hash de la contraseña del usuario. */
    private String passwordHash;

    /** Indica si la cuenta está activa. */
    private boolean active;

    /** Indica si la cuenta no está bloqueada. */
    private boolean accountNonLocked;

    /** Fecha y hora del último cambio de contraseña. */
    private LocalDateTime lastPasswordChange;

    /** Fecha y hora en que expira la contraseña. */
    private LocalDateTime passwordExpiresAt;

    /** Número de intentos fallidos de inicio de sesión. */
    private int failedLoginAttempts;

    /** Indica si el email del usuario ha sido verificado. */
    private boolean emailVerified;

    /** Indica si el usuario debe cambiar la contraseña en el próximo inicio de sesión. */
    private boolean mustChangePassword;

    /**
     * Constructor simplificado para crear un usuario con solo ID, nombre de usuario y hash de contraseña.
     *
     * @param id Identificador del usuario.
     * @param username Nombre de usuario.
     * @param passwordHash Hash de la contraseña.
     */
    public User(Long id, String username, String passwordHash) {
        this.id = id;
        this.passwordHash = passwordHash;
        this.username = username;
    }

    /**
     * Constructor completo para crear un usuario con todos los campos excepto el ID.
     * Se utiliza normalmente antes de insertar un usuario en la base de datos.
     *
     * @param username Nombre de usuario.
     * @param passwordHash Hash de la contraseña.
     * @param active Indica si la cuenta está activa.
     * @param accountNonLocked Indica si la cuenta no está bloqueada.
     * @param lastPasswordChange Fecha del último cambio de contraseña.
     * @param passwordExpiresAt Fecha de expiración de la contraseña.
     * @param failedLoginAttempts Número de intentos fallidos de inicio de sesión.
     * @param emailVerified Indica si el email ha sido verificado.
     * @param mustChangePassword Indica si el usuario debe cambiar la contraseña.
     */
    public User(String username, String passwordHash, boolean active, boolean accountNonLocked,
                LocalDateTime lastPasswordChange, LocalDateTime passwordExpiresAt,
                int failedLoginAttempts, boolean emailVerified, boolean mustChangePassword) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.active = active;
        this.accountNonLocked = accountNonLocked;
        this.lastPasswordChange = lastPasswordChange;
        this.passwordExpiresAt = passwordExpiresAt;
        this.failedLoginAttempts = failedLoginAttempts;
        this.emailVerified = emailVerified;
        this.mustChangePassword = mustChangePassword;
    }
}
