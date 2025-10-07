package moment.comment.service.facade;

import lombok.RequiredArgsConstructor;
import moment.comment.dto.response.MyCommentPageResponse;
import moment.comment.service.tobe.application.CommentApplicationService;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyCommentPageFacadeService {
    
    private final CommentApplicationService commentApplicationService;
    
    public MyCommentPageResponse getMyCommentsPage(String nextCursor, int limit, Long commenterId) {
        // Todo : 1.코멘트 조회 2.모멘트 조회(List<Long>코멘트id) 3,알림조회  4.합친다.
        Cursor cursor = new Cursor(nextCursor);
        PageSize pageSize = new PageSize(limit);
        
        commentApplicationService.getMyCommentCompositions(cursor, pageSize, commenterId);
        
    }
}
