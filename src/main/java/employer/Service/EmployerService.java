package employer.Service;

import employer.DTO.EmployerDTO;
import employer.EmployerEntity;
import employer.Mapper.EmployerMapper;
import employer.Repository.EmployerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployerService {

    private final EmployerRepository repository;
    private final EmployerMapper mapper;

    public EmployerDTO createJob(EmployerDTO dto) {
        EmployerEntity entity = mapper.toEntity(dto);
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
        repository.deleteById(id);
    }
}

