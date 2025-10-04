package moment.moment.service.tobe.moment;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.moment.infrastructure.MomentImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentImageService {

    private final MomentImageRepository momentImageRepository;

    @Transactional
    public Optional<MomentImage> create(Moment moment, String imageUrl, String imageName) {
        Optional<MomentImage> momentImage = MomentImage.createNew(moment, imageUrl, imageName);
        momentImage.ifPresent(momentImageRepository::save);
        return momentImage;
    }

    public Map<Moment, MomentImage> getMomentImageByMoment(List<Moment> moments) {
        return momentImageRepository.findAllByMomentIn(moments).stream()
                .collect(Collectors.toMap(MomentImage::getMoment, momentImage -> momentImage));
    }

    public Optional<MomentImage> findMomentImage(Moment moment) {
        return momentImageRepository.findByMoment(moment);
    }

    @Transactional
    public void deleteBy(Long momentId) {
        momentImageRepository.deleteByMomentId(momentId);
    }
}
