package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.repositories.UserRepository;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.repositories.UserProfileRepository;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.UserProfileFormDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.User;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.UserProfile;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers.UserProfileMapper;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services.FileStorageService;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services.UserProfileService;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services.UserProfileServiceImpl;
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

import java.security.Principal;
import java.util.Locale;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);
    private static final long MAX_PROFILE_IMAGE_SIZE = 2 * 1024 * 1024; // 2MB
    
    @Autowired
    private UserProfileService userProfileService;
    
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/edit")
    public String showProfileForm(Model model, Locale locale, Principal principal) {
        String email = principal.getName();
        logger.info("Mostrando formulario de perfil para el usuario fijo {}", email);

        try{
            UserProfileFormDTO formDto = userProfileService.getFormByEmail(email);
            model.addAttribute("userProfileForm", formDto);
            return "views/user-profile/user-profile-form";
        }
        catch (ResourceNotFoundException ex){
            logger.warn("No se encontró el usuario con email {}", ex.getMessage());
            String errorMessage = messageSource.getMessage("msg.user-controller.edit.notfound", null, locale);
            model.addAttribute("errorMessage", errorMessage);
            return "views/user-profile/user-profile-form";
        }
        catch (Exception ex){
            logger.warn("Error inesperado cargando el formulario de perfil: {}", ex.getMessage(), ex);
            String errorMessage = messageSource.getMessage("msg.userProfile.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
            return "views/user-profile/user-profile-form";
        }
    }

    @PostMapping("/update")
    public String updateProfile(
            @Valid @ModelAttribute("userProfileForm") UserProfileFormDTO profileDto,
            BindingResult result,
            @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile,
            RedirectAttributes redirectAttributes,
            Locale locale,
            Principal principal) {

        String email = principal.getName();
        logger.info("Actualizando perfil para email={}", email);

        if (result.hasErrors()) {
            logger.warn("Errores de validación en el formulario de perfil para userId={}", profileDto.getUserId());
            return "views/user-profile/user-profile-form";
        }

        try {
            // 2) Delegar logica de negocio en el service
            userProfileService.updateProfile(email, profileDto, profileImageFile);
            // 3) Mensaje de exito
            String successMessage = messageSource.getMessage("msg.userProfile.success", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch(ResourceNotFoundException ex){
            logger.warn("No se pudo actualizar el perfil porque falta un recurso: {}", ex.getMessage());
            String errorMessage = messageSource.getMessage("msg.user-controller.edit.notfound", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        } catch (Exception ex) {
            logger.error("Error al actualizar el perfil del usuario con ID {}: {}", profileDto.getUserId(), ex.getMessage(), ex);
            String errorMessage = messageSource.getMessage("msg.userProfile.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        // 4) Redirect siempre al formulario (patron PRG)
        return "redirect:/profile/edit";
    }
}
