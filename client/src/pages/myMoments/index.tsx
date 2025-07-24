import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
<<<<<<< HEAD
=======
import { Send, Timer } from 'lucide-react';
import { NotFoundComments } from '../../features/comment/ui/NotFoundComments';
import { EmojiButton } from '../../features/emoji/ui/EmojiButton';
>>>>>>> 9f0ca0624ec697ca15a524299e0180d6ae355870
import * as S from './index.styles';
import { MyMomentsList } from '@/features/moment/ui/MyMomentsList';

export default function MyMoments() {
  return (
    <S.MyMomentsPageContainer>
      <TitleContainer
        title="나의 모멘트"
        subtitle="내가 공유한 모멘트와 받은 공감을 확인해보세요"
      />
      <S.MomentsContainer>
        {myMomentsData.map(moment => (
          <Card width="large" key={moment.id}>
            <Card.TitleContainer
              title={
                <S.TitleWrapper>
                  <Timer size={16} color={theme.colors['gray-400']} />
                  <S.TimeStamp>{moment.timeStamp}</S.TimeStamp>
                </S.TitleWrapper>
              }
              subtitle={moment.title}
            />
            <Card.Content>
              <S.TitleContainer>
                <Send size={20} color={theme.colors['yellow-500']} />
                <span>받은 공감</span>
              </S.TitleContainer>
              <SimpleCard height="small" content={moment.content || <NotFoundComments />} />
            </Card.Content>
            <Card.Action position="space-between">
              <EmojiButton />
            </Card.Action>
          </Card>
        ))}
      </S.MomentsContainer>
    </S.MyMomentsPageContainer>
  );
}
