import { theme } from '@/app/styles/theme';
import { useDeleteEmoji } from '@/features/emoji/hooks/useDeleteEmoji';
import { Emoji } from '@/features/emoji/ui/Emoji';
import { EmojiButton } from '@/features/emoji/ui/EmojiButton';
import { emojiMapping } from '@/features/emoji/utils/emojiMapping';
import { Button, Card, NotFound, SimpleCard } from '@/shared/ui';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';
import { Send, Timer } from 'lucide-react';
import * as S from './MyMomentsList.styles';
import type { MomentWithNotifications } from '../types/momentsWithNotifications';
import { useReadNotifications } from '../../notification/hooks/useReadNotifications';

export const MyMomentsCard = ({ myMoment }: { myMoment: MomentWithNotifications }) => {
  const { handleDeleteEmoji } = useDeleteEmoji();
  const { handleReadNotifications, isLoading: isReadingNotification } = useReadNotifications();
  const emojis = myMoment.comment?.emojis || [];

  const handleMomentOpen = () => {
    if (myMoment.read || isReadingNotification) return;
    if (myMoment.notificationId) {
      handleReadNotifications(myMoment.notificationId);
    }
  };

  const getFormattedTime = (dateString: string) => {
    try {
      if (!dateString) return '시간 정보 없음';
      return formatRelativeTime(dateString);
    } catch (error) {
      console.error('Date formatting error:', error, 'dateString:', dateString);
      return '시간 정보 오류';
    }
  };

  return (
    <Card width="medium" key={myMoment.id} shadow={!myMoment.read}>
      <Card.TitleContainer
        title={
          <S.TitleWrapper>
            <Timer size={16} color={theme.colors['gray-400']} />
            <S.TimeStamp>{getFormattedTime(myMoment.createdAt)}</S.TimeStamp>
          </S.TitleWrapper>
        }
        subtitle={myMoment.content}
      />
      <Card.Content>
        <S.TitleContainer>
          <Send size={20} color={theme.colors['yellow-500']} />
          <span>받은 공감</span>
        </S.TitleContainer>
        <SimpleCard
          height="small"
          content={
            myMoment.comment?.content || (
              <NotFound
                title="아직 응답이 없어요."
                subtitle="곧 누군가가 따뜻한 응답을 보내줄 거예요."
                size="small"
              />
            )
          }
        />
      </Card.Content>
      <Card.Action position="space-between">
        {myMoment.comment?.content && emojis.length === 0 && (
          <EmojiButton commentId={myMoment.comment.id} />
        )}
        {myMoment.comment && (
          <S.EmojiContainer>
            {emojis.map(emoji => (
              <Emoji key={emoji.id} onClick={() => handleDeleteEmoji(emoji.id)}>
                {emojiMapping(emoji.emojiType)}
              </Emoji>
            ))}
          </S.EmojiContainer>
        )}
        {/* TODO: 임시방편.추후 모멘트 모달 버튼으로 대체 */}
        {!myMoment.read && <Button onClick={handleMomentOpen} title="확인" />}
      </Card.Action>
    </Card>
  );
};
