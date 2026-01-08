package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.repositories.ProvinceRepository;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.repositories.RegionRepository;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.*;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Province;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.entities.Region;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers.ProvinceMapper;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.mappers.RegionMapper;
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

@Controller
@RequestMapping("/provinces")
public class ProvinceController {

    private static final Logger logger = LoggerFactory.getLogger(ProvinceController.class);

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public String listProvinces(@PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
                                Model model) {
        logger.info("Solicitando la lista de provincias... page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        try {
            Page<ProvinceDTO> provincesPage = provinceRepository.findAll(pageable).map(ProvinceMapper::toDTO);
            logger.info("Se han cargado {} provincias en la página {}",
                    provincesPage.getNumberOfElements(), provincesPage.getNumber());

            model.addAttribute("page", provincesPage);

            String sortParam = "name,asc";
            if (provincesPage.getSort().isSorted()) {
                Sort.Order order = provincesPage.getSort().iterator().next();
                sortParam = order.getProperty() + "," + order.getDirection().name().toLowerCase();
            }
            model.addAttribute("sortParam", sortParam);
        } catch (Exception e) {
            logger.error("Error al listar las provincias: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error al listar las provincias.");
        }

        return "views/province/province-list";
    }

    @GetMapping("/new")
    public String showNewForm(Model model, Locale locale) {
        logger.info("Mostrando formulario para nueva provincia.");
        try {
            List<Region> listRegions = regionRepository.findAll();
            List<RegionDTO> listRegionsDTOs = RegionMapper.toDTOList(listRegions);

            model.addAttribute("province", new ProvinceCreateDTO());
            model.addAttribute("listRegions", listRegionsDTOs);
        } catch (Exception e) {
            logger.error("Error al cargar las regiones para el formulario de provincia: {}", e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.province-controller.edit.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
        }

        return "views/province/province-form";
    }

    @PostMapping("/insert")
    public String insertProvince(@Valid @ModelAttribute("province") ProvinceCreateDTO provinceDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Locale locale) {
        logger.info("Insertando nueva provincia con código {}", provinceDTO.getCode());

        try {
            if (result.hasErrors()) {
                return "views/province/province-form";
            }

            if (provinceRepository.existsByCode(provinceDTO.getCode())) {
                logger.warn("El código de la provincia {} ya existe.", provinceDTO.getCode());
                String errorMessage = messageSource.getMessage("msg.province-controller.insert.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/provinces/new";
            }

            Province province = ProvinceMapper.toEntity(provinceDTO);
            provinceRepository.save(province);

            logger.info("Provincia {} insertada con éxito.", province.getCode());
        } catch (Exception e) {
            logger.error("Error al insertar la provincia {}: {}", provinceDTO.getCode(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.province-controller.insert.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }

        return "redirect:/provinces";
    }

    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model, Locale locale) {
        logger.info("Mostrando formulario de edición para la provincia con ID {}", id);

        try {
            Optional<Province> provinceOpt = provinceRepository.findById(id);
            if (provinceOpt.isEmpty()) {
                logger.warn("No se encontró la provincia con ID {}", id);
                String errorMessage = messageSource.getMessage("msg.province-controller.edit.notfound", null, locale);
                model.addAttribute("errorMessage", errorMessage);
                return "views/province/province-form";
            }

            Province province = provinceOpt.get();
            ProvinceUpdateDTO provinceDTO = ProvinceMapper.toUpdateDTO(province);

            List<Region> listRegions = regionRepository.findAll();
            List<RegionDTO> listRegionsDTOs = RegionMapper.toDTOList(listRegions);

            model.addAttribute("province", provinceDTO);
            model.addAttribute("listRegions", listRegionsDTOs);
        } catch (Exception e) {
            logger.error("Error al obtener la provincia con ID {}: {}", id, e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.province-controller.edit.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);
        }

        return "views/province/province-form";
    }

    @PostMapping("/update")
    public String updateProvince(@Valid @ModelAttribute("province") ProvinceUpdateDTO provinceDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Locale locale) {
        logger.info("Actualizando provincia con ID {}", provinceDTO.getId());

        try {
            if (result.hasErrors()) {
                return "views/province/province-form";
            }

            if (provinceRepository.existsByCodeAndIdNot(provinceDTO.getCode(), provinceDTO.getId())) {
                logger.warn("El código de la provincia {} ya existe para otra región.", provinceDTO.getCode());
                String errorMessage = messageSource.getMessage("msg.province-controller.update.codeExist", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/provinces/edit?id=" + provinceDTO.getId();
            }

            Optional<Province> provinceOpt = provinceRepository.findById(provinceDTO.getId());
            if (provinceOpt.isEmpty()) {
                logger.warn("No se encontró la provincia con ID {}", provinceDTO.getId());
                String notFound = messageSource.getMessage("msg.province-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", notFound);
                return "redirect:/provinces";
            }

            Province province = provinceOpt.get();
            ProvinceMapper.copyToExistingEntity(provinceDTO, province);
            provinceRepository.save(province);

            logger.info("Provincia con ID {} actualizada con éxito.", province.getId());
        } catch (Exception e) {
            logger.error("Error al actualizar la provincia con ID {}: {}", provinceDTO.getId(), e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.province-controller.update.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }

        return "redirect:/provinces";
    }

    @PostMapping("/delete")
    public String deleteProvince(@RequestParam("id") Long id, RedirectAttributes redirectAttributes, Locale locale) {
        logger.info("Eliminando provincia con ID {}", id);

        try {
            Optional<Province> provinceOpt = provinceRepository.findById(id);
            if (provinceOpt.isEmpty()) {
                logger.warn("No se encontró la provincia con ID {}", id);
                String notFound = messageSource.getMessage("msg.province-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", notFound);
                return "redirect:/provinces";
            }

            provinceRepository.deleteById(id);
            logger.info("Provincia con ID {} eliminada con éxito.", id);
        } catch (Exception e) {
            logger.error("Error al eliminar la provincia con ID {}: {}", id, e.getMessage(), e);
            String errorMessage = messageSource.getMessage("msg.province-controller.delete.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }

        return "redirect:/provinces";
    }

    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Mostrando detalle de la provincia con ID {}", id);

        try {
            Optional<Province> provinceOpt = provinceRepository.findById(id);
            if (provinceOpt.isEmpty()) {
                String msg = messageSource.getMessage("msg.province-controller.detail.notFound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", msg);
                return "redirect:/provinces";
            }

            Province province = provinceOpt.get();
            ProvinceDetailDTO provinceDTO = ProvinceMapper.toDetailDTO(province);

            model.addAttribute("province", provinceDTO);
            return "views/province/province-detail";
        } catch (Exception e) {
            logger.error("Error al obtener el detalle de la provincia {}: {}", id, e.getMessage(), e);
            String msg = messageSource.getMessage("msg.province-controller.detail.error", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/provinces";
        }
    }
}
