package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos.UserDAO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos.UserProfileDAO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos.UserProfileDAOImpl;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.UserProfileFormDTO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.User;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.UserProfile;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers.UserProfileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.naming.Binding;
import java.util.Locale;

@Controller
@RequestMapping("/profile")
public class UserProfileController {
    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserProfileDAO userProfileDAO;

    @GetMapping("/edit")
    public String showProfileForm(Model model, Locale locale){
        final String fixedEmail = "admin@app.local";
        logger.info("Mostrando forulario de perfil para el usuario fijo {}", fixedEmail);
        //1. Cargar la entidad User por email fijo
        User user = userDAO.getUserByEmail(fixedEmail);
        if(user == null) {
            logger.warn("No se encontro el usuario con email {}", fixedEmail);
            String errorMessage = messageSource.getMessage("msg.user-controller.edit.notfound", null, locale);
            model.addAttribute("errorMessage", errorMessage);
            //Puedes mostrar la misma vista con solo el error
            return "views/user-profile/user-profile-form";
        }
        //2. Cargar el perfil
        UserProfile profile = userProfileDAO.getUserProfileByUserId(user.getId());
        //3. Mapear User + UserProfile ->  DTO de formulario
        UserProfileFormDTO formDTO = UserProfileMapper.toFormDto(user, profile);
        //4. Enviar al modelo
        model.addAttribute("userProfileForm", formDTO);

        return "views/user-profile/user-profile-form";
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("userProfileForm") UserProfileFormDTO profileDto,
                                BindingResult result, RedirectAttributes redirectAttributes, Locale locale){
        logger.info("Actualizando perfil para el usuario con ID {}", profileDto.getUserId());
        //1. Si hay errores de validacion, volvemos al formulario
        if (result.hasErrors()){
            logger.warn("Errores de validacion en el formulario de perfil para userId={}", profileDto.getUserId());
            return "views/user-profile/user-profile-form";
        }
        try {
            Long userId = profileDto.getUserId();
            User user = userDAO.getUserById(userId);
            if (user == null) {
                logger.warn("No se encontro el usuario con ID {}", userId);
                String errorMessage = messageSource.getMessage("msg.user-controller.edit.notfound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/profile/edit";
            }
            //3. Cargar perfil existente (si lo hay)
            UserProfile profile = userProfileDAO.getUserProfileByUserId(userId);
            boolean isNew = (profile == null);
            if (isNew) {
                //Crear un nuevo perfil a partir del DTO y el User
                profile = UserProfileMapper.toNewEntity(profileDto, user);
            }
            else {
                //Actualizar el perfil existente con los datos del DTO
                UserProfileMapper.copyToExistingEntity(profileDto, profile);
            }
                //4. Guardar (insert/update) usando el DAO
                userProfileDAO.saveOrUpdateUserProfile(profile);
                //5. Mensaje de exito
                String successMessage = messageSource.getMessage("msg.userProfile.success", null, locale);
                redirectAttributes.addFlashAttribute("successMessage", successMessage);
            }
        catch (Exception e) {
            logger.error("Error al actualizar el perfil del usuario con ID {}: {}",
                    profileDto.getUserId(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.userProfile.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        // 6. Redirigir la vuelta al formulario de perfil
        return "redirect:/profile/edit";
    }

}
