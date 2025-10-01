package moment.moment.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.infrastructure.MomentImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentImageService {

    private final MomentImageRepository momentImageRepository;

    @Transactional
    public Optional<MomentImage> create(MomentCreateRequest request, Moment moment) {
        if (request.imageName() == null || request.imageUrl() == null) {
            return Optional.empty();
        }
        MomentImage momentImageWithoutId = new MomentImage(moment, request.imageUrl(), request.imageName());
        return Optional.of(momentImageRepository.save(momentImageWithoutId));
    }

    public Optional<MomentImage> findMomentImage(Moment moment) {
        return momentImageRepository.findByMoment(moment);
    }

    public Map<Moment, MomentImage> getMomentImageByMoment(List<Moment> momentsWithinCursor) {
        List<MomentImage> momentImages = momentImageRepository.findAllByMomentIn(momentsWithinCursor);

        Map<Moment, MomentImage> momentMomentImageMap = momentImages.stream().collect(Collectors.toMap(
                MomentImage::getMoment,
                momentImage -> momentImage,
                (existing, replacement) -> existing
        ));

        Map<Moment, MomentImage> results = new HashMap<>();
        
        for (Moment moment : momentsWithinCursor) {
            results.put(moment, momentMomentImageMap.get(moment));
        }

        return results;
    }

    @Transactional
    public void deleteByMoment(Moment moment) {
        momentImageRepository.deleteByMoment(moment);
    }
}
