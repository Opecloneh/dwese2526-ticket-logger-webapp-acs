package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.apache.logging.log4j.message.Message;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos.ProvinceDAO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos.RegionDAO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Province;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/provinces")
public class ProvinceController {
    private static final Logger logger = LoggerFactory.getLogger(ProvinceController.class);

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ProvinceDAO provinceDAO;

    @Autowired
    private RegionDAO regionDAO;

    @GetMapping
    public String listProvinces(Model model, Locale locale){
        logger.info("Solicitando la lista de toddas las provincias...");
        try {
            List<Province> listProvinces = provinceDAO.listALlProvinces();
            logger.info("Se han cargado {} provincias.", listProvinces.size());
            model.addAttribute("listProvinces", listProvinces);
        }
        catch (Exception e){
            logger.error("Error al listar las provincias: {}", e.getMessage());
            String errorMessage = messageSource.getMessage("msg.province-controller.list.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
        }
        return "views/province/province-list";
    }

    @GetMapping("/new")
    public String showNewForm(Model model, Locale locale) {
        logger.info("Mostrando formulario para nueva provincia.");
        try {
            List<Region> listRegions = regionDAO.listAllRegions();
            model.addAttribute("province", new Province());
            model.addAttribute("listRegions", listRegions);
        }
        catch(Exception e){
            logger.error("Error al cargar las regiones para el formulario de provincia: {}", e.getMessage());
            String errorMEssage = messageSource.getMessage("msg.province-controller.edit.error", null, locale);
            model.addAttribute("errorMessage", errorMEssage);
        }
        return "views/province/province-form";
    }
    @PostMapping("/insert")
    public String insertProvince(@Valid @ModelAttribute("province") Province province,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Locale locale,
                                 Model model) {
        logger.info("Insertando nueva provincia con código {}", province.getCode());
        try {
            if (result.hasErrors()) {
                List<Region> listRegions = regionDAO.listAllRegions();
                model.addAttribute("listRegions", listRegions);
                return "views/province/province-form";
            }
            if (provinceDAO.existProvinceByCode(province.getCode())) {
                logger.warn("El código de la provincia {} ya existe.", province.getCode());
                String errorMessage = messageSource.getMessage("msg.province-controller.insert.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/provinces/new";
            }
            provinceDAO.insertProvince(province);
            logger.info("Provincia {} insertada con éxito.", province.getCode());
        } catch (Exception e) {
            logger.error("Error al insertar la provincia {}: {}", province.getCode(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.province-controller.insert.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/provinces";
    }
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model, Locale locale) {
        logger.info("Mostrando formulario de edicion para la provincia con ID {}", id);
        try {
            Province province = provinceDAO.getProvinceById(id);
            if (province == null) {
                logger.warn("No se encontro la provincia con ID{}", id);
                String errorMessage = messageSource.getMessage("msg.province-controller.edit.notfound", null, locale);
                model.addAttribute("errorMessage", errorMessage);
            }
            else{
                List<Region> listRegions = regionDAO.listAllRegions();
                model.addAttribute("province", province);
                model.addAttribute("listRegions", listRegions);
            }
        } catch (Exception e) {
            logger.error("Error al obtener la region con ID {}: {}", id, e.getMessage());
            String errorMessage = messageSource.getMessage("msg.province-controller.edit.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
        }
        return "views/province/province-form";
    }
    @PostMapping("/update")
    public String updateProvince(@Valid @ModelAttribute("province") Province province, BindingResult result,
                               RedirectAttributes redirectAttributes, Locale locale, Model model) {
        logger.info("Actualizando provincia con ID {}", province.getId());
        try {
            if (result.hasErrors()) {
                List<Region> listRegions = regionDAO.listAllRegions();
                model.addAttribute("listRegions", listRegions);
                return "views/province/province-form";
            }
            if (provinceDAO.existProvinceByCodeAndNotId(province.getCode(), province.getId())) {
                logger.warn("El código de la provincia {} ya existe para otra región.", province.getCode());
                String errorMessage = messageSource.getMessage("msg.province-controller.update.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/provinces/edit?id=" + province.getId();
            }
            provinceDAO.updateProvince(province);
            logger.info("Provincia con ID {} actualizada con éxito.", province.getId());
        } catch (Exception e) {
            logger.error("Error al actualizar la provincia con ID {}: {}", province.getId(), e.getMessage());
            String errorMessage = messageSource.getMessage("msg.province-controller.update.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }
        return "redirect:/provinces";
    }
    @PostMapping("/delete")
    public String deleteProvince(@RequestParam("id") Long id, RedirectAttributes redirectAttributes, Locale locale) {
        logger.info("Eliminando provincia con ID {}", id);
        try {
            provinceDAO.deleteProvince(id);
            logger.info("Provincia con ID {} eliminada con exito.", id);
        } catch (Exception e) {
            logger.error("Error al eliminar la provincia con ID {}: {}", id, e.getMessage());
            String errorMessaage = messageSource.getMessage("msg.province-controller.delete.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessaage);
        }
        return "redirect:/provinces";
    }
}
