package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos.UserDAO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String listUsers(Model model) {
        logger.info("Solicitando la lista de todos los usuarios...");
        List<User> listUsers = null;
        try {
            listUsers = userDAO.listAllUsers();
            logger.info("Se han cargado {} usuarios.", listUsers.size());
        } catch (Exception e) {
            logger.error("Error al listar los usuarios: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error al listar los usuarios.");
        }
        model.addAttribute("listUsers", listUsers);
        return "views/user/user-list";
    }

    /**
     * Muestra el formulario para crear un nuevo usuario.
     *
     * @param model Modelo de Spring para pasar datos a la vista.
     * @return Nombre de la vista con el formulario de nuevo usuario.
     */
    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nuevo usuario.");
        model.addAttribute("user", new User());
        return "views/user/user-form";
    }

    /**
     * Inserta un nuevo usuario en la base de datos.
     *
     * @param user               Objeto que contiene los datos del formulario.
     * @param result             Resultados de validación del formulario.
     * @param redirectAttributes Atributos para mensajes flash de redirección.
     * @param locale             Localización para mensajes internacionalizados.
     * @return Redirección a la lista de usuarios o al formulario si hay errores.
     */
    @PostMapping("/insert")
    public String insertUser(@Valid @ModelAttribute("user") User user, BindingResult result,
                             RedirectAttributes redirectAttributes, Locale locale) {
        logger.info("Insertando nuevo usuario con nombre {}", user.getUsername());
        try {
            if (result.hasErrors()) {
                return "user-form";
            }
            if (userDAO.existsUserByName(user.getUsername())) {
                logger.warn("El nombre de usuario {} ya existe.", user.getUsername());
                String errorMessage = messageSource.getMessage("msg.user-controller.insert.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users/new";
            }
            userDAO.insertUser(user);
            logger.info("Usuario {} insertado con éxito.", user.getUsername());
        } catch (Exception e) {
            logger.error("Error al insertar el usuario {}: {}", user.getUsername(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.user-controller.insert.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/users";
    }

    /**
     * Actualiza un usuario existente en la base de datos.
     *
     * @param user               Objeto que contiene los datos del formulario.
     * @param result             Resultados de validación del formulario.
     * @param redirectAttributes Atributos para mensajes flash de redirección.
     * @param locale             Localización para mensajes internacionalizados.
     * @return Redirección a la lista de usuarios o al formulario si hay errores.
     */
    @PostMapping("/update")
    public String updateUser(@Valid @ModelAttribute("user") User user, BindingResult result,
                             RedirectAttributes redirectAttributes, Locale locale) {
        logger.info("Actualizando usuario con ID {}", user.getId());
        try {
            if (result.hasErrors()) {
                return "user-form";
            }
            if (userDAO.existUserByNameAndNotId(user.getUsername(), user.getId())) {
                logger.warn("El nombre de usuario {} ya existe para otro usuario.", user.getUsername());
                String errorMessage = messageSource.getMessage("msg.user-controller.update.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users/edit?id=" + user.getId();
            }
            userDAO.updateUser(user);
            logger.info("Usuario con ID {} actualizado con éxito.", user.getId());
        } catch (Exception e) {
            logger.error("Error al actualizar el usuario con ID {}: {}", user.getId(), e.getMessage());
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
        try {
            user = userDAO.getUserById(id);
            if (user == null) {
                logger.warn("No se encontró el usuario con ID {}", id);
            }
        } catch (Exception e) {
            logger.error("Error al obtener el usuario con ID {}: {}", id, e.getMessage());
            model.addAttribute("errorMessage", "Error al obtener el usuario.");
        }
        model.addAttribute("user", user);
        return "views/user/user-form";
    }
}
