package moment.comment.service.application;

import moment.comment.dto.tobe.CommentCompositions;

@FunctionalInterface
public interface CommentComposable {
    CommentCompositions generate();
}
