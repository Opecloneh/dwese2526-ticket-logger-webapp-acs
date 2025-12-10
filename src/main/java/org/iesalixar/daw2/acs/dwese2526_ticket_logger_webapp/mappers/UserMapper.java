package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers;

import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.*;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Role;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.User;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.UserProfile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {
    //Entity -> DTO (listado/tabla basico)
    public static UserDTO toDTO(User entity) {
        if (entity == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setActive(entity.getActive());
        dto.setEmailVerified(entity.isEmailVerified());
        dto.setAccountNonLocked(entity.getAccountNonLocked());
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
        dto.setEmail(entity.getEmail());
        dto.setActive(entity.getActive());
        dto.setEmailVerified(entity.isEmailVerified());
        dto.setAccountNonLocked(entity.getAccountNonLocked());
        dto.setFailedLoginAttempts(entity.getFailedLoginAttempts());
        dto.setLastPasswordChange(entity.getLastPasswordChange());
        dto.setMustChangePassword(entity.isMustChangePassword());
        dto.setPasswordExpiresAt(entity.getPasswordExpiresAt());
        dto.setPasswordHash(entity.getPasswordHash());

        if (entity.getRoles() != null) {
            Set<Long> rolesIds = entity.getRoles().stream()
                    .map(Role::getId)
                    .collect(Collectors.toSet());
            dto.setRoleIds(rolesIds);
        }

        return dto;
    }
    public static User toEntity(UserCreateDTO dto) {
        if (dto == null) return null;
        User u = new User();
        u.setId(dto.getId());
        u.setActive(dto.isActive());
        u.setEmail(dto.getEmail());
        u.setEmailVerified(dto.isEmailVerified());
        u.setAccountNonLocked(dto.isAccountNonLocked());
        u.setFailedLoginAttempts(dto.getFailedLoginAttempts());
        u.setLastPasswordChange(dto.getLastPasswordChange());
        u.setMustChangePassword(dto.isMustChangePassword());
        u.setPasswordExpiresAt(dto.getPasswordExpiresAt());
        u.setPasswordHash(dto.getPasswordHash());
        return u;
    }
    public static User toEntity(UserUpdateDTO dto) {
        if (dto == null) return null;
        User u = new User();
        u.setId(dto.getId());
        u.setActive(dto.isActive());
        u.setEmail(dto.getEmail());
        u.setEmailVerified(dto.isEmailVerified());
        u.setAccountNonLocked(dto.isAccountNonLocked());
        u.setFailedLoginAttempts(dto.getFailedLoginAttempts());
        u.setLastPasswordChange(dto.getLastPasswordChange());
        u.setMustChangePassword(dto.isMustChangePassword());
        u.setPasswordExpiresAt(dto.getPasswordExpiresAt());
        u.setPasswordHash(dto.getPasswordHash());
        return u;
    }
    public static User toEntity(UserCreateDTO dto, Set<Role> roles) {
        if (dto == null) return null;

        User e = toEntity(dto);
        e.setRoles(roles);
        return e;
    }
    public static User toEntity(UserUpdateDTO dto, Set<Role> roles) {
        if (dto == null) return null;

        User e = toEntity(dto);
        e.setRoles(roles);
        return e;
    }
    public static void copyToExistingEntity(UserUpdateDTO dto, User entity){
        if (dto == null || entity == null) return;
        entity.setActive(dto.isActive());
        entity.setEmail(dto.getEmail());
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
        dto.setEmail(entity.getEmail());
        dto.setId(entity.getId());
        dto.setPasswordHash(entity.getPasswordHash());
        dto.setActive(entity.getActive());
        dto.setAccountNonLocked(entity.getAccountNonLocked());
        dto.setLastPasswordChange(entity.getLastPasswordChange());
        dto.setPasswordExpiresAt(entity.getPasswordExpiresAt());
        dto.setFailedLoginAttempts(entity.getFailedLoginAttempts());
        dto.setEmailVerified(entity.isEmailVerified());
        dto.setMustChangePassword(entity.isMustChangePassword());

        //Cargar datos del perfil si existe
        UserProfile profile = entity.getProfile();

        if (profile != null) {
            dto.setFirstName(profile.getFirstName());
            dto.setLastName(profile.getLastName());
            dto.setPhoneNumber(profile.getPhoneNumber());
            dto.setProfileImage(profile.getProfileImage());
            dto.setBio(profile.getBio());
            dto.setLocale(profile.getLocale());
        }

        return dto;
    }


}
