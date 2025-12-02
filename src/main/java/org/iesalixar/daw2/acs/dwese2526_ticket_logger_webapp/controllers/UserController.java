package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos.UserDAO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.*;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Province;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.User;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers.ProvinceMapper;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers.RegionMapper;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

/**
 * Controlador Spring MVC para gestionar operaciones CRUD sobre usuarios.
 * Proporciona métodos para listar, insertar, actualizar y eliminar usuarios.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * DAO para acceder a los datos de los usuarios en la base de datos.
     */
    @Autowired
    private UserDAO userDAO;

    /**
     * Fuente de mensajes internacionalizados.
     */
    @Autowired
    private MessageSource messageSource;

    /**
     * Muestra la lista de todos los usuarios.
     *
     * @param model Modelo de Spring para pasar datos a la vista.
     * @return Nombre de la vista que renderiza la lista de usuarios.
     */
    @GetMapping
    public String listUsers(@RequestParam(name = "page", defaultValue = "0") int page,
                            @RequestParam(name="size", defaultValue = "10") int size,
                            Model model,
                            Locale locale) {
        logger.info("Solicitando la lista de todas los usuarios... page={}, size={}", page, size);
        if (page < 0) page = 0;
        if (size < 0) size = 0;
        try {
            long totalElements = userDAO.countUsers();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            if (totalPages > 0 && page >= totalPages) {
                page = totalPages - 1;
            }
            List<User> listUsers = userDAO.listUserPage(page, size);
            List<UserDTO> listUsersDTO = UserMapper.toDTOList(listUsers);
            logger.info("Se han cargado {} usuarios en la pagina {}.", listUsersDTO.size(), page);
            model.addAttribute("listUsers", listUsersDTO);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalElements", totalElements);
        }
        catch (Exception e) {
            logger.error("Error al listar los usuarios: {}", e.getMessage());
            String errorMessage = messageSource.getMessage("msg.user-controller.list.error", null, locale);
            model.addAttribute("errorMessage", "Error al listar los usuarios.");
        }
        return "views/user/user-list";

    }

    /**
     * Muestra el formulario para crear un nuevo usuario.
     *
     * @param model Modelo de Spring para pasar datos a la vista.
     * @return Nombre de la vista con el formulario de nuevo usuario.
     */
    @GetMapping("/new")
    public String showNewForm(Model model, Locale locale) {
        logger.info("Mostrando formulario para nuevo usuario.");
        try {
            List<User> listUsers = userDAO.listAllUsers();
            model.addAttribute("user", new UserCreateDTO());
            model.addAttribute("listUsers", listUsers);
        }
        catch (Exception e) {
            logger.error("Error al cargar los usuarios para el formulario de usuarios: {}", e.getMessage());
            String errorMessage = messageSource.getMessage("msg.user-controller.edit.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
        }
        return "views/user/user-form";
    }

    /**
     * Inserta un nuevo usuario en la base de datos.
     *
     * @param userDTO               Objeto que contiene los datos del formulario.
     * @param result             Resultados de validación del formulario.
     * @param redirectAttributes Atributos para mensajes flash de redirección.
     * @param locale             Localización para mensajes internacionalizados.
     * @return Redirección a la lista de usuarios o al formulario si hay errores.
     */
    @PostMapping("/insert")
    public String insertUser(@Valid @ModelAttribute("user") UserCreateDTO userDTO, BindingResult result,
                             RedirectAttributes redirectAttributes, Locale locale, Model model) {
        logger.info("Insertando nuevo usuario con nombre {}", userDTO.getUsername());
        try {
            if (result.hasErrors()) {
                List<User> listUsers = userDAO.listAllUsers();
                model.addAttribute("listUsers", listUsers);
                return "views/user/user-form";
            }
            if (userDAO.existsUserByName(userDTO.getUsername())) {
                logger.warn("El nombre de usuario {} ya existe.", userDTO.getUsername());
                String errorMessage = messageSource.getMessage("msg.user-controller.insert.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users/new";
            }
            User user = UserMapper.toEntity(userDTO);
            userDAO.insertUser(user);
            logger.info("Usuario {} insertado con éxito.", user.getUsername());
        } catch (Exception e) {
            logger.error("Error al insertar el usuario {}: {}", userDTO.getUsername(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.user-controller.insert.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/users";
    }

    /**
     * Actualiza un usuario existente en la base de datos.
     *
     * @param userDTO               Objeto que contiene los datos del formulario.
     * @param result             Resultados de validación del formulario.
     * @param redirectAttributes Atributos para mensajes flash de redirección.
     * @param locale             Localización para mensajes internacionalizados.
     * @return Redirección a la lista de usuarios o al formulario si hay errores.
     */
    @PostMapping("/update")
    public String updateUser(@Valid @ModelAttribute("user") UserUpdateDTO userDTO, BindingResult result,
                             RedirectAttributes redirectAttributes, Locale locale) {
        logger.info("Actualizando usuario con ID {}", userDTO.getId());
        try {
            if (result.hasErrors()) {
                return "views/user/user-form";
            }
            if (userDAO.existUserByNameAndNotId(userDTO.getUsername(), userDTO.getId())) {
                logger.warn("El nombre de usuario {} ya existe para otro usuario.", userDTO.getUsername());
                String errorMessage = messageSource.getMessage("msg.user-controller.update.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users/edit?id=" + userDTO.getId();
            }
            User user = userDAO.getUserById(userDTO.getId());
            if (user == null){
                logger.warn("No se encontro el usuario con ID {}", userDTO.getId());
                String notFound = messageSource.getMessage("msg.user-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", notFound);
                return "redirect:/users";
            }
            UserMapper.copyToExistingEntity(userDTO, user);
            userDAO.updateUser(user);
            logger.info("Usuario con ID {} actualizado con éxito.", user.getId());
        } catch (Exception e) {
            logger.error("Error al actualizar el usuario con ID {}: {}", userDTO.getId(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.user-controller.update.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/users";
    }

    /**
     * Elimina un usuario por su ID.
     *
     * @param id                 ID del usuario a eliminar.
     * @param redirectAttributes Atributos para mensajes flash de redirección.
     * @return Redirección a la lista de usuarios.
     */
    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        logger.info("Eliminando usuario con ID {}", id);
        try {
            userDAO.deleteUser(id);
            logger.info("Usuario con ID {} eliminado con éxito.", id);
        } catch (Exception e) {
            logger.error("Error al eliminar el usuario con ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el usuario.");
        }
        return "redirect:/users";
    }

    /**
     * Muestra el formulario de edición para un usuario existente.
     *
     * @param id    ID del usuario a editar.
     * @param model Modelo de Spring para pasar datos a la vista.
     * @return Nombre de la vista con el formulario de edición de usuario.
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        logger.info("Mostrando formulario de edición para el usuario con ID {}", id);
        User user = null;
        UserUpdateDTO userDTO = null;
        try {
            user = userDAO.getUserById(id);
            if (user == null) {
                logger.warn("No se encontró el usuario con ID {}", id);
            }
            userDTO = UserMapper.toUpdateDTO(user);
        } catch (Exception e) {
            logger.error("Error al obtener el usuario con ID {}: {}", id, e.getMessage());
            model.addAttribute("errorMessage", "Error al obtener el usuario.");
        }
        model.addAttribute("user", userDTO);
        return "views/user/user-form";
    }
    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Mostrando detalle del usuario con ID {}", id);
        try {
            User user = userDAO.getUserById(id);
            if (user == null) {
                String msg = messageSource.getMessage("msg.user-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", msg);
                return "redirect:/users";
            }
            UserDetailDTO userDTO = UserMapper.toDetailDTO(user);
            model.addAttribute("user", userDTO);
            return "views/user/user-detail";
        } catch (Exception e) {
            logger.error("Error al obtener el detalle del usuario {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage("msg.user-controller.detail.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/users";
        }
    }

}
