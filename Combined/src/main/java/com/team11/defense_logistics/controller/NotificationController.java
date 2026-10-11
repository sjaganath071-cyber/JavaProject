package com.team11.defense_logistics.controller;

import com.team11.defense_logistics.dto.NotificationDto;
import com.team11.defense_logistics.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@Controller
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        List<NotificationDto> notifications = Collections.emptyList();
        String notificationError = null;

        try {
            if (!notificationService.isApiConfigured()) {
                notificationError = "The notification backend endpoint is not configured yet. Please configure defense-logistics.notifications.api-url.";
            } else {
                notifications = notificationService.fetchNotifications();
            }
        } catch (Exception ex) {
            notificationError = "The notifications service is currently unavailable. Please try again later.";
        }

        long total = notifications.size();
        long unread = notifications.stream().filter(item -> !item.isRead()).count();
        long read = notifications.stream().filter(NotificationDto::isRead).count();

        model.addAttribute("notifications", notifications);
        model.addAttribute("totalNotifications", total);
        model.addAttribute("unreadNotifications", unread);
        model.addAttribute("readNotifications", read);
        model.addAttribute("notificationError", notificationError);

        return "notifications";
    }
}
