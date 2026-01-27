package moment.like.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.group.domain.GroupMember;
import moment.like.domain.MomentLike;
import moment.like.dto.event.MomentLikeEvent;
import moment.like.infrastructure.MomentLikeRepository;
import moment.moment.domain.Moment;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentLikeService {

    private final MomentLikeRepository likeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public boolean toggle(Moment moment, GroupMember member) {
        Optional<MomentLike> existing = likeRepository
            .findByMomentIdAndMemberIdIncludeDeleted(moment.getId(), member.getId());

        boolean isNowLiked;

        if (existing.isPresent()) {
            MomentLike like = existing.get();
            like.toggleDeleted();
            isNowLiked = !like.isDeleted();
        } else {
            MomentLike newLike = new MomentLike(moment, member);
            likeRepository.save(newLike);
            isNowLiked = true;
        }

        if (isNowLiked && !moment.getMomenter().getId().equals(member.getUser().getId())) {
            eventPublisher.publishEvent(new MomentLikeEvent(
                moment.getId(),
                moment.getMomenter().getId(),
                member.getId(),
                member.getNickname()
            ));
        }

        return isNowLiked;
    }

    public long getCount(Long momentId) {
        return likeRepository.countByMomentId(momentId);
    }

    public boolean hasLiked(Long momentId, Long memberId) {
        return likeRepository.existsByMomentIdAndMemberId(momentId, memberId);
    }
}
