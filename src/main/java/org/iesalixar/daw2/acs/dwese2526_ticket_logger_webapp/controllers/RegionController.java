package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos.RegionDAO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.*;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.User;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers.RegionMapper;
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
    private RegionDAO regionDAO;

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
    public String listRegions(@RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(name="size", defaultValue = "10") int size,
                              @RequestParam(name="sortField", defaultValue = "name") String sortField,
                              @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
                              Model model){
        logger.info("Solicitando la lista de todas las regiones... page={}, size={}, sortField={}, sortDir={}",
                page, size, sortField ,sortDir);
        if (page<0) page = 0;
        if (size<0) size = 0;
        try {
            long totalElements = regionDAO.countRegions();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            if (totalPages > 0 && page >= totalPages) {
                page = totalPages - 1;
            }
            List<Region> listRegions = regionDAO.listRegionsPage(page,size, sortField, sortDir);
            List<RegionDTO> listRegionsDTOs = RegionMapper.toDTOList(listRegions);
            logger.info("Se han cargado {} regiones en la pagina {}.", listRegionsDTOs.size(), page);
            model.addAttribute("listRegions", listRegionsDTOs);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalElements", totalElements);
            //Para que la vista sepa como estamos ordenando ASC/DESC
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("reverseSortDir", "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc");
        }
        catch (Exception e) {
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
    public String showNewForm(Model model, Locale locale) {
        logger.info("Mostrando formulario para nueva region.");
        try {
            List<Region> listRegions = regionDAO.listAllRegions();
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
     * @param regionDTO            Objeto que contiene los datos del formulario.
     * @param result              Resultados de validación del formulario.
     * @param redirectAttributes  Atributos para mensajes flash de redirección.
     * @param locale              Localización para mensajes internacionalizados.
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
            if (regionDAO.existRegionByCode(regionDTO.getCode())) {
                logger.warn("El código de la región {} ya existe.", regionDTO.getCode());
                String errorMessage = messageSource.getMessage("msg.region-controller.insert.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/regions/new";
            }
            Region region = RegionMapper.toEntity(regionDTO);
            regionDAO.insertRegion(region);
            logger.info("Región {} insertada con éxito.", region.getCode());
        } catch (Exception e) {
            logger.error("Error al insertar la región {}: {}", regionDTO.getCode(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.region-controller.insert.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/regions";
    }

    /**
     * Actualiza una región existente en la base de datos.
     *
     * @param region              Objeto que contiene los datos del formulario.
     * @param result              Resultados de validación del formulario.
     * @param redirectAttributes  Atributos para mensajes flash de redirección.
     * @param locale              Localización para mensajes internacionalizados.
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
            if (regionDAO.existRegionByCodeAndNotId(regionDTO.getCode(), regionDTO.getId())) {
                logger.warn("El código de la región {} ya existe para otra región.", regionDTO.getCode());
                String errorMessage = messageSource.getMessage("msg.region-controller.update.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/regions/edit?id=" + regionDTO.getId();
            }
            Region region = regionDAO.getRegionById(regionDTO.getId());
            if (region == null) {
                logger.warn("No se ha encontrado la region con ID {}", regionDTO.getId());
                String notFound = messageSource.getMessage("msg.region-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", notFound);
                return "redirect:/regions";
            }
            RegionMapper.copyToExistingEntity(regionDTO, region);
            regionDAO.updateRegion(region);
            logger.info("Región con ID {} actualizada con éxito.", regionDTO.getId());
        } catch (Exception e) {
            logger.error("Error al actualizar la región con ID {}: {}", regionDTO.getId(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.region-controller.update.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/regions";
    }

    /**
     * Elimina una región por su ID.
     *
     * @param id                 ID de la región a eliminar.
     * @param redirectAttributes Atributos para mensajes flash de redirección.
     * @return Redirección a la lista de regiones.
     */
    @PostMapping("/delete")
    public String deleteRegion(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        logger.info("Eliminando region con ID {}", id);
        try {
            regionDAO.deleteRegion(id);
            logger.info("Region con ID {} eliminada con exito.", id);
        } catch (Exception e) {
            logger.error("Error al eliminar la region con ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la region.");
        }
        return "redirect:/regions";
    }

    /**
     * Muestra el formulario de edición para una región existente.
     *
     * @param id    ID de la región a editar.
     * @param model Modelo de Spring para pasar datos a la vista.
     * @return Nombre de la vista con el formulario de edición de región.
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        logger.info("Mostrando formulario de edicion para la region con ID {}", id);
        Region region = null;
        RegionUpdateDTO regionDTO = null;
        try {
            region = regionDAO.getRegionById(id);
            if (region == null) {
                logger.warn("No se encontro la region con ID{}", id);
            }
            regionDTO = RegionMapper.toUpdateDTO(region);
        } catch (Exception e) {
            logger.error("Error al obtener la region con ID {}: {}", id, e.getMessage());
            model.addAttribute("errorMessage", "Error al obtener la region.");
        }
        model.addAttribute("region", regionDTO);
        return "views/region/region-form";
    }

    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale){
        logger.info("Mostrando detalle de la region con ID {}", id);
        try {
            Region region = regionDAO.getRegionById(id);
            if (region == null){
                String msg = messageSource.getMessage("msg.province-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("erroressage", msg);
                return "redirect:/regions";
            }
            RegionDetailDTO regionDTO = RegionMapper.toDetailDTO(region);
            model.addAttribute("region", regionDTO);
            return "views/region/region-detail";
        }
        catch (Exception e){
            logger.error("Error al obtener el detalle de la region {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage("msg.region-controller.detail.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/regions";
        }
    }
}
