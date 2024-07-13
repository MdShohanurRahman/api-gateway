package my.com.liberty.apigateway.controller;

import lombok.RequiredArgsConstructor;
import my.com.liberty.apigateway.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/route-list")
    public ResponseEntity<?> getRouteList() {
        return ResponseEntity.ok(adminService.apiRouteList());
    }

    @GetMapping("/refresh-restrictions")
    public void refreshRestrictions() {
        adminService.refreshRestrictions();
    }
}