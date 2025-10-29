package moment.moment.service.moment;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
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

    private static final int COMMENTABLE_PERIOD_IN_DAYS = 7;
    private static final Random RANDOM = new Random();

    private final MomentRepository momentRepository;

    @Transactional
    public Moment create(String content, User momenter, WriteType writeType) {
        Moment moment = new Moment(content, momenter, writeType);
        return momentRepository.save(moment);
    }

    public List<Moment> getMomentsBy(User momenter, Cursor cursor, PageSize pageSize) {
        PageRequest pageable = pageSize.getPageRequest();
        if (cursor.isFirstPage()) {
            return momentRepository.findMyMomentFirstPage(momenter, pageable);
        }
        return momentRepository.findMyMomentsNextPage(momenter, cursor.dateTime(), cursor.id(), pageable);
    }

    public List<Moment> getUnreadMomentsBy(List<Long> unreadMomentIds, Cursor cursor, PageSize pageSize) {
        PageRequest pageable = pageSize.getPageRequest();
        if (cursor.isFirstPage()) {
            return momentRepository.findMyUnreadMomentFirstPage(unreadMomentIds, pageable);
        }

        return momentRepository.findMyUnreadMomentNextPage(
                unreadMomentIds,
                cursor.dateTime(),
                cursor.id(),
                pageable);
    }

    public List<Moment> getCommentableMoments(User user, List<Long> reportedMomentIds) {
        LocalDateTime cutoffDateTime = LocalDateTime.now().minusDays(COMMENTABLE_PERIOD_IN_DAYS);

        List<Long> momentIds = momentRepository.findMomentIdsExcludingReported(
                user.getId(),
                cutoffDateTime,
                reportedMomentIds
        );

        if (reportedMomentIds == null || reportedMomentIds.isEmpty()) {
            momentIds = momentRepository.findMomentIdsWithoutReported(user.getId(), cutoffDateTime);
        }

        if (momentIds.isEmpty()) {
            return Collections.emptyList();
        }

        int randomIndex = RANDOM.nextInt(momentIds.size());
        Long randomId = momentIds.get(randomIndex);

        Optional<Moment> momentOptional = momentRepository.findById(randomId);

        return momentOptional
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }

    public List<Moment> getMomentsBy(List<Long> momentIds) {
        return momentRepository.findAllWithMomenterByIds(momentIds);
    }

    @Transactional
    public void deleteBy(Long momentId) {
        momentRepository.deleteById(momentId);
    }

    public boolean existsMoment(Long momentId) {
        return momentRepository.existsById(momentId);
    }

    public void validateMomenter(Long momentId, User momenter) {
        Moment moment = getMomentBy(momentId);

        if (moment.isNotSame(momenter)) {
            throw new MomentException(ErrorCode.USER_UNAUTHORIZED);
        }
    }

    public Moment getMomentBy(Long momentId) {
        return momentRepository.findById(momentId)
                .orElseThrow(() -> new MomentException(ErrorCode.MOMENT_NOT_FOUND));
    }
}
