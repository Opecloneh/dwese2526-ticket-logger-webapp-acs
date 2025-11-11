package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloControler {
    private static final Logger logger = LoggerFactory.getLogger(HelloControler.class);
    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        //Loguea cuando el metodo hello es llamado, mostrando el nombre recibido
        logger.info("Entrando al metodo que recibe el parametro nombre: {}",name);
        //Formatea y devuelve el mensaje de saludo
        String greeting = String.format("Hola %s!", name);
        //Loguea el mensaje de saludo que se va a devolver
        logger.info("Saliendo del metodo con el mensaje : {}", greeting);

        return greeting;
    }
}
