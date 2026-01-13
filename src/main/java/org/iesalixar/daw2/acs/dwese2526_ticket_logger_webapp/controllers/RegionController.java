package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.repositories.RegionRepository;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.*;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers.RegionMapper;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services.RegionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controlador Spring MVC para gestionar operaciones CRUD sobre regiones.
 * Proporciona métodos para listar, insertar, actualizar y eliminar regiones.
 */
@Controller
@RequestMapping("/regions")
public class RegionController {

    private static final Logger logger = LoggerFactory.getLogger(RegionController.class);

    /**
     * DAO para acceder a los datos de las regiones en la base de datos.
     */
    @Autowired
    private RegionService regionService;

    /**
     * Fuente de mensajes internacionalizados.
     */
    @Autowired
    private MessageSource messageSource;

    /**
     * Muestra la lista de todas las regiones.
     *
     * @param model Modelo de Spring para pasar datos a la vista.
     * @return Nombre de la vista que renderiza la lista de regiones.
     */
    @GetMapping
    public String listRegions(@PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
                              Model model) {
        logger.info("Solicitando la lista de todas las regiones... page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        try {
            Page<RegionDTO> listRegionsDTOs = regionService.list(pageable);
            logger.info("Se han cargado {} regiones en la pagina {}",
                    listRegionsDTOs.getNumberOfElements(), listRegionsDTOs.getNumber());
            model.addAttribute("page", listRegionsDTOs);

            String sortParam = "name,asc";
            if (listRegionsDTOs.getSort().isSorted()) {
                Sort.Order order = listRegionsDTOs.getSort().iterator().next();
                sortParam = order.getProperty() + "," + order.getDirection().name().toLowerCase();
            }
            model.addAttribute("sortParam", sortParam);
        } catch (Exception e) {
            logger.error("Error al listar las regiones: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error al listar las regiones.");
        }
        return "views/region/region-list";
    }

    /**
     * Muestra el formulario para crear una nueva región.
     *
     * @param model Modelo de Spring para pasar datos a la vista.
     * @return Nombre de la vista con el formulario de nueva región.
     */
    @GetMapping("/new")
    public String showNewForm(Model model, Locale locale, Pageable pageable) {
        logger.info("Mostrando formulario para nueva region.");
        try {
            List<RegionDTO> listRegions = regionService.listAll();
            model.addAttribute("region", new RegionCreateDTO());
            model.addAttribute("listRegions", listRegions);
        } catch (Exception e) {
            logger.error("Error al cargar las regiones para el formulario de regiones: {}", e.getMessage());
            String errorMessage = messageSource.getMessage("msg.user-controller.edit.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
        }
        return "views/region/region-form";
    }

    /**
     * Inserta una nueva región en la base de datos.
     *
     * @param regionDTO          Objeto que contiene los datos del formulario.
     * @param result             Resultados de validación del formulario.
     * @param redirectAttributes Atributos para mensajes flash de redirección.
     * @param locale             Localización para mensajes internacionalizados.
     * @return Redirección a la lista de regiones o al formulario si hay errores.
     */
    @PostMapping("/insert")
    public String insertRegion(@Valid @ModelAttribute("region") RegionCreateDTO regionDTO, BindingResult result,
                               RedirectAttributes redirectAttributes, Locale locale) {
        logger.info("Insertando nueva región con código {}", regionDTO.getCode());
        try {
            if (result.hasErrors()) {
                return "views/region/region-form";
            }
            regionService.create(regionDTO);

            logger.info("Region {} insertada con exito.", regionDTO.getCode());
            return "redirect:/regions";
        } catch (DuplicateResourceException ex) {
            logger.warn("El codigo de la region {} ya existe", regionDTO.getCode());
            String errorMessage = messageSource.getMessage("msg.region-controller.insert.codeExist", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/regions/new";
        } catch (Exception e) {
            logger.error("Error al insertar la región {}: {}", regionDTO.getCode(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.region-controller.insert.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/regions/new";
        }

    }

    /**
     * Actualiza una región existente en la base de datos.
     *
     * @param regionDTO          Objeto que contiene los datos del formulario.
     * @param result             Resultados de validación del formulario.
     * @param redirectAttributes Atributos para mensajes flash de redirección.
     * @param locale             Localización para mensajes internacionalizados.
     * @return Redirección a la lista de regiones o al formulario si hay errores.
     */
    @PostMapping("/update")
    public String updateRegion(@Valid @ModelAttribute("region") RegionUpdateDTO regionDTO, BindingResult result,
                               RedirectAttributes redirectAttributes, Locale locale) {
        logger.info("Actualizando región con ID {}", regionDTO.getId());
        try {
            if (result.hasErrors()) {
                return "views/region/region-form";
            }
            regionService.update(regionDTO);

            logger.info("Region con ID {} actualizada con exito", regionDTO.getId());
            return "redirect:/regions";

        } catch (DuplicateResourceException ex){
            logger.warn("El codigo de la region {} ya existe para otra region", regionDTO.getCode());
            String errorMessage = messageSource.getMessage("msg.region-controller.update.codeExist", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/regions/edit?id=" + regionDTO.getId();

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontro la region con ID {}", regionDTO.getId());
            String notFound = messageSource.getMessage("msg.region-controller.detail.notFound", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", notFound);
            return "redirect:/regions";
        }

        catch (Exception e) {
            logger.error("Error al actualizar la región con ID {}: {}", regionDTO.getId(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.region-controller.update.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/regions/edit?id=" + regionDTO.getId();
        }
    }

    /**
     * Elimina una región por su ID.
     *
     * @param id                 ID de la región a eliminar.
     * @param redirectAttributes Atributos para mensajes flash de redirección.
     * @return Redirección a la lista de regiones.
     */
    @PostMapping("/delete")
    public String deleteRegion(@RequestParam("id") Long id, RedirectAttributes redirectAttributes, Locale locale) {
        logger.info("Eliminando region con ID {}", id);
        try {
           regionService.delete(id);

           logger.info("Region con ID {} eliminada con exito.", id);
            return "redirect:/regions";
        }
        catch (ResourceNotFoundException ex) {
            logger.warn("No se encontro la region con ID {}", id);
            String notFound = messageSource.getMessage("msg.region-controller.detail.notFound", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", notFound);
            return "redirect:/regions";
        }
        catch (Exception e) {
            logger.error("Error al eliminar la region con ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la region.");
            return "redirect:/regions";
        }

    }

    /**
     * Muestra el formulario de edición para una región existente.
     *
     * @param id    ID de la región a editar.
     * @param model Modelo de Spring para pasar datos a la vista.
     * @return Nombre de la vista con el formulario de edición de región.
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model, Locale locale) {
        logger.info("Mostrando formulario de edicion para la region con ID {}", id);

        try {
            RegionUpdateDTO regionDTO = regionService.getForEdit(id);
            model.addAttribute("region", regionDTO);
            return "views/region/region-form";

        } catch (ResourceNotFoundException ex) {
            logger.error("Error al obtener la region con ID {}: {}", id, ex.getMessage());
            model.addAttribute("errorMessage", "Error al obtener la region.");
            return "redirect:/regions";
        } catch (Exception e) {
            logger.error("Error al obtener la region con ID {}: {}", id, e.getMessage());
            model.addAttribute("errorMessage", "Error al obtener la region.");
            return "redirect:/regions";
        }
    }

    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Mostrando detalle de la region con ID {}", id);
        try {
         RegionDetailDTO regionDTO = regionService.getDetail(id);
         model.addAttribute("region", regionDTO);
         return "views/region/region-detail";
        }
        catch (ResourceNotFoundException ex) {
            String msg = messageSource.getMessage("msg.region-controller.detail.norFound", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/regions";
        }

        catch (Exception e) {
            logger.error("Error al obtener el detalle de la region {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage("msg.region-controller.detail.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/regions";
        }
    }
}
