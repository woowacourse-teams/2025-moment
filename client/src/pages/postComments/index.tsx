import { theme } from '@/app/styles/theme';
import { emojiMapping } from '@/features/emoji/utils/emojiMapping';
import { Button, Card, CommonSkeletonCard, NotFound, SimpleCard } from '@/shared/ui';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import { Gift, MessageSquare, Send } from 'lucide-react';
import * as S from './index.styles';
import { useCommentsWithNotifications } from '@/features/comment/hooks/useCommentsWithNotifications';
import { useReadNotifications } from '@/features/notification/hooks/useReadNotifications';

export default function PostCommentsPage() {
  const { commentsWithNotifications, isLoading, error } = useCommentsWithNotifications();
  const { handleReadNotifications, isLoading: isReadingNotification } = useReadNotifications();

  const handleCommentOpen = (commentId: number) => {
    if (isReadingNotification) return;
    const comment = commentsWithNotifications.find(comment => comment.id === commentId);
    if (comment?.notificationId) {
      handleReadNotifications(comment.notificationId);
    }
  };

  if (isLoading) {
    return (
      <S.PostCommentsPageContainer>
        <TitleContainer title="보낸 코멘트" subtitle="내가 보낸 공감을 확인해보세요" />
        <S.MomentsContainer>
          {Array.from({ length: 3 }).map((_, index) => (
            <CommonSkeletonCard key={`comments-skeleton-card-${index}`} variant="comment" />
          ))}
        </S.MomentsContainer>
      </S.PostCommentsPageContainer>
    );
  }

  if (error) {
    return <div>오류가 발생했습니다.</div>;
  }

  return (
    <S.PostCommentsPageContainer>
      <TitleContainer title="보낸 코멘트" subtitle="내가 보낸 공감을 확인해보세요" />
      {commentsWithNotifications.length > 0 ? (
        <S.MomentsContainer>
          {commentsWithNotifications.map(comment => (
            <Card width="medium" key={`card-${comment.id}`} shadow={!comment.read}>
              <Card.TitleContainer
                title={
                  <S.TitleWrapper>
                    <Gift size={16} color={theme.colors['gray-400']} />
                    <S.TimeStamp>{new Date(comment.createdAt).toLocaleDateString()}</S.TimeStamp>
                  </S.TitleWrapper>
                }
                subtitle={comment.moment.content}
              />
              <Card.Content>
                <S.ContentContainer>
                  <S.TitleContainer>
                    <MessageSquare size={20} color={theme.colors['yellow-500']} />
                    <span>보낸 코멘트</span>
                  </S.TitleContainer>
                  <SimpleCard height="small" content={comment.content} />
                </S.ContentContainer>
                <S.ContentContainer>
                  <S.TitleContainer>
                    <Send size={20} color={theme.colors['yellow-500']} />
                    <span>받은 리액션</span>
                  </S.TitleContainer>
                  <S.Emoji>
                    {(comment.emojis || []).map(emoji => emojiMapping(emoji.emojiType)).join(' ')}
                  </S.Emoji>
                  {/* TODO: 임시방편.추후 코멘트 모달 버튼으로 대체 */}
                  {!comment.read && (
                    <Button onClick={() => handleCommentOpen(comment.id)} title="읽음 처리" />
                  )}
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
    </S.PostCommentsPageContainer>
  );
}
