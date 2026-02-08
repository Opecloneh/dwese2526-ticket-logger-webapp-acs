package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        // Recuperar el mensaje de error desde la sesión
        String errorMessage = (String) request.getSession().getAttribute("errorMessage");

        // Si existe un mensaje de error, añadirlo al modelo y limpiar la sesión
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            request.getSession().removeAttribute("errorMessage"); // Limpiar el mensaje después de mostrarlo
        }
        return "views/login/login"; // Redirige a una plantilla personalizada de login
    }
}
