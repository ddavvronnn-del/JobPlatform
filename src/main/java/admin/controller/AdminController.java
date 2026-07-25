package admin.controller;

import admin.dto.AdminDto;
import admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/admins")
@RequiredArgsConstructor
public class AdminController {

        private final AdminService adminService;

        @GetMapping
        public ResponseEntity<List<AdminDto.AdminDTO>> getAllAdmins() {
            return ResponseEntity.ok(adminService.getAllAdmins());
        }

        @GetMapping("/{id}")
        public ResponseEntity<AdminDto.AdminDTO> getAdminById(@PathVariable Long id) {
            return ResponseEntity.ok(adminService.getAdminById(id));
        }

        @PostMapping
        public ResponseEntity<AdminDto.AdminDTO> createAdmin(@RequestBody AdminDto.AdminDTO adminDTO) {
            return ResponseEntity.ok(adminService.createAdmin(adminDTO));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
            adminService.deleteAdmin(id);
            return ResponseEntity.noContent().build();
        }
    }

