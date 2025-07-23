import { theme } from '@/app/styles/theme';
import { Card, SimpleCard } from '@/shared/ui';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import { Gift, MessageSquare, Send } from 'lucide-react';
import * as S from './index.styles';
import { useCommentsQuery } from '@/features/comment/hooks/useCommentsQuery';

export default function PostCommentsPage() {
  const { data: commentsResponse, isLoading, error } = useCommentsQuery();

  if (isLoading) return <div>로딩 중...</div>;
  if (error) return <div>에러가 발생했습니다.</div>;
  if (!commentsResponse?.data || !Array.isArray(commentsResponse.data)) {
    return <div>데이터가 없습니다.</div>;
  }

  return (
    <S.PostCommentsPageContainer>
      <TitleContainer title="보낸 코멘트" subtitle="내가 보낸 공감을 확인해보세요" />
      <S.MomentsContainer>
        {commentsResponse?.data.map(post => (
          <Card width="large" key={`${post.createdAt}-${post.content.slice(0, 10)}`}>
            <Card.TitleContainer
              title={
                <S.TitleWrapper>
                  <S.TimeStamp>{new Date(post.createdAt).toLocaleDateString()}</S.TimeStamp>
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
                <SimpleCard height="small" content={post.moment.content} />
              </S.ContentContainer>
              <S.ContentContainer>
                <S.TitleContainer>
                  <Send size={20} color={theme.colors['yellow-500']} />
                  <span>내가 보낸 공감</span>
                </S.TitleContainer>
                <SimpleCard height="small" content={post.content} backgroundColor="yellow-300_10" />
              </S.ContentContainer>
              <S.ContentContainer>
                <S.TitleContainer>
                  <Gift size={20} color={theme.colors['yellow-500']} />
                  <span>받은 스티커</span>
                </S.TitleContainer>
                <S.Emoji>
                  {post.emojis?.map(emoji => emoji.emojiType).join(' ') || '스티커가 없습니다'}
                </S.Emoji>
              </S.ContentContainer>
            </Card.Content>
          </Card>
        ))}
      </S.MomentsContainer>
    </S.PostCommentsPageContainer>
  );
}
