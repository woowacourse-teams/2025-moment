import { theme } from '@/app/styles/theme';
import { Card, SimpleCard } from '@/shared/ui';
import { Send, Timer } from 'lucide-react';
import { useMomentsQuery } from '../hook/useMomentsQuery';
import * as S from './MyMomentsList.styles';
import { EmojiButton } from '@/features/emoji/ui/EmojiButton';
import { NotFoundComents } from '@/features/comment/ui/NotFoundComents';
import { MyMoments } from '../types/Moments';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';

export const MyMomentsList = () => {
  const { data } = useMomentsQuery();
  const myMoments = data?.data;

  return (
    <S.MomentsContainer>
      {myMoments?.map((myMoment: MyMoments, index: number) => (
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
            <SimpleCard height="small" content={myMoment.comment?.content || <NotFoundComents />} />
          </Card.Content>
          <Card.Action position="space-between">
            <EmojiButton />
          </Card.Action>
        </Card>
      ))}
    </S.MomentsContainer>
  );
};
