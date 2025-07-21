import { Button, Card, Category, Text } from '@/shared/ui';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import { Send } from 'lucide-react';
import * as S from './index.styles';
import { theme } from '@/app/styles/theme';

// 임시 데이터(추후 제거 예정)
const myMomentsData = [
  {
    id: 1,
    category: '위로가 필요해요',
    timeStamp: '2시간 전',
    title: '오늘 첫 면접에서 떨어졌어요. 너무 실망스럽고 자신감이 없어져요. 위로 받고 싶어요.',
    content: '',
  },
  {
    id: 2,
    category: '일상 공유',
    timeStamp: '1일 전',
    title:
      '오늘 드디어 새로운 직장에 첫 출근을 했어요. 떨리자만 설레는 마음으로 새로운 시작을 하게 되었습니다.',
    content: '새로운 시작을 축하드려요! 분명 좋은 일들이 기다리고 있을 거에요. 화이팅!',
  },
  {
    id: 3,
    category: '칭찬 받고 싶어요',
    timeStamp: '1주일 전',
    title:
      '6개월 동안 준비한 자격증 시험에 합격했어요! 정말 기쁘고 누군가와 이 기쁨을 나누고 싶어요.',
    content: '축하합니다! 6개월이라는 긴 시간 동안 포기하지 않고 노력하신 모습이 정말 멋져요!',
  },
];

export default function MyMoments() {
  return (
    <S.MyMomentsPageContainer>
      <TitleContainer
        title="나의 모멘트"
        subtitle="내가 공유한 모멘트와 받은 공감을 확인해보세요"
      />
      <S.MomentsContainer>
        {myMomentsData.map(moment => (
          <Card width="large">
            <Card.TitleContainer
              title={
                <S.TitleWrapper>
                  <Category text={moment.category} />
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
              <Text height="small" content={moment.content} />
            </Card.Content>
            <Card.Action position="space-between">
              <Button variant="sendEmojis" title="+ 스티커 보내기" />
            </Card.Action>
          </Card>
        ))}
      </S.MomentsContainer>
    </S.MyMomentsPageContainer>
  );
}
