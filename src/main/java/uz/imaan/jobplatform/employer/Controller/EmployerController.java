package uz.imaan.jobplatform.employer.Controller;

import uz.imaan.jobplatform.employer.DTO.EmployerDTO;
import uz.imaan.jobplatform.employer.Service.EmployerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employer/jobs")
@RequiredArgsConstructor
public class EmployerController {

    private final EmployerService service;

    @PostMapping
    public ResponseEntity<EmployerDTO> create(@RequestBody EmployerDTO dto) {
        return ResponseEntity.ok(service.createJob(dto));
    }

    @GetMapping
    public ResponseEntity<List<EmployerDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployerDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/my-jobs/{chatId}")
    public ResponseEntity<List<EmployerDTO>> getByEmployerChatId(@PathVariable Long chatId) {
        return ResponseEntity.ok(service.getByEmployerChatId(chatId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
