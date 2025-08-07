import { theme } from '@/app/styles/theme';
import { CommentItem } from '@/features/comment/types/comments';
import { emojiMapping } from '@/features/emoji/utils/emojiMapping';
import { Card, SimpleCard } from '@/shared/ui';
import { Gift, MessageSquare, Send } from 'lucide-react';
import * as S from './MyCommentsCard.styles';

export const MyCommentsCard = ({ myComment }: { myComment: CommentItem }) => {
  return (
    <Card width="medium" key={`card-${myComment.id}`}>
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
        </S.ContentContainer>
      </Card.Content>
    </Card>
  );
};
