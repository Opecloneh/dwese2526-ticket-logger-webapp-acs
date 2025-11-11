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

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    //DAO para gestionar las operaciones de las usuarios en la base de datos
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public String listUsers(Model model) {
        logger.info("Solicitando la lista de todos los usuarios...");
        List<User> listUsers = null;
        try{
            listUsers = userDAO.listAllUsers();
            logger.info("Se han cargado {} usuarios.", listUsers.size());
        }
        catch (Exception e) {
            logger.error("Error al listar los usuarios: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error al listar los usuarios.");
        }
        model.addAttribute("listUsers", listUsers);
        return "views/user/user-list";
    }

    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nueva user.");
        model.addAttribute("user", new User());
        return "views/user/user-form";
    }

    /**
     * Inserta una nueva región en la base de datos.
     *
     * @param user              Objeto que contiene los datos del formulario.
     * @param redirectAttributes  Atributos para mensajes flash de redirección.
     * @return Redirección a la lista de usuarios.
     */
    @PostMapping("/insert")
    public String insertUser(@Valid @ModelAttribute("user") User user, BindingResult result, RedirectAttributes redirectAttributes, Locale locale) {
        logger.info("Insertando nuevo usuario con nombre {}", user.getUsername());
        try {
            if (result.hasErrors()) {
                return "user-form";  // Devuelve el formulario para mostrar los errores de validación
            }
            if (userDAO.existsUserByName(user.getUsername())) {
                logger.warn("El código del usuario {} ya existe.", user.getUsername());
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
        return "redirect:/users"; // Redirigir a la lista de usuarios
    }


    /**
     * Actualiza una región existente en la base de datos.
     *
     * @param user              Objeto que contiene los datos del formulario.
     * @param redirectAttributes  Atributos para mensajes flash de redirección.
     * @return Redirección a la lista de usuarios.
     */
    @PostMapping("/update")
    public String updateUser(@Valid @ModelAttribute("user") User user, BindingResult result, RedirectAttributes redirectAttributes, Locale locale) {
        logger.info("Actualizando usuario con ID {}", user.getId());
        try {
            if (result.hasErrors()) {
                return "user-form";  // Devuelve el formulario para mostrar los errores de validación
            }
            if (userDAO.existUserByNameAndNotId(user.getUsername(), user.getId())) {
                logger.warn("El nombre de usuario {} ya existe para otro usuario.", user.getUsername());
                String errorMessage = messageSource.getMessage("msg.user-controller.update.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/users/edit?id=" + user.getId();
            }
            userDAO.updateUser(user);
            logger.info("Región con ID {} actualizada con éxito.", user.getId());
        } catch (Exception e) {
            logger.error("Error al actualizar la región con ID {}: {}", user.getId(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.user-controller.update.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/users"; // Redirigir a la lista de usuarios
    }


    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        logger.info("Eliminando usuario con ID {}", id);
        try {
            userDAO.deleteUser(id);
            logger.info("usuario con ID {} eliminada con exito.", id);
        } catch (Exception e) {
            logger.error("Error al eliminar el usuario con ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la user.");
        }
        return "redirect:/users";
    }

    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id")Long id, Model model) {
        logger.info("Mostrando formulario de edicion para el usuario con ID {}", id);
        User user = null;
        try{
            user = userDAO.getUserById(id);
            if (user==null) {
                logger.warn("No se encontro el usuario con ID{}", id);
            }
        } catch (Exception e) {
            logger.error("Error al obtener el usuario con ID {}: {}", id, e.getMessage());
            model.addAttribute("errorMessage", "Error al obtener el usuario.");
        }
        model.addAttribute("user", user);
        return "views/user/user-form";
    }
}
