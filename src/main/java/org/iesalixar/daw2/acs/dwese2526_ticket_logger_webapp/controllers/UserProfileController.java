package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.repositories.UserRepository;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.repositories.UserProfileRepository;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.UserProfileFormDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.User;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.UserProfile;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers.UserProfileMapper;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);
    private static final long MAX_PROFILE_IMAGE_SIZE = 2 * 1024 * 1024; // 2MB

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/edit")
    public String showProfileForm(Model model, Locale locale) {
        final String fixedEmail = "admin@app.local";
        logger.info("Mostrando formulario de perfil para el usuario fijo {}", fixedEmail);

        Optional<User> userOpt = userRepository.findByEmail(fixedEmail);
        if (userOpt.isEmpty()) {
            logger.warn("No se encontró el usuario con email {}", fixedEmail);
            String errorMessage = messageSource.getMessage("msg.user-controller.edit.notfound", null, locale);
            model.addAttribute("errorMessage", errorMessage);
            return "views/user-profile/user-profile-form";
        }

        User user = userOpt.get();

        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(user.getId());
        UserProfileFormDTO formDTO = UserProfileMapper.toFormDto(user, profileOpt.orElse(null));
        model.addAttribute("userProfileForm", formDTO);

        return "views/user-profile/user-profile-form";
    }

    @PostMapping("/update")
    public String updateProfile(
            @Valid @ModelAttribute("userProfileForm") UserProfileFormDTO profileDto,
            BindingResult result,
            @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        logger.info("Actualizando perfil para el usuario con ID {}", profileDto.getUserId());

        if (result.hasErrors()) {
            logger.warn("Errores de validación en el formulario de perfil para userId={}", profileDto.getUserId());
            return "views/user-profile/user-profile-form";
        }

        try {
            Optional<User> userOpt = userRepository.findById(profileDto.getUserId());
            if (userOpt.isEmpty()) {
                logger.warn("No se encontró el usuario con ID {}", profileDto.getUserId());
                String errorMessage = messageSource.getMessage("msg.user-controller.edit.notfound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/profile/edit";
            }
            User user = userOpt.get();

            Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(user.getId());
            UserProfile profile = profileOpt.orElse(null);
            boolean isNew = (profile == null);

            // Gestión de imagen subida
            if (profileImageFile != null && !profileImageFile.isEmpty()) {
                logger.info("Se ha subido un nuevo archivo de imagen para el perfil del usuario {}", user.getId());

                String contentType = profileImageFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    logger.warn("Archivo de tipo no permitido: {}", contentType);
                    String msg = messageSource.getMessage("msg.userProfile.image.invalidType", null, locale);
                    redirectAttributes.addFlashAttribute("errorMessage", msg);
                    return "redirect:/profile/edit";
                }

                if (profileImageFile.getSize() > MAX_PROFILE_IMAGE_SIZE) {
                    logger.warn("Archivo demasiado grande: {} bytes (límite {} bytes)", profileImageFile.getSize(), MAX_PROFILE_IMAGE_SIZE);
                    String msg = messageSource.getMessage("msg.userProfile.image.tooLarge", null, locale);
                    redirectAttributes.addFlashAttribute("errorMessage", msg);
                    return "redirect:/profile/edit";
                }

                String oldImagePath = profileDto.getProfileImage();

                String newImageWebPath = fileStorageService.saveFile(profileImageFile);
                if (newImageWebPath == null) {
                    logger.error("No se pudo guardar la nueva imagen de perfil para el usuario {}", user.getId());
                    String msg = messageSource.getMessage("msg.userProfile.image.saveError", null, locale);
                    redirectAttributes.addFlashAttribute("errorMessage", msg);
                    return "redirect:/profile/edit";
                }
                logger.info("Nueva imagen de perfil guardada en {}", newImageWebPath);
                profileDto.setProfileImage(newImageWebPath);

                if (oldImagePath != null && !oldImagePath.isBlank()) {
                    logger.info("Eliminando imagen anterior de perfil: {}", oldImagePath);
                    fileStorageService.deleteFile(oldImagePath);
                }
            }

            // Crear o actualizar perfil
            if (isNew) {
                profile = UserProfileMapper.toNewEntity(profileDto, user);
            } else {
                UserProfileMapper.copyToExistingEntity(profileDto, profile);
            }

            userProfileRepository.save(profile);

            String successMessage = messageSource.getMessage("msg.userProfile.success", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);

        } catch (Exception e) {
            logger.error("Error al actualizar el perfil del usuario con ID {}: {}", profileDto.getUserId(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.userProfile.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }

        return "redirect:/profile/edit";
    }
}
