package moment.like.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.group.domain.GroupMember;
import moment.like.domain.CommentLike;
import moment.like.dto.event.CommentLikeEvent;
import moment.like.infrastructure.CommentLikeRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentLikeService {

    private final CommentLikeRepository likeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public boolean toggle(Comment comment, GroupMember member) {
        Optional<CommentLike> existing = likeRepository
            .findByCommentIdAndMemberIdIncludeDeleted(comment.getId(), member.getId());

        boolean isNowLiked;

        if (existing.isPresent()) {
            CommentLike like = existing.get();
            like.toggleDeleted();
            isNowLiked = !like.isDeleted();
        } else {
            CommentLike newLike = new CommentLike(comment, member);
            likeRepository.save(newLike);
            isNowLiked = true;
        }

        if (isNowLiked && !comment.getCommenter().getId().equals(member.getUser().getId())) {
            eventPublisher.publishEvent(new CommentLikeEvent(
                comment.getId(),
                comment.getCommenter().getId(),
                member.getId(),
                member.getNickname()
            ));
        }

        return isNowLiked;
    }

    public long getCount(Long commentId) {
        return likeRepository.countByCommentId(commentId);
    }

    public boolean hasLiked(Long commentId, Long memberId) {
        return likeRepository.existsByCommentIdAndMemberId(commentId, memberId);
    }
}
