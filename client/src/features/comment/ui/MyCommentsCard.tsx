import { theme } from '@/app/styles/theme';
import { emojiMapping } from '@/features/emoji/utils/emojiMapping';
import { Button, Card, SimpleCard } from '@/shared/ui';
import { Gift, MessageSquare, Send } from 'lucide-react';
import * as S from './MyCommentsCard.styles';
import type { CommentWithNotifications } from '../types/commentsWithNotifications';
import { useReadNotifications } from '@/features/notification/hooks/useReadNotifications';

export const MyCommentsCard = ({ myComment }: { myComment: CommentWithNotifications }) => {
  const { handleReadNotifications, isLoading: isReadingNotification } = useReadNotifications();

  const handleCommentOpen = () => {
    if (myComment.read || isReadingNotification) return;
    if (myComment.notificationId) {
      handleReadNotifications(myComment.notificationId);
    }
  };

  return (
    <Card width="medium" key={`card-${myComment.id}`} shadow={!myComment.read}>
      <Card.TitleContainer
        title={
          <S.TitleWrapper>
            <Gift size={16} color={theme.colors['gray-400']} />
            <S.TimeStamp>{new Date(myComment.createdAt).toLocaleDateString()}</S.TimeStamp>
          </S.TitleWrapper>
        }
        subtitle={myComment.moment.content}
      />
      <Card.Content>
        <S.ContentContainer>
          <S.TitleContainer>
            <MessageSquare size={20} color={theme.colors['yellow-500']} />
            <span>보낸 코멘트</span>
          </S.TitleContainer>
          <SimpleCard height="small" content={myComment.content} />
        </S.ContentContainer>
        <S.ContentContainer>
          <S.TitleContainer>
            <Send size={20} color={theme.colors['yellow-500']} />
            <span>받은 리액션</span>
          </S.TitleContainer>
          <S.Emoji>
            {(myComment.emojis || []).map(emoji => emojiMapping(emoji.emojiType)).join(' ')}
          </S.Emoji>
          {/* TODO: 임시방편.추후 코멘트 모달 버튼으로 대체 */}
          {!myComment.read && <Button onClick={handleCommentOpen} title="확인" />}
        </S.ContentContainer>
      </Card.Content>
    </Card>
  );
};
