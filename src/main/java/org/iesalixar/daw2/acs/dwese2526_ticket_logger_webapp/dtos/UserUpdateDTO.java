package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDTO {
    private Long id;

    /** Nombre de usuario único para autenticación. */
    @NotEmpty(message = "{msg.user.email.notEmpty}")
    @Size(max = 50, message = "{msg.user.email.size}")
    private String email;

    /** Hash de la contraseña del usuario. */
    @NotEmpty(message = "{msg.user.passwordHash.notEmpty}")
    @Size(max = 50, message = "{msg.user.passwordHash.size}")
    private String passwordHash;

    /** Indica si la cuenta está activa. */
    private boolean active;

    /** Indica si la cuenta no está bloqueada. */
    private boolean accountNonLocked;

    /** Fecha y hora del último cambio de contraseña. */
    @NotNull(message = "{msg.user.lastPasswordChange.notNull}")
    private LocalDateTime lastPasswordChange;

    /** Fecha y hora en que expira la contraseña. */
    private LocalDateTime passwordExpiresAt;

    /** Número de intentos fallidos de inicio de sesión. */
    private int failedLoginAttempts;

    /** Indica si el email del usuario ha sido verificado. */
    private boolean emailVerified;

    /** Indica si el usuario debe cambiar la contraseña en el próximo inicio de sesión. */
    private boolean mustChangePassword;

    @NotEmpty(message = "{msg.user.roles.notempty}")
    private Set<Long> roleIds = new HashSet<>();

}
