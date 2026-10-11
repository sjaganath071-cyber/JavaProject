
package com.team11.logistics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogisticsController {

    @GetMapping("/")
    public String home() {
        return "Logistics Service is running successfully!";
    }

    @GetMapping("/api/logistics/health")
    public String health() {
        return "UP";
    }
}
