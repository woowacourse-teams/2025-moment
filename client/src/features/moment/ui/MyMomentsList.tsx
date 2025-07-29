import { CommonSkeletonCard } from '@/shared/ui';
import { useMomentsQuery } from '../hook/useMomentsQuery';
import { MyMoments } from '../types/moments';
import { MyMomentsCard } from './MyMomentsCard';
import * as S from './MyMomentsList.styles';
import { NotFoundMyMoments } from './NotFoundMyMoments';

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
          <MyMomentsCard myMoment={myMoment} index={index} />
        ))
      ) : (
        <NotFoundMyMoments />
      )}
    </S.MomentsContainer>
  );
};
