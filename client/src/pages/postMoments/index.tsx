import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import * as S from './index.styles';
import { Card, Category, SimpleCard } from '@/shared/ui';
import { theme } from '@/app/styles/theme';
import { Gift, MessageSquare, Send } from 'lucide-react';

// 임시 데이터(추후 제거 예정)
const postMomentsData = [
  {
    id: 1,
    category: '위로가 필요해요',
    timeStamp: '2시간 전',
    moment: '오늘 첫 면접에서 떨어졌어요. 너무 실망스럽고 자신감이 없어져요. 위로 받고 싶어요.',
    comment: '새로운 시작을 축하드려요! 분명 좋은 일들이 기다리고 있을 거에요. 화이팅!',
    emojis: '😆',
  },
  {
    id: 2,
    category: '일상 공유',
    timeStamp: '1일 전',
    moment:
      '오늘 드디어 새로운 직장에 첫 출근을 했어요. 떨리자만 설레는 마음으로 새로운 시작을 하게 되었습니다.',
    comment: '새로운 시작을 축하드려요! 분명 좋은 일들이 기다리고 있을 거에요. 화이팅!',
    emojis: '👍',
  },
];

export default function PostMomentsPage() {
  return (
    <S.PostMomentsPageContainer>
      <TitleContainer title="보낸 모멘트" subtitle="내가 보낸 공감을 확인해보세요" />
      <S.MomentsContainer>
        {postMomentsData.map(post => (
          <Card width="large">
            <Card.TitleContainer
              title={
                <S.TitleWrapper>
                  <Category text={post.category} />
                  <S.TimeStamp>{post.timeStamp}</S.TimeStamp>
                </S.TitleWrapper>
              }
              subtitle={''} // TODO: subtitle - 옵션으로 수정 필요
            />
            <Card.Content>
              <S.ContentContainer>
                <S.TitleContainer>
                  <MessageSquare size={20} color={theme.colors['yellow-500']} />
                  <span>원본 모멘트</span>
                </S.TitleContainer>
                <SimpleCard height="small" content={post.moment} />
              </S.ContentContainer>
              <S.ContentContainer>
                <S.TitleContainer>
                  <Send size={20} color={theme.colors['yellow-500']} />
                  <span>내가 보낸 공감</span>
                </S.TitleContainer>
                <SimpleCard height="small" content={post.comment} backgroundColor="yellow-300_10" />
              </S.ContentContainer>
              <S.ContentContainer>
                <S.TitleContainer>
                  <Gift size={20} color={theme.colors['yellow-500']} />
                  <span>받은 스티커</span>
                </S.TitleContainer>
                <S.Emoji>{post.emojis}</S.Emoji>
              </S.ContentContainer>
            </Card.Content>
          </Card>
        ))}
      </S.MomentsContainer>
    </S.PostMomentsPageContainer>
  );
}
