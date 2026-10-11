package com.team11.defense_logistics.controller;

import com.team11.defense_logistics.dto.PersonnelDto;
import com.team11.defense_logistics.service.PersonnelService;
import com.team11.defense_logistics.service.PersonnelService.PersonnelServiceException;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/personnel")
public class PersonnelController {

    private final PersonnelService personnelService;

    public PersonnelController(PersonnelService personnelService) {
        this.personnelService = personnelService;
    }

    @GetMapping
    public String listPersonnel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String unit,
            @RequestParam(required = false) String availability,
            @RequestParam(required = false) Boolean active,
            Model model) {

        try {
            List<PersonnelDto> allPersonnel =
                    personnelService.getAllPersonnel();

            List<PersonnelDto> filteredPersonnel = allPersonnel.stream()
                    .filter(person -> matchesKeyword(person, keyword))
                    .filter(person -> matchesUnit(person, unit))
                    .filter(person -> matchesAvailability(person, availability))
                    .filter(person -> matchesActive(person, active))
                    .toList();

            model.addAttribute("personnelList", filteredPersonnel);
            model.addAttribute("keyword", keyword == null ? "" : keyword);
            model.addAttribute("unit", unit == null ? "" : unit);
            model.addAttribute(
                    "availability",
                    availability == null ? "" : availability
            );
            model.addAttribute("active", active == null ? "" : active);
            model.addAttribute(
                    "units",
                    allPersonnel.stream()
                            .map(PersonnelDto::getUnit)
                            .filter(value -> value != null && !value.isBlank())
                            .distinct()
                            .sorted()
                            .toList()
            );

        } catch (PersonnelServiceException exception) {
            model.addAttribute("personnelList", List.of());
            model.addAttribute("units", List.of());
            model.addAttribute("keyword", keyword == null ? "" : keyword);
            model.addAttribute("unit", unit == null ? "" : unit);
            model.addAttribute(
                    "availability",
                    availability == null ? "" : availability
            );
            model.addAttribute("active", active == null ? "" : active);
            model.addAttribute("errorMessage", exception.getMessage());
        }

        return "personnel/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("personnel", new PersonnelDto());
        model.addAttribute("pageTitle", "Add Personnel");
        model.addAttribute("formAction", "/personnel/new");
        model.addAttribute("formMode", "create");

        return "personnel/form";
    }

    @PostMapping("/new")
    public String createPersonnel(
            @Valid @ModelAttribute("personnel") PersonnelDto personnel,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Add Personnel");
            model.addAttribute("formAction", "/personnel/new");
            model.addAttribute("formMode", "create");

            return "personnel/form";
        }

        try {
            personnelService.createPersonnel(personnel);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Personnel record created successfully."
            );

            return "redirect:/personnel";

        } catch (PersonnelServiceException exception) {
            model.addAttribute("pageTitle", "Add Personnel");
            model.addAttribute("formAction", "/personnel/new");
            model.addAttribute("formMode", "create");
            model.addAttribute("errorMessage", exception.getMessage());

            return "personnel/form";
        }
    }

    @GetMapping("/{id}")
    public String viewPersonnel(
            @PathVariable Long id,
            Model model) {

        try {
            model.addAttribute(
                    "personnel",
                    personnelService.getPersonnelById(id)
            );

            return "personnel/details";

        } catch (PersonnelServiceException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("backUrl", "/personnel");

            return "personnel/details";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        try {
            model.addAttribute(
                    "personnel",
                    personnelService.getPersonnelById(id)
            );
            model.addAttribute("pageTitle", "Edit Personnel");
            model.addAttribute("formAction", "/personnel/" + id + "/edit");
            model.addAttribute("formMode", "edit");

            return "personnel/form";

        } catch (PersonnelServiceException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("backUrl", "/personnel");

            return "personnel/details";
        }
    }

    @PostMapping("/{id}/edit")
    public String updatePersonnel(
            @PathVariable Long id,
            @Valid @ModelAttribute("personnel") PersonnelDto personnel,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Personnel");
            model.addAttribute("formAction", "/personnel/" + id + "/edit");
            model.addAttribute("formMode", "edit");

            return "personnel/form";
        }

        try {
            personnelService.updatePersonnel(id, personnel);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Personnel record updated successfully."
            );

            return "redirect:/personnel/" + id;

        } catch (PersonnelServiceException exception) {
            model.addAttribute("pageTitle", "Edit Personnel");
            model.addAttribute("formAction", "/personnel/" + id + "/edit");
            model.addAttribute("formMode", "edit");
            model.addAttribute("errorMessage", exception.getMessage());

            return "personnel/form";
        }
    }

    @PostMapping("/{id}/availability")
    public String updateAvailability(
            @PathVariable Long id,
            @RequestParam String availability,
            RedirectAttributes redirectAttributes) {

        List<String> allowedValues = List.of(
                "AVAILABLE",
                "ON_DUTY",
                "ON_LEAVE",
                "UNAVAILABLE"
        );

        if (!allowedValues.contains(availability)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Invalid availability value."
            );

            return "redirect:/personnel/" + id;
        }

        try {
            personnelService.updateAvailability(id, availability);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Personnel availability updated successfully."
            );

        } catch (PersonnelServiceException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/personnel/" + id;
    }

    private boolean matchesKeyword(PersonnelDto person, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String search = keyword.toLowerCase(Locale.ROOT);

        return contains(person.getName(), search)
                || contains(person.getPersonnelCode(), search)
                || contains(person.getRole(), search)
                || contains(person.getPosting(), search);
    }

    private boolean matchesUnit(PersonnelDto person, String unit) {
        return unit == null
                || unit.isBlank()
                || (person.getUnit() != null
                && person.getUnit().equalsIgnoreCase(unit));
    }

    private boolean matchesAvailability(
            PersonnelDto person,
            String availability) {

        return availability == null
                || availability.isBlank()
                || (person.getAvailability() != null
                && person.getAvailability().equalsIgnoreCase(availability));
    }

    private boolean matchesActive(PersonnelDto person, Boolean active) {
        return active == null || person.isActive() == active;
    }

    private boolean contains(String value, String search) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(search);
    }
}
