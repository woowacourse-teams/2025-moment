import { theme } from '@/app/styles/theme';
import { useCommentsQuery } from '@/features/comment/hooks/useCommentsQuery';
import { emojiMapping } from '@/features/emoji/utils/emojiMapping';
import { Card, CommonSkeletonCard, NotFound, SimpleCard } from '@/shared/ui';
import { Gift, MessageSquare, Send } from 'lucide-react';
import * as S from './index.styles';

export default function PostCommentsList() {
  const { data: commentsResponse, isLoading, error } = useCommentsQuery();

  if (isLoading) {
    return (
      <S.MomentsContainer>
        {Array.from({ length: 3 }).map((_, index) => (
          <CommonSkeletonCard key={`comments-skeleton-card-${index}`} variant="comment" />
        ))}
      </S.MomentsContainer>
    );
  }

  if (error) {
    return <div>오류가 발생했습니다.</div>;
  }

  const comments = commentsResponse?.data || [];
  const hasComments = comments?.length && comments.length > 0;
  return (
    <>
      {' '}
      {hasComments ? (
        <S.MomentsContainer>
          {comments.map(post => (
            <Card width="medium" key={`card-${post.id}`}>
              <Card.TitleContainer
                title={
                  <S.TitleWrapper>
                    <Gift size={16} color={theme.colors['gray-400']} />
                    <S.TimeStamp>{new Date(post.createdAt).toLocaleDateString()}</S.TimeStamp>
                  </S.TitleWrapper>
                }
                subtitle={post.moment.content}
              />
              <Card.Content>
                <S.ContentContainer>
                  <S.TitleContainer>
                    <MessageSquare size={20} color={theme.colors['yellow-500']} />
                    <span>보낸 코멘트</span>
                  </S.TitleContainer>
                  <SimpleCard height="small" content={post.content} />
                </S.ContentContainer>
                <S.ContentContainer>
                  <S.TitleContainer>
                    <Send size={20} color={theme.colors['yellow-500']} />
                    <span>받은 리액션</span>
                  </S.TitleContainer>
                  <S.Emoji>
                    {(post.emojis || []).map(emoji => emojiMapping(emoji.emojiType)).join(' ')}
                  </S.Emoji>
                </S.ContentContainer>
              </Card.Content>
            </Card>
          ))}
        </S.MomentsContainer>
      ) : (
        <NotFound
          title="아직 작성한 코멘트가 없어요"
          subtitle="다른 사용자의 모멘트에 따뜻한 공감을 보내보세요"
        />
      )}
    </>
  );
}
