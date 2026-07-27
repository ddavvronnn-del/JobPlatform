package uz.imaan.jobplatform.employer.Service;

import uz.imaan.jobplatform.employer.DTO.EmployerDTO;
import uz.imaan.jobplatform.employer.EmployerEntity;
import uz.imaan.jobplatform.employer.Mapper.EmployerMapper;
import uz.imaan.jobplatform.employer.Repository.EmployerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
@RequiredArgsConstructor
public class EmployerService {

    private final EmployerRepository repository;
    private final EmployerMapper mapper;

    public EmployerDTO createJob(EmployerDTO dto) {
        EmployerEntity entity = mapper.toEntity(dto);

        if (dto.getInn() != null && dto.getPassportSeriesNumber() != null) {
            entity.setFullName(fetchFullNameFromGovRegistry(dto.getPassportSeriesNumber(), dto.getInn()));
        }

        entity.setCreatedAt(LocalDateTime.now());

        if (entity.getStatus() == null) {
            entity.setStatus("ACTIVE");
        }

        EmployerEntity saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    public List<EmployerDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public EmployerDTO getById(Long id) {
        EmployerEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("E'lon topilmadi: " + id));
        return mapper.toDTO(entity);
    }

    public List<EmployerDTO> getByEmployerChatId(Long chatId) {
        return repository.findByEmployerChatId(chatId).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public void deleteJob(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("E'lon topilmadi: " + id);
        }
        repository.deleteById(id);
    }

    private String fetchFullNameFromGovRegistry(String passport, String inn) {
        return "Shoxrux Sultanboyev";
    }
}

