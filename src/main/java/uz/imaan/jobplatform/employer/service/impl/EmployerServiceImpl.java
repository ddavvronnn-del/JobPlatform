package uz.imaan.jobplatform.employer.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.imaan.jobplatform.employer.entity.EmployerEntity;
import uz.imaan.jobplatform.employer.dto.EmployerCreateDTO;
import uz.imaan.jobplatform.employer.dto.EmployerResponseDTO;
import uz.imaan.jobplatform.employer.dto.EmployerUpdateDTO;
import uz.imaan.jobplatform.employer.mapper.EmployerMapper;
import uz.imaan.jobplatform.employer.repository.EmployerRepository;
import uz.imaan.jobplatform.employer.service.interfacee.EmployerService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployerServiceImpl implements EmployerService {

    private final EmployerRepository repository;
    private final EmployerMapper mapper;

    @Override
    @Transactional
    public EmployerResponseDTO createJob(EmployerCreateDTO dto) {
        EmployerEntity entity = mapper.toEntity(dto);

        if (dto.getInn() != null && dto.getPassportSeriesNumber() != null) {
            entity.setFullName(fetchFullNameFromGovRegistry(dto.getPassportSeriesNumber(), dto.getInn()));
        }

        entity.setCreatedAt(LocalDateTime.now());

        if (entity.getStatus() == null) {
            entity.setStatus("ACTIVE");
        }

        EmployerEntity saved = repository.save(entity);
        return mapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployerResponseDTO getById(Long id) {
        EmployerEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("E'lon topilmadi: " + id));
        return mapper.toResponseDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployerResponseDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployerResponseDTO> getByEmployerChatId(Long chatId) {
        return repository.findByEmployerChatId(chatId).stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmployerResponseDTO updateJob(Long id, EmployerUpdateDTO dto) {
        EmployerEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("E'lon topilmadi: " + id));

        if (dto.getCategory() != null) entity.setCategory(dto.getCategory());
        if (dto.getJobType() != null) entity.setJobType(dto.getJobType());
        if (dto.getSalary() != null) entity.setSalary(dto.getSalary());
        if (dto.getWorkHours() != null) entity.setWorkHours(dto.getWorkHours());
        if (dto.getJobDate() != null) entity.setJobDate(dto.getJobDate());
        if (dto.getWorkerCount() != null) entity.setWorkerCount(dto.getWorkerCount());
        if (dto.getRequirements() != null) entity.setRequirements(dto.getRequirements());
        if (dto.getFoodProvided() != null) entity.setFoodProvided(dto.getFoodProvided());
        if (dto.getLatitude() != null) entity.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) entity.setLongitude(dto.getLongitude());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());

        EmployerEntity updated = repository.save(entity);
        return mapper.toResponseDTO(updated);
    }

    @Override
    @Transactional
    public void deleteJob(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("E'lon topilmadi: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public SendMessage handleEmployer(Message message) {
        return null;
    }

    private String fetchFullNameFromGovRegistry(String passport, String inn) {
        return "Shoxrux Sultanboyev";
    }
}