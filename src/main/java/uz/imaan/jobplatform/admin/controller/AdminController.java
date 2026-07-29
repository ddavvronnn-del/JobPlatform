package uz.imaan.jobplatform.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import uz.imaan.jobplatform.admin.dto.AdminDTO;
import uz.imaan.jobplatform.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/admins")
@RequiredArgsConstructor
public class AdminController {

        private final AdminService adminService;


    @GetMapping("/stats")
    public ResponseEntity<AdminDTO> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
        @GetMapping
        public ResponseEntity<List<AdminDTO>> getAllAdmins() {
            return ResponseEntity.ok(adminService.getAllAdmins());
        }

        @GetMapping("/{id}")
        public ResponseEntity<AdminDTO> getAdminById(@PathVariable Long id) {
            return ResponseEntity.ok(adminService.getAdminById(id));
        }

        @PostMapping
        public ResponseEntity<AdminDTO> createAdmin(@RequestBody AdminDTO adminDTO) {
            return ResponseEntity.ok(adminService.createAdmin(adminDTO));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
            adminService.deleteAdmin(id);
            return ResponseEntity.noContent().build();
        }

@Operation(
        summary = "Заблокировать пользователя",
        description = "Деактивирует аккаунт пользователя и сохраняет причину блокировки"
)
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Пользователь успешно заблокирован"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден")
})
@PutMapping("/users/block")
public ResponseEntity<String> blockUser(@RequestBody AdminDTO blockDTO) {
    adminService.blockUser(blockDTO);
    return ResponseEntity.ok("Пользователь успешно заблокирован");
}

@Operation(
        summary = "Разблокировать пользователя",
        description = "Снимает блокировку и восстанавливает активность аккаунта"
)
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Пользователь успешно разблокирован"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден")
})
@PutMapping("/users/unblock/{userId}")
public ResponseEntity<String> unblockUser(@PathVariable Long userId) {
    adminService.unblockUser(userId);
    return ResponseEntity.ok("Пользователь успешно разблокирован");
}}

