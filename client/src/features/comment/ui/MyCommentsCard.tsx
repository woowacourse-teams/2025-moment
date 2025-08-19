import { theme } from '@/app/styles/theme';
import { echoMapping } from '@/features/echo/utils/echoMapping';
import { Button, Card, SimpleCard } from '@/shared/ui';
import { Heart, Send } from 'lucide-react';
import * as S from './MyCommentsCard.styles';
import type { CommentWithNotifications } from '../types/commentsWithNotifications';
import { useReadNotifications } from '@/features/notification/hooks/useReadNotifications';
import { EchoTypeKey } from '@/features/echo/type/echos';
import { useToast } from '@/shared/hooks';

const ECHO_REWARD_POINT = 3;
import { WriterInfo } from '@/widgets/writerInfo';

export const MyCommentsCard = ({ myComment }: { myComment: CommentWithNotifications }) => {
  const { showSuccess } = useToast();
  const { handleReadNotifications, isLoading: isReadingNotification } = useReadNotifications();

  const handleCommentOpen = () => {
    if (myComment.read || isReadingNotification) return;
    if (myComment.notificationId) {
      handleReadNotifications(myComment.notificationId);

      if (myComment.echos && myComment.echos.length > 0) {
        showSuccess(`별조각 ${ECHO_REWARD_POINT} 개를 획득했습니다!`);
      }
    }
  };

  return (
    <Card width="medium" key={`card-${myComment.id}`} shadow={!myComment.read}>
      <Card.TitleContainer
        title={
          <S.TitleWrapper>
            <WriterInfo writer={myComment.moment.nickName} level={myComment.moment.level} />
            <S.TimeStamp>{new Date(myComment.createdAt).toLocaleDateString()}</S.TimeStamp>
          </S.TitleWrapper>
        }
        subtitle={myComment.moment.content}
      />
      <Card.Content>
        <S.ContentContainer>
          <S.TitleContainer>
            <Send size={20} color={theme.colors['yellow-500']} />
            <p>보낸 코멘트</p>
          </S.TitleContainer>
          <SimpleCard height="small" content={myComment.content} />
        </S.ContentContainer>
        <S.ContentContainer>
          <S.TitleContainer>
            <Heart size={20} color={theme.colors['yellow-500']} />
            <p>받은 리액션</p>
          </S.TitleContainer>
          <S.Emoji>
            {(myComment.echos || [])
              .map(echo => echoMapping(echo.emojiType as EchoTypeKey))
              .join(' ')}
          </S.Emoji>
          {!myComment.read && <Button onClick={handleCommentOpen} title="확인" />}
        </S.ContentContainer>
      </Card.Content>
    </Card>
  );
};
