import { theme } from '@/app/styles/theme';
import { useCommentsQuery } from '@/features/comment/hooks/useCommentsQuery';
import { useEmojisQuery } from '@/features/emoji/hooks/useEmojisQuery';
import { emojiMapping } from '@/features/emoji/utils/emojiMapping';
import { Card, CommonSkeletonCard, SimpleCard } from '@/shared/ui';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import { Gift, MessageSquare, Send } from 'lucide-react';
import * as S from './index.styles';
import { NotFoundMyComments } from './NotFoundMyComments';

export default function PostCommentsPage() {
  const { data: commentsResponse, isLoading, error } = useCommentsQuery();

  const commentIds = commentsResponse?.data?.map(post => post.id) || [];
  const emojiQueries = commentIds.map(id => useEmojisQuery(id));

  const getEmojiData = (commentId: number) => {
    const queryIndex = commentIds.indexOf(commentId);
    if (queryIndex === -1) return '';

    const emojiData = emojiQueries[queryIndex]?.data?.data;
    return emojiData?.map(emoji => emojiMapping(emoji.emojiType)).join(' ') || '';
  };

  if (isLoading) {
    return (
      <S.PostCommentsPageContainer>
        <TitleContainer title="보낸 코멘트" subtitle="내가 보낸 공감을 확인해보세요" />
        <S.MomentsContainer>
          {Array.from({ length: 3 }).map((_, index) => (
            <CommonSkeletonCard key={`comments-skeleton-card-${index}`} variant="comment" />
          ))}
        </S.MomentsContainer>
      </S.PostCommentsPageContainer>
    );
  }
  if (error) return <div>에러가 발생했습니다.</div>;
  if (!commentsResponse?.data || !Array.isArray(commentsResponse.data)) {
    return <div>데이터가 없습니다.</div>;
  }

  const hasComments = commentsResponse?.data.length > 0;

  return (
    <S.PostCommentsPageContainer>
      <TitleContainer title="보낸 코멘트" subtitle="내가 보낸 공감을 확인해보세요" />
      {hasComments ? (
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
                  <SimpleCard
                    height="small"
                    content={post.content}
                    backgroundColor="yellow-300_10"
                  />
                </S.ContentContainer>
                <S.ContentContainer>
                  <S.TitleContainer>
                    <Gift size={20} color={theme.colors['yellow-500']} />
                    <span>받은 스티커</span>
                  </S.TitleContainer>
                  <S.Emoji>{getEmojiData(post.id)}</S.Emoji>
                </S.ContentContainer>
              </Card.Content>
            </Card>
          ))}
        </S.MomentsContainer>
      ) : (
        <NotFoundMyComments />
      )}
    </S.PostCommentsPageContainer>
  );
}
