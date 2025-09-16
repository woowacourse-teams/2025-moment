package moment.comment.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.infrastructure.CommentImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentImageService {

    private final CommentImageRepository commentImageRepository;

    @Transactional
    public Optional<CommentImage> create(CommentCreateRequest request, Comment comment) {
        if (request.imageName() == null || request.imageUrl() == null) {
            return Optional.empty();
        }
        CommentImage commentImageWithoutId = new CommentImage(comment, request.imageUrl(), request.imageName());
        return Optional.of(commentImageRepository.save(commentImageWithoutId));
    }

    public Map<Comment, CommentImage> getCommentImageByMoment(List<Comment> comments) {
        List<CommentImage> momentImages = commentImageRepository.findAllByCommentIn(comments);

        Map<Comment, CommentImage> commentCommentImageMap = momentImages.stream().collect(Collectors.toMap(
                CommentImage::getComment,
                commentImage -> commentImage,
                (existing, replacement) -> existing
        ));

        Map<Comment, CommentImage> results = new HashMap<>();

        for (Comment comment : comments) {
            results.put(comment, commentCommentImageMap.get(comment));
        }

        return results;
    }

    public void deleteByComment(Comment comment) {
        commentImageRepository.deleteByComment(comment);
    }
}
