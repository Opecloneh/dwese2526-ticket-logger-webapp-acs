package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.*;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.User;

import java.util.List;

public class UserMapper {
    //Entity -> DTO (listado/tabla basico)
    public static UserDTO toDTO(User entity) {
        if (entity == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setActive(entity.isActive());
        dto.setUsername(entity.getUsername());
        dto.setEmailVerified(entity.isEmailVerified());
        dto.setAccountNonLocked(entity.isAccountNonLocked());
        dto.setFailedLoginAttempts(entity.getFailedLoginAttempts());
        dto.setLastPasswordChange(entity.getLastPasswordChange());
        dto.setMustChangePassword(entity.isMustChangePassword());
        dto.setPasswordExpiresAt(entity.getPasswordExpiresAt());
        dto.setPasswordHash(entity.getPasswordHash());
        return dto;
    }
    public static List<UserDTO> toDTOList(List<User> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(UserMapper::toDTO).toList();
    }
    //Update
    public static UserUpdateDTO toUpdateDTO(User entity) {
        if (entity == null) return null;
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(entity.getId());
        dto.setActive(entity.isActive());
        dto.setUsername(entity.getUsername());
        dto.setEmailVerified(entity.isEmailVerified());
        dto.setAccountNonLocked(entity.isAccountNonLocked());
        dto.setFailedLoginAttempts(entity.getFailedLoginAttempts());
        dto.setLastPasswordChange(entity.getLastPasswordChange());
        dto.setMustChangePassword(entity.isMustChangePassword());
        dto.setPasswordExpiresAt(entity.getPasswordExpiresAt());
        dto.setPasswordHash(entity.getPasswordHash());
        return dto;
    }
    public static User toEntity(UserCreateDTO dto) {
        if (dto == null) return null;
        User u = new User();
        u.setId(dto.getId());
        u.setActive(dto.isActive());
        u.setUsername(dto.getUsername());
        u.setEmailVerified(dto.isEmailVerified());
        u.setAccountNonLocked(dto.isAccountNonLocked());
        u.setFailedLoginAttempts(dto.getFailedLoginAttempts());
        u.setLastPasswordChange(dto.getLastPasswordChange());
        u.setMustChangePassword(dto.isMustChangePassword());
        u.setPasswordExpiresAt(dto.getPasswordExpiresAt());
        u.setPasswordHash(dto.getPasswordHash());
        return u;
    }
    public static void copyToExistingEntity(UserUpdateDTO dto, User entity){
        if (dto == null || entity == null) return;
        entity.setActive(dto.isActive());
        entity.setUsername(dto.getUsername());
        entity.setEmailVerified(dto.isEmailVerified());
        entity.setAccountNonLocked(dto.isAccountNonLocked());
        entity.setFailedLoginAttempts(dto.getFailedLoginAttempts());
        entity.setLastPasswordChange(dto.getLastPasswordChange());
        entity.setMustChangePassword(dto.isMustChangePassword());
        entity.setPasswordExpiresAt(dto.getPasswordExpiresAt());
        entity.setPasswordHash(dto.getPasswordHash());
    }
    public static UserDetailDTO toDetailDTO(User entity) {
        if (entity == null) return null;

        UserDetailDTO dto = new UserDetailDTO();
        dto.setUsername(entity.getUsername());
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setPasswordHash(entity.getPasswordHash());
        dto.setActive(entity.isActive());
        dto.setAccountNonLocked(entity.isAccountNonLocked());
        dto.setLastPasswordChange(entity.getLastPasswordChange());
        dto.setPasswordExpiresAt(entity.getPasswordExpiresAt());
        dto.setFailedLoginAttempts(entity.getFailedLoginAttempts());
        dto.setEmailVerified(entity.isEmailVerified());
        dto.setMustChangePassword(entity.isMustChangePassword());

        return dto;
    }


}
