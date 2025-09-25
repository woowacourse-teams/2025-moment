package moment.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.moment.domain.MomentTag;
import moment.reply.domain.Echo;

public record MyCommentsResponse(List<MyCommentResponse> myCommentsResponse) {

    public static MyCommentsResponse of(
            List<Comment> comments,
            Map<Moment, List<MomentTag>> momentTagsOfMoment,
            Map<Moment, MomentImage> momentImagesOfMoment,
            Map<Comment, CommentImage> commentImagesOfComment
    ) {

        return new MyCommentsResponse(
                comments.stream()
                        .map(comment -> {
                            CommentImage commentImage = commentImagesOfComment.getOrDefault(comment, null);
                            Moment momentOfComment = comment.getMoment();
                            if (momentOfComment == null) {
                                return MyCommentResponse.from(comment, Collections.emptyList(), commentImage, null);

                            }

                            List<MomentTag> momentTags = momentTagsOfMoment.getOrDefault(
                                    momentOfComment,
                                    Collections.emptyList()
                            );
                            MomentImage momentImage = momentImagesOfMoment.getOrDefault(momentOfComment, null);
                            return MyCommentResponse.from(comment, momentTags, commentImage, momentImage);
                        })
                        .toList());
    }

    public static MyCommentsResponse of(
            List<Comment> comments,
            Map<Comment, List<Echo>> commentAndEchos,
            Map<Moment, List<MomentTag>> momentTagsOfMoment,
            Map<Moment, MomentImage> momentImagesOfMoment,
            Map<Comment, CommentImage> commentImagesOfComment
    ) {

        return new MyCommentsResponse(
                comments.stream()
                        .map(comment -> {
                            CommentImage commentImage = commentImagesOfComment.getOrDefault(comment, null);
                            List<Echo> echoes = commentAndEchos.getOrDefault(comment, Collections.emptyList());
                            Moment momentOfComment = comment.getMoment();
                            if (momentOfComment == null) {
                                return MyCommentResponse.from(comment, echoes, Collections.emptyList(), commentImage,
                                        null);
                            }

                            List<MomentTag> momentTags = momentTagsOfMoment.getOrDefault(momentOfComment,
                                    Collections.emptyList());

                            MomentImage momentImage = momentImagesOfMoment.getOrDefault(momentOfComment, null);
                            return MyCommentResponse.from(comment, echoes, momentTags, commentImage, momentImage);
                        }).toList());

    }

    @JsonValue
    public List<MyCommentResponse> getMyCommentsResponse() {
        return myCommentsResponse;
    }
}
