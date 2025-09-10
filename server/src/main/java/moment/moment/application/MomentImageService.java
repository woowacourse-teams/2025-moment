package moment.moment.application;

import java.util.Optional;
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
        return momentImageRepository.findMomentImageByMoment(moment);
    }
}
