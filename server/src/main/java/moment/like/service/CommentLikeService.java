package moment.like.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.group.domain.GroupMember;
import moment.like.domain.CommentLike;
import moment.like.dto.event.CommentLikeEvent;
import moment.like.infrastructure.CommentLikeRepository;
import moment.user.domain.User;
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

        User commenter = comment.getCommenter();
        if (isNowLiked && commenter != null && !commenter.getId().equals(member.getUser().getId())) {
            eventPublisher.publishEvent(CommentLikeEvent.of(comment, member));
        }

        return isNowLiked;
    }

    public long getCount(Long commentId) {
        return likeRepository.countByCommentId(commentId);
    }

    public boolean hasLiked(Long commentId, Long memberId) {
        return likeRepository.existsByCommentIdAndMemberId(commentId, memberId);
    }

    public Map<Long, Long> getCountsByCommentIds(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Object[]> results = likeRepository.countByCommentIdIn(commentIds);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    public Set<Long> getLikedCommentIds(List<Long> commentIds, Long memberId) {
        if (commentIds == null || commentIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(likeRepository.findLikedCommentIds(commentIds, memberId));
    }
}
