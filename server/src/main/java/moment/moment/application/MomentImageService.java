package moment.moment.application;

import lombok.RequiredArgsConstructor;
import moment.moment.infrastructure.MomentImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentImageService {

    private final MomentImageRepository momentImageRepository;


}
