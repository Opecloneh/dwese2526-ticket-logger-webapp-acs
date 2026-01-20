package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Role;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.repositories.RoleRepository;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.repositories.UserRepository;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.*;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.User;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers.UserMapper;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services.UserService;
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
import java.util.*;

/**
 * Controlador Spring MVC para gestionar operaciones CRUD sobre usuarios.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public String listUsers(@PageableDefault(size = 10, sort = "id") Pageable pageable,
                            Model model,
                            Locale locale) {
        logger.info("Solicitando la lista de usuarios... page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<UserDTO> listUsersDTOs = userService.list(pageable);
            logger.info("Se han cargado {} usuarios en la página {}",
                    listUsersDTOs.getNumberOfElements(), listUsersDTOs.getNumber());
            model.addAttribute("page", listUsersDTOs);

            String sortParam = "id,asc";
            if (listUsersDTOs.getSort().isSorted()) {
                var order = listUsersDTOs.getSort().iterator().next();
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
            List<UserDTO> listUsers = userService.listAll();
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

            userService.create(userDTO);
            logger.info("Usuario {} insertada con exito.", userDTO.getEmail());
            return "redirect:/users";

        } catch (DuplicateResourceException ex) {
            logger.warn("El codigo de la user {} ya existe", userDTO.getEmail());
            String errorMessage = messageSource.getMessage("msg.user-controller.insert.codeExist", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/users/new";

        } catch (Exception e) {
            logger.error("Error al insertar el usuario {}: {}", userDTO.getEmail(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.user-controller.insert.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/users/new";
        }

    }


    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id,
                               Model model,
                               Locale locale,
                               RedirectAttributes redirectAttributes) {
        logger.info("Mostrando formulario de edición para usuario con ID {}", id);
        try {
            UserUpdateDTO userDTO = userService.getForEdit(id);
            model.addAttribute("user", userDTO);
            model.addAttribute("allRoles", roleRepository.findAll()); // siempre cargar roles
            return "views/user/user-form";

        } catch (ResourceNotFoundException ex) {
            logger.error("Error al obtener la user con ID {}: {}", id, ex.getMessage());
            String msg = messageSource.getMessage("msg.user.error.notfound", new Object[]{id}, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/users";
        } catch (Exception e) {
            logger.error("Error al obtener la user de ID {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage("msg.user.error.load", new Object[]{id}, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/users";
        }
    }

    @PostMapping("/update")
    public String updateUser(@Valid @ModelAttribute("user") UserUpdateDTO userDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Locale locale,
                             Model model) {
        logger.info("Actualizando usuario con ID {}", userDTO.getId());
        if (userDTO.getRoleIds() != null && userDTO.getRoleIds().isEmpty()) {
            userDTO.setRoleIds(null);
        }

        try {
            if (result.hasErrors()) {
                model.addAttribute("allRoles", roleRepository.findAll());
                return "views/user/user-form";
            }

            // Filtrar nulls de roleIds
            Set<Long> roleIds = new HashSet<>();
            if (userDTO.getRoleIds() != null) {
                for (Long id : userDTO.getRoleIds()) {
                    if (id != null) {
                        roleIds.add(id);
                    }
                }
            }

            // Obtener roles existentes
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));

            if (roles.size() != roleIds.size()) {
                throw new ResourceNotFoundException("role", "ids", roleIds);
            }

            // Llamar a service
            userService.update(userDTO, roles);
            logger.info("Usuario con ID {} actualizado con éxito. Expira el {}", userDTO.getId(), userDTO.getPasswordExpiresAt());
            return "redirect:/users";

        } catch (Exception e) {
            logger.error("Error al actualizar usuario con ID {}: {}", userDTO.getId(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.user-controller.update.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/users/edit?id=" + userDTO.getId();
        }
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Eliminando usuario con ID {}", id);
        try {
            userService.delete(id);

            logger.info("Usuario con ID {} eliminado con exito.", id);
            return "redirect:/users";

        } catch (ResourceNotFoundException ex) {
            logger.error("No se encontro el usuario con ID {}: {}", id, ex.getMessage(), ex);
            String errorMessage = messageSource.getMessage("msg.user-controller.delete.notFound", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redurect:/users";
        }catch (Exception e) {
            logger.error("Error al eliminar usuario con ID {}: {}", id, e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.user-controller.delete.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redurect:/users";
        }
    }

    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Mostrando detalle de usuario con ID {}", id);
        try {
            UserDetailDTO userDTO = userService.getDetail(id);
            model.addAttribute("user", userDTO);
            return "views/user/user-detail";

        } catch (ResourceNotFoundException ex) {
            String errorMessage = messageSource.getMessage("msg.user-controller.detail.notFound", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/users";

        } catch (Exception e) {
            String errorMessage = messageSource.getMessage("msg.user-controller.detail.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/users";
        }
    }
}
