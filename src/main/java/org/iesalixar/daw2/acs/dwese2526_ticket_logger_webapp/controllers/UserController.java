package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.repositories.RoleRepository;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.repositories.UserRepository;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.*;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.User;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;

/**
 * Controlador Spring MVC para gestionar operaciones CRUD sobre usuarios.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final int PASSWORD_EXPIRY_DAYS = 90;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public String listUsers(@PageableDefault(size = 10, sort = "id") Pageable pageable,
                            Model model,
                            Locale locale) {
        logger.info("Solicitando la lista de usuarios... page={}, size={}, sort={}", pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<UserDTO> pageUsers = userRepository.findAll(pageable).map(UserMapper::toDTO);
            logger.info("Se han cargado {} usuarios en la página {}", pageUsers.getNumberOfElements(), pageUsers.getNumber());
            model.addAttribute("page", pageUsers);

            String sortParam = "id,asc";
            if (pageUsers.getSort().isSorted()) {
                var order = pageUsers.getSort().iterator().next();
                sortParam = order.getProperty() + "," + order.getDirection().name().toLowerCase();
            }
            model.addAttribute("sortParam", sortParam);

        } catch (Exception e) {
            logger.error("Error al listar los usuarios: {}", e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.user-controller.list.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
        }
        return "views/user/user-list";
    }

    @GetMapping("/new")
    public String showNewForm(Model model, Locale locale) {
        logger.info("Mostrando formulario para nuevo usuario.");
        try {
            model.addAttribute("user", new UserCreateDTO());
            model.addAttribute("allRoles", roleRepository.findAll());
        } catch (Exception e) {
            logger.error("Error al cargar roles para nuevo usuario: {}", e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.user-controller.form.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
        }
        return "views/user/user-form";
    }

    @PostMapping("/insert")
    public String insertUser(@Valid @ModelAttribute("user") UserCreateDTO userDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Locale locale,
                             Model model) {
        logger.info("Insertando nuevo usuario con email {}", userDTO.getEmail());
        try {
            if (result.hasErrors()) {
                model.addAttribute("allRoles", roleRepository.findAll());
                return "views/user/user-form";
            }
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                logger.warn("El email {} ya existe.", userDTO.getEmail());
                String errorMessage = messageSource.getMessage("msg.user-controller.insert.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users/new";
            }
            LocalDateTime lastPasswordChange = Optional.ofNullable(userDTO.getLastPasswordChange()).orElse(LocalDateTime.now());
            userDTO.setLastPasswordChange(lastPasswordChange);
            userDTO.setPasswordExpiresAt(lastPasswordChange.plusDays(PASSWORD_EXPIRY_DAYS));

            var roles = new HashSet<>(roleRepository.findAllById(userDTO.getRoleIds()));
            User user = UserMapper.toEntity(userDTO, roles);
            userRepository.save(user);
            logger.info("Usuario {} insertado con éxito.", user.getEmail());

        } catch (Exception e) {
            logger.error("Error al insertar el usuario {}: {}", userDTO.getEmail(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.user-controller.insert.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/users";
    }

    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id,
                               Model model,
                               Locale locale,
                               RedirectAttributes redirectAttributes) {
        logger.info("Mostrando formulario de edición para usuario con ID {}", id);
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                logger.warn("No se encontró el usuario con ID {}", id);
                String errorMessage = messageSource.getMessage("msg.user-controller.edit.notfound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users";
            }
            UserUpdateDTO userDTO = UserMapper.toUpdateDTO(userOpt.get());
            model.addAttribute("user", userDTO);
            model.addAttribute("allRoles", roleRepository.findAll());
        } catch (Exception e) {
            logger.error("Error al obtener usuario con ID {}: {}", id, e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.user-controller.edit.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("user", new UserUpdateDTO());
            model.addAttribute("allRoles", roleRepository.findAll());
        }
        return "views/user/user-form";
    }

    @PostMapping("/update")
    public String updateUser(@Valid @ModelAttribute("user") UserUpdateDTO userDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Locale locale,
                             Model model) {
        logger.info("Actualizando usuario con ID {}", userDTO.getId());
        try {
            if (result.hasErrors()) {
                model.addAttribute("allRoles", roleRepository.findAll());
                return "views/user/user-form";
            }
            if (userRepository.existsByEmailAndIdNot(userDTO.getEmail(), userDTO.getId())) {
                logger.warn("El email {} ya existe para otro usuario.", userDTO.getEmail());
                String errorMessage = messageSource.getMessage("msg.user-controller.update.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users/edit?id=" + userDTO.getId();
            }

            Optional<User> userOpt = userRepository.findById(userDTO.getId());
            if (userOpt.isEmpty()) {
                logger.warn("No se encontró el usuario con ID {}", userDTO.getId());
                String errorMessage = messageSource.getMessage("msg.user-controller.edit.notfound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users";
            }

            LocalDateTime lastPasswordChange = Optional.ofNullable(userDTO.getLastPasswordChange()).orElse(LocalDateTime.now());
            userDTO.setLastPasswordChange(lastPasswordChange);
            userDTO.setPasswordExpiresAt(lastPasswordChange.plusDays(PASSWORD_EXPIRY_DAYS));

            var roles = new HashSet<>(roleRepository.findAllById(userDTO.getRoleIds()));
            User user = userOpt.get();
            UserMapper.copyToExistingEntity(userDTO, user, roles);
            userRepository.save(user);
            logger.info("Usuario con ID {} actualizado con éxito. Expira el {}", user.getId(), userDTO.getPasswordExpiresAt());

        } catch (Exception e) {
            logger.error("Error al actualizar usuario con ID {}: {}", userDTO.getId(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.user-controller.update.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/users/edit?id=" + userDTO.getId();
        }
        return "redirect:/users";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Eliminando usuario con ID {}", id);
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                logger.warn("No se encontró el usuario con ID {}", id);
                String errorMessage = messageSource.getMessage("msg.user-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users";
            }
            userRepository.deleteById(id);
            logger.info("Usuario con ID {} eliminado con éxito.", id);
        } catch (Exception e) {
            logger.error("Error al eliminar usuario con ID {}: {}", id, e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.user-controller.delete.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/users";
    }

    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Mostrando detalle de usuario con ID {}", id);
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                String errorMessage = messageSource.getMessage("msg.user-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users";
            }
            UserDetailDTO userDTO = UserMapper.toDetailDTO(userOpt.get());
            model.addAttribute("user", userDTO);
            return "views/user/user-detail";
        } catch (Exception e) {
            logger.error("Error al obtener detalle del usuario con ID {}: {}", id, e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.user-controller.detail.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/users";
        }
    }
}
