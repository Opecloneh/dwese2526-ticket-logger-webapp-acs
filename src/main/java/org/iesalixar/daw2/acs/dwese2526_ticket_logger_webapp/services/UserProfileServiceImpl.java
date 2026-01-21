package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services;

import jakarta.transaction.Transactional;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.UserProfileFormDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.User;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.UserProfile;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.exceptions.InvalidFileException;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers.UserProfileMapper;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.repositories.UserProfileRepository;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileServiceImpl.class);

    private static final long MAX_IMAMGE_SIZE_BYTES = 2 * 1024 * 1024;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private FileStorageService fileStorageService;


    @Override
    public UserProfileFormDTO getFormByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("user", "email", email));
        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(user.getId());
        UserProfile profile = profileOpt.orElse(null);

        return UserProfileMapper.toFormDto(user, profile);
    }

    @Override
    public void updateProfile(String email, UserProfileFormDTO profileDto, MultipartFile profileImageFile) {

    logger.info("Actualizando perfil para email={}", email);

    // 1) Comprobar que existe el User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("user", "email", email));

        Long userId = user.getId();

        // 2) Cargar perfil (puede no existir)
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        boolean isNew = (profile == null);

        // 3) Si hay imagen nueva, validar + guardar + borrar anterior
        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            validateProfileImage(profileImageFile);

            String oldImagePath = fileStorageService.saveFile(profileImageFile);

            String newImageWebPath = fileStorageService.saveFile(profileImageFile);
            if (newImageWebPath == null || newImageWebPath.isBlank()) {
                throw new InvalidFileException(
                        "userProfile",
                        "profileImageFile",
                        profileImageFile.getOriginalFilename(),
                        "No se pudo guardar la imagen de perfil"
                );
            }
            profileDto.setProfileImage(newImageWebPath);

            //Borrar anterior si existia
            if (oldImagePath != null & !oldImagePath.isBlank()) {
                fileStorageService.deleteFile(oldImagePath);
            }
        }
        // 4) Crear o actualizar entidad de perfil
        if (isNew) {
            profile = UserProfileMapper.toNewEntity(profileDto, user);
        }
        else {
            UserProfileMapper.copyToExistingEntity(profileDto, profile);
        }

        // 5) Persistir
        userProfileRepository.save(profile);
    }

    private void validateProfileImage(MultipartFile file) {
        String contentType = file.getContentType();
        // MIME invalido
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileException(
                    "userProfile",
                    "profileImageFile",
                    contentType,
                    "Tipo de archivo no permitido"
            );
        }
        // Tamaño excedido
        if (file.getSize() > MAX_IMAMGE_SIZE_BYTES) {
            throw new InvalidFileException(
                    "userProfile",
                    "profileImageFile",
                    file.getSize(),
                    "Archivo demasiado grande (maximo " + MAX_IMAMGE_SIZE_BYTES + " bytes)"
            );
        }
    }
}
