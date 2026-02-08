package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.dtos.*;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services.ProvinceService;
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

@Controller
@RequestMapping("/provinces")
public class ProvinceController {

    private static final Logger logger = LoggerFactory.getLogger(ProvinceController.class);

    @Autowired
    private ProvinceService provinceService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private MessageSource messageSource;

    // =========================
    // GET /provinces
    // =========================
    @GetMapping
    public String listProvinces(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
            Model model,
            Locale locale) {

        try {
            Page<ProvinceDTO> page = provinceService.list(pageable);
            model.addAttribute("page", page);

            String sortParam = "name,asc";
            if (page.getSort().isSorted()) {
                Sort.Order order = page.getSort().iterator().next();
                sortParam = order.getProperty() + "," + order.getDirection().name().toLowerCase();
            }
            model.addAttribute("sortParam", sortParam);

        } catch (Exception e) {
            logger.error("Error listando provincias", e);
            String errorMessage = messageSource.getMessage("msg.province-controller.list.error", null, locale);
            model.addAttribute("errorMessage", errorMessage);

        }

        return "views/province/province-list";
    }

    // =========================
    // GET /provinces/new
    // =========================
    @GetMapping("/new")
    public String showNewForm(Model model, Locale locale) {

        model.addAttribute("listRegions", provinceService.listRegionsForSelect());
        model.addAttribute("province", new ProvinceCreateDTO());

        return "views/province/province-form";
    }

    // =========================
    // POST /provinces/insert
    // =========================
    @PostMapping("/insert")
    public String insertProvince(
            @Valid @ModelAttribute("province") ProvinceCreateDTO provinceDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model,
            Locale locale) {

        try {
            if (result.hasErrors()) {
                model.addAttribute("listRegions", provinceService.listRegionsForSelect());
                return "views/province/province-form";
            }

            provinceService.create(provinceDTO);
            return "redirect:/provinces";

        } catch (DuplicateResourceException ex) {
            String msg = messageSource.getMessage(
                    "msg.province-controller.insert.codeExist",
                    null,
                    locale
            );
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/provinces/new";
        }
    }

    // =========================
    // GET /provinces/edit
    // =========================
    @GetMapping("/edit")
    public String showEditForm(
            @RequestParam("id") Long id,
            Model model,
            Locale locale,
            RedirectAttributes redirectAttributes) {

        try {
            ProvinceUpdateDTO dto = provinceService.getForEdit(id);
            model.addAttribute("province", dto);
            model.addAttribute("listRegions", provinceService.listRegionsForSelect());
            return "views/province/province-form";

        } catch (ResourceNotFoundException ex) {
            String msg = messageSource.getMessage(
                    "msg.province-controller.edit.notfound",
                    new Object[]{id},
                    locale
            );
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/provinces";
        }
    }

    // =========================
    // POST /provinces/update
    // =========================
    @PostMapping("/update")
    public String updateProvince(
            @Valid @ModelAttribute("province") ProvinceUpdateDTO provinceDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model,
            Locale locale) {

        try {
            if (result.hasErrors()) {
                model.addAttribute("listRegions", provinceService.listRegionsForSelect());
                return "views/province/province-form";
            }

            provinceService.update(provinceDTO);
            return "redirect:/provinces";

        } catch (DuplicateResourceException ex) {
            String msg = messageSource.getMessage(
                    "msg.province-controller.update.codeExist",
                    null,
                    locale
            );
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/provinces/edit?id=" + provinceDTO.getId();

        } catch (ResourceNotFoundException ex) {
            String msg = messageSource.getMessage(
                    "msg.province-controller.detail.notFound",
                    null,
                    locale
            );
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/provinces";
        }
    }

    // =========================
    // POST /provinces/delete
    // =========================
    @PostMapping("/delete")
    public String deleteProvince(
            @RequestParam("id") Long id,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        try {
            provinceService.delete(id);
            return "redirect:/provinces";

        } catch (ResourceNotFoundException ex) {
            String msg = messageSource.getMessage(
                    "msg.province-controller.detail.notFound",
                    null,
                    locale
            );
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/provinces";
        }
    }

    // =========================
    // GET /provinces/detail
    // =========================
    @GetMapping("/detail")
    public String showDetail(
            @RequestParam("id") Long id,
            Model model,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        try {
            ProvinceDetailDTO dto = provinceService.getDetail(id);
            model.addAttribute("province", dto);
            return "views/province/province-detail";

        } catch (ResourceNotFoundException ex) {
            String msg = messageSource.getMessage(
                    "msg.province-controller.detail.notFound",
                    null,
                    locale
            );
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/provinces";
        }
    }
}
