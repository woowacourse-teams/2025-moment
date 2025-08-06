import { theme } from '@/app/styles/theme';
import { useCommentsQuery } from '@/features/comment/hooks/useCommentsQuery';
import { emojiMapping } from '@/features/emoji/utils/emojiMapping';
import { Card, CommonSkeletonCard, NotFound, SimpleCard } from '@/shared/ui';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import { Gift, MessageSquare, Send } from 'lucide-react';
import { useCallback, useEffect, useRef } from 'react';
import * as S from './index.styles';

export default function MyCommentsPage() {
  const {
    data: commentsResponse,
    isLoading,
    isError,
    error,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
  } = useCommentsQuery();

  if (isError) {
    console.error('Error fetching comments:', error);
    return <div>오류가 발생했습니다. 잠시 후 다시 시도해주세요.</div>;
  }

  const myComments = commentsResponse?.pages.flatMap(page => page.data.items) ?? [];
  const hasComments = myComments?.length && myComments.length > 0;

  const observerRef = useRef<HTMLDivElement>(null);

  const handleIntersect = useCallback(
    (entries: globalThis.IntersectionObserverEntry[]) => {
      const [entry] = entries;
      if (entry.isIntersecting && hasNextPage && !isFetchingNextPage) {
        fetchNextPage();
      }
    },
    [hasNextPage, isFetchingNextPage, fetchNextPage],
  );

  useEffect(() => {
    if (!observerRef.current) return;

    const observer = new globalThis.IntersectionObserver(handleIntersect, {
      threshold: 0.1,
    });

    observer.observe(observerRef.current);

    return () => observer.disconnect();
  }, [handleIntersect]);

  if (isLoading) {
    return (
      <S.MyCommentsPageContainer>
        <TitleContainer title="보낸 코멘트" subtitle="내가 보낸 공감을 확인해보세요" />
        <S.MomentsContainer>
          {Array.from({ length: 3 }).map((_, index) => (
            <CommonSkeletonCard key={`comments-skeleton-card-${index}`} variant="comment" />
          ))}
        </S.MomentsContainer>
      </S.MyCommentsPageContainer>
    );
  }

  return (
    <S.MyCommentsPageContainer>
      <TitleContainer title="보낸 코멘트" subtitle="내가 보낸 공감을 확인해보세요" />
      {hasComments ? (
        <S.MomentsContainer>
          {myComments.map((myComment, index) => (
            <Card width="medium" key={`card-${myComment.id}-${index}`}>
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
          ))}

          <div ref={observerRef} style={{ height: '1px' }} />
        </S.MomentsContainer>
      ) : (
        <NotFound
          title="아직 작성한 코멘트가 없어요"
          subtitle="다른 사용자의 모멘트에 따뜻한 공감을 보내보세요"
        />
      )}
    </S.MyCommentsPageContainer>
  );
}
