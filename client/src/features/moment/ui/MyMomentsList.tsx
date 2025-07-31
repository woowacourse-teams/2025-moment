import { CommonSkeletonCard, NotFound } from '@/shared/ui';
import { Clock } from 'lucide-react';
import { useMomentsQuery } from '../hook/useMomentsQuery';
import { MyMoments } from '../types/moments';
import { MyMomentsCard } from './MyMomentsCard';
import * as S from './MyMomentsList.styles';

export const MyMomentsList = () => {
  const { data, isLoading } = useMomentsQuery();
  const myMoments = data?.data;

  const hasMoments = myMoments?.length && myMoments.length > 0;

  if (isLoading) {
    return (
      <S.MomentsContainer>
        {Array.from({ length: 3 }).map((_, index) => (
          <CommonSkeletonCard key={`moments-skeleton-card-${index}`} variant="moment" />
        ))}
      </S.MomentsContainer>
    );
  }

  return (
    <S.MomentsContainer>
      {hasMoments ? (
        myMoments?.map((myMoment: MyMoments, index: number) => (
          <MyMomentsCard key={myMoment.createdAt} myMoment={myMoment} index={index} />
        ))
      ) : (
        <NotFound
          title="아직 모멘트가 없어요"
          subtitle="오늘의 모멘트를 작성하고 따뜻한 공감을 받아보세요"
          icon={Clock}
          size="large"
        />
      )}
    </S.MomentsContainer>
  );
};
