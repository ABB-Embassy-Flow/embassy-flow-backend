package az.abb.embassyflow.embassy.service;

import az.abb.embassyflow.embassy.dao.entity.Embassy;
import az.abb.embassyflow.embassy.dao.repository.EmbassyRepository;
import az.abb.embassyflow.embassy.dto.response.EmbassyResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmbassyService {

    private final EmbassyRepository embassyRepository;

    public EmbassyService(EmbassyRepository embassyRepository) {
        this.embassyRepository = embassyRepository;
    }

    @Transactional(readOnly = true)
    public List<EmbassyResponse> listActive() {
        return embassyRepository.findAllByActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return embassyRepository.existsById(id);
    }

    private EmbassyResponse toResponse(Embassy embassy) {
        return new EmbassyResponse(
                embassy.getId(),
                embassy.getName(),
                embassy.getCountry(),
                embassy.getCity(),
                embassy.getLanguage());
    }
}