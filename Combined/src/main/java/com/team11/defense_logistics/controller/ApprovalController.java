package com.team11.defense_logistics.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.team11.defense_logistics.dto.LogisticsRequestDto;
import com.team11.defense_logistics.model.Approval;
import com.team11.defense_logistics.service.ApprovalService;
import com.team11.defense_logistics.service.LogisticsRequestService;

@Controller
@RequestMapping("/approvals")
public class ApprovalController {

    private final ApprovalService service;
    private final LogisticsRequestService requestService;

    public ApprovalController(
            ApprovalService service,
            LogisticsRequestService requestService) {
        this.service = service;
        this.requestService = requestService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("approvals", service.findAll());
        model.addAttribute("approval", new Approval());
        model.addAttribute("pendingRequests", getPendingRequests());
        return "approvals/list";
    }

    @GetMapping("/new")
    public String newApproval(Model model) {
        model.addAttribute("approval", new Approval());
        model.addAttribute("pendingRequests", getPendingRequests());
        return "approvals/form";
    }

    @PostMapping
    public String createApproval(
            @Valid @ModelAttribute("approval") Approval approval,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pendingRequests", getPendingRequests());
            return "approvals/form";
        }

        try {
            service.create(approval);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Approval decision recorded successfully.");
            return "redirect:/approvals";
        } catch (RuntimeException exception) {
            bindingResult.reject("approval.invalid", exception.getMessage());
            model.addAttribute("pendingRequests", getPendingRequests());
            return "approvals/form";
        }
    }

    private List<LogisticsRequestDto> getPendingRequests() {
        try {
            return requestService.getAllRequests().stream()
                    .filter(request -> "PENDING".equalsIgnoreCase(request.getStatus()))
                    .toList();
        } catch (IllegalStateException exception) {
            return List.of();
        }
    }
}
