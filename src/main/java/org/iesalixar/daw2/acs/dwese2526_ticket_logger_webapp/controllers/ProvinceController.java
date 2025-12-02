package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.apache.logging.log4j.message.Message;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos.ProvinceDAO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.daos.RegionDAO;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.*;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Province;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers.ProvinceMapper;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers.RegionMapper;
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
    public String listProvinces(@RequestParam(name = "page", defaultValue = "0") int page,
                                @RequestParam(name="size", defaultValue = "10") int size,
                                @RequestParam(name="sortField", defaultValue = "name") String sortField,
                                @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
                                Model model,
                                Locale locale) {
        logger.info("Solicitando la lista de todas las provincias... page={}, size={}, sortField{}, sortDir{}", page, size, sortField, sortDir);
        if (page < 0) page = 0;
        if (size < 0) size = 0;
        try {
            long totalElements = provinceDAO.countProvinces();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            if (totalPages > 0 && page >= totalPages) {
                page = totalPages - 1;
            }
            List<Province> listProvinces = provinceDAO.listProvincesPage(page, size, sortField, sortDir);
            List<ProvinceDTO> listProvincesDTO = ProvinceMapper.toDTOList(listProvinces);
            logger.info("Se han cargado {} provincias en la pagina {}.", listProvincesDTO.size(), page);
            model.addAttribute("listProvinces", listProvincesDTO);
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
            logger.error("Error al listar las provincias: {}", e.getMessage());
            String errorMessage = messageSource.getMessage("msg.province-controller.list.error", null, locale);
            model.addAttribute("errorMessage", "Error al listar las provincias.");
        }
        return "views/province/province-list";
    }

    @GetMapping("/new")
    public String showNewForm(Model model, Locale locale) {
        logger.info("Mostrando formulario para nueva provincia.");
        try {
            List<Region> listRegions = regionDAO.listAllRegions();
            List<RegionDTO> listRegionsDTOs = RegionMapper.toDTOList(listRegions);
            model.addAttribute("province", new ProvinceCreateDTO());
            model.addAttribute("listRegions", listRegionsDTOs);
        }
        catch(Exception e){
            logger.error("Error al cargar las regiones para el formulario de provincia: {}", e.getMessage());
            String errorMessage = messageSource.getMessage("msg.province-controller.edit.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
        }
        return "views/province/province-form";
    }
    @PostMapping("/insert")
    public String insertProvince(@Valid @ModelAttribute("province") ProvinceCreateDTO provinceDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Locale locale,
                                 Model model) {
        logger.info("Insertando nueva provincia con código {}", provinceDTO.getCode());
        try {
            if (result.hasErrors()) {
                List<Region> listRegions = regionDAO.listAllRegions();
                List<RegionDTO> listRegionsDTOs = RegionMapper.toDTOList(listRegions);
                model.addAttribute("listRegions", listRegionsDTOs);
                return "views/province/province-form";
            }
            if (provinceDAO.existProvinceByCode(provinceDTO.getCode())) {
                logger.warn("El código de la provincia {} ya existe.", provinceDTO.getCode());
                String errorMessage = messageSource.getMessage("msg.province-controller.insert.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/provinces/new";
            }
            Province province = ProvinceMapper.toEntity(provinceDTO);
            provinceDAO.insertProvince(province);
            logger.info("Provincia {} insertada con éxito.", province.getCode());
        } catch (Exception e) {
            logger.error("Error al insertar la provincia {}: {}", provinceDTO.getCode(), e.getMessage());
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
            ProvinceUpdateDTO provinceDTO = ProvinceMapper.toUpdateDTO(province);
            if (province == null) {
                logger.warn("No se encontro la provincia con ID{}", id);
                String errorMessage = messageSource.getMessage("msg.province-controller.edit.notfound", null, locale);
                model.addAttribute("errorMessage", errorMessage);
            }
            else{
                List<Region> listRegions = regionDAO.listAllRegions();
                List<RegionDTO> listRegionsDTOs = RegionMapper.toDTOList(listRegions);
                model.addAttribute("province", provinceDTO);
                model.addAttribute("listRegions", listRegionsDTOs);
            }
        } catch (Exception e) {
            logger.error("Error al obtener la region con ID {}: {}", id, e.getMessage());
            String errorMessage = messageSource.getMessage("msg.province-controller.edit.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
        }
        return "views/province/province-form";
    }
    @PostMapping("/update")
    public String updateProvince(@Valid @ModelAttribute("province") ProvinceUpdateDTO provinceDTO, BindingResult result,
                               RedirectAttributes redirectAttributes, Locale locale, Model model) {
        logger.info("Actualizando provincia con ID {}", provinceDTO.getId());
        try {
            if (result.hasErrors()) {
                List<Region> listRegions = regionDAO.listAllRegions();
                List<RegionDTO> listRegionsDTOs = RegionMapper.toDTOList(listRegions);
                model.addAttribute("listRegions", listRegionsDTOs);
                return "views/province/province-form";
            }
            if (provinceDAO.existProvinceByCodeAndNotId(provinceDTO.getCode(), provinceDTO.getId())) {
                logger.warn("El código de la provincia {} ya existe para otra región.", provinceDTO.getCode());
                String errorMessage = messageSource.getMessage("msg.province-controller.update.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/provinces/edit?id=" + provinceDTO.getId();
            }
            Province province = ProvinceMapper.toEntity(provinceDTO);
            provinceDAO.updateProvince(province);
            logger.info("Provincia con ID {} actualizada con éxito.", province.getId());
        } catch (Exception e) {
            logger.error("Error al actualizar la provincia con ID {}: {}", provinceDTO.getId(), e.getMessage());
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
    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale){
        logger.info("Mostrando detalle de la provincia con ID {}", id);
        try {
            Province province = provinceDAO.getProvinceById(id);
            ProvinceDetailDTO provinceDTO = ProvinceMapper.toDetailDTO(province);
            if (province == null){
                String msg = messageSource.getMessage("msg.province-controller.detail.notFund", null, locale);
                redirectAttributes.addFlashAttribute("erroressage", msg);
                return "redirect:/provinces";
            }
            model.addAttribute("province", provinceDTO);
            return "views/province/province-detail";
        }
        catch (Exception e){
            logger.error("Error al obtener el detalle de la provincia {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage("msg.province-controller.detail.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/provices";
        }
    }
}
