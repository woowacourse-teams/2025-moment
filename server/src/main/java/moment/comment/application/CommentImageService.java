package moment.comment.application;

import java.util.Optional;
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
}
