package moment.moment.service.tobe.moment;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentService {

    private final MomentRepository momentRepository;
    
    @Transactional
    public Moment create(String content, User momenter, WriteType writeType) {
        Moment moment = new Moment(content, momenter, writeType);
        return momentRepository.save(moment);
    }
    
    public List<Moment> getMyPage(User momenter, Cursor cursor, PageSize pageSize) {
        PageRequest pageable = pageSize.getPageRequest();
        if (cursor.isFirstPage()) {
            return momentRepository.findMyMomentFirstPage(momenter, pageable);
        }
        return momentRepository.findMyMomentsNextPage(momenter, cursor.dateTime(), cursor.id(), pageable);
    }
}
