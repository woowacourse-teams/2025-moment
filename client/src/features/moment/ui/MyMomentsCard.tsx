import { theme } from '@/app/styles/theme';
import { useDeleteEmoji } from '@/features/emoji/hooks/useDeleteEmoji';
import { Emoji } from '@/features/emoji/ui/Emoji';
import { EmojiButton } from '@/features/emoji/ui/EmojiButton';
import { emojiMapping } from '@/features/emoji/utils/emojiMapping';
import { Card, NotFound, SimpleCard } from '@/shared/ui';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';
import { Send, Timer } from 'lucide-react';
import { MyMoments } from '../types/moments';
import * as S from './MyMomentsList.styles';

export const MyMomentsCard = ({ myMoment, index }: { myMoment: MyMoments; index: number }) => {
  const { handleDeleteEmoji } = useDeleteEmoji();
  const emojis = myMoment.comment?.emojis || [];

  return (
    <Card width="large" key={index}>
      {/* TODO: 추후 key값에 id 값으로 변경 필요 */}
      <Card.TitleContainer
        title={
          <S.TitleWrapper>
            {/* TODO: 추후 Icon 컴포넌트로 변경 필요 */}
            <Timer size={16} color={theme.colors['gray-400']} />{' '}
            <S.TimeStamp>{formatRelativeTime(myMoment.createdAt)}</S.TimeStamp>
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
      </Card.Action>
    </Card>
  );
};
