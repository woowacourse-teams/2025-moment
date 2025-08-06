import { CommentItem } from '@/features/comment/types/comments';
import { MyCommentsCard } from '@/features/comment/ui/MyCommentsCard';
import { NotFound } from '@/shared/ui';
import styled from '@emotion/styled';

interface MyCommentsListProps {
  myComments: CommentItem[];
  observerRef: React.RefObject<HTMLDivElement | null>;
}

export const MyCommentsList = ({ myComments, observerRef }: MyCommentsListProps) => {
  const hasComments = myComments?.length > 0;

  return (
    <>
      {hasComments ? (
        <MyCommentsListContainer>
          {myComments.map(myComment => (
            <MyCommentsCard key={myComment.id} myComment={myComment} />
          ))}

          <div ref={observerRef} style={{ height: '1px' }} />
        </MyCommentsListContainer>
      ) : (
        <NotFound
          title="아직 작성한 코멘트가 없어요"
          subtitle="다른 사용자의 모멘트에 따뜻한 공감을 보내보세요"
        />
      )}
    </>
  );
};

const MyCommentsListContainer = styled.section`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
`;
