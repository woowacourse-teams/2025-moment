import { theme } from '@/app/styles/theme';
import { NotFoundComments } from '@/features/comment/ui/NotFoundComments';
import { EmojiButton } from '@/features/emoji/ui/EmojiButton';
import { Card, SimpleCard } from '@/shared/ui';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';
import { Send, Timer } from 'lucide-react';
import { emojiMapping } from '@/features/emoji/utils/emojiMapping';
import { Emoji } from '@/features/emoji/ui/Emoji';
import { Emoji as EmojiType, MyMoments } from '../types/moments';
import { useEffect, useState } from 'react';
import { useDeleteEmoji } from '@/features/emoji/hooks/useDeleteEmoji';
import * as S from './MyMomentsList.styles';

export const MyMomentsCard = ({ myMoment, index }: { myMoment: MyMoments; index: number }) => {
  const { handleDeleteEmoji } = useDeleteEmoji();

  // TODO: 추후 커스텀 훅으로 분리 예정
  const [emojiData, setEmojiData] = useState<EmojiType[]>([]);

  useEffect(() => {
    if (myMoment.comment?.emojis) {
      setEmojiData(myMoment.comment?.emojis);
    }
  }, [myMoment.comment?.emojis]);

  const handleDeleteEmojiData = (emojiId: number) => {
    handleDeleteEmoji(emojiId);
    const emojis = emojiData.filter(emoji => emoji.id !== emojiId);
    setEmojiData(emojis);
  };

  const handleAddEmojiData = (newEmoji: EmojiType) => {
    setEmojiData(prev => [...prev, newEmoji]);
  };

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
        <SimpleCard height="small" content={myMoment.comment?.content || <NotFoundComments />} />
      </Card.Content>
      <Card.Action position="space-between">
        {/* TODO: 이모지 딜리트 구현 */}
        {myMoment.comment?.content && (
          <EmojiButton commentId={myMoment.comment.id} onAddEmojiData={handleAddEmojiData} />
        )}
        {myMoment.comment && (
          <S.EmojiContainer>
            {emojiData.map(emoji => (
              <Emoji key={emoji.id} onClick={() => handleDeleteEmojiData(emoji.id)}>
                {emojiMapping(emoji.emojiType)}
              </Emoji>
            ))}
          </S.EmojiContainer>
        )}
      </Card.Action>
    </Card>
  );
};
