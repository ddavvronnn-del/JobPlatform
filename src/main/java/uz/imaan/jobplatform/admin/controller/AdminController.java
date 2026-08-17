package uz.imaan.jobplatform.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.imaan.jobplatform.admin.dto.AdminDtoTwo;
import uz.imaan.jobplatform.admin.service.AdminService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<AdminDtoTwo> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping
    public ResponseEntity<List<AdminDtoTwo>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminDtoTwo> getAdminById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getAdminById(id));
    }

    @PostMapping
    public ResponseEntity<AdminDtoTwo> createAdmin(@RequestBody AdminDtoTwo adminDTO) {
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
    public ResponseEntity<String> blockUser(@RequestBody AdminDtoTwo blockDTO) {
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
    }

    @Operation(
            summary = "Получить подробную информацию об исполнителе",
            description = "Возвращает отформатированную карточку профиля исполнителя по его ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о профиле успешно получена"),
            @ApiResponse(responseCode = "404", description = "Исполнитель не найден")
    })
    @GetMapping("/workers/{userId}")
    public ResponseEntity<String> getWorkerDetails(@PathVariable Long userId) {
        String workerDetails = adminService.getWorkerDetails(userId);
        return ResponseEntity.ok(workerDetails);
    }
}