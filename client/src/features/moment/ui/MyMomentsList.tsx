import { useMomentsQuery } from '../hook/useMomentsQuery';
import { MyMoments } from '../types/moments';
import * as S from './MyMomentsList.styles';
import { MyMomentsCard } from './MyMomentsCard';
import { NotFoundMyMoments } from './NotFoundMyMoments';
import { CommonSkeletonCard } from '@/shared/ui';

export const MyMomentsList = () => {
  const { data, isLoading } = useMomentsQuery();
  const myMoments = data?.data;

  const hasMoments = myMoments?.length && myMoments.length > 0;

  if (isLoading) {
    return (
      <S.MomentsContainer>
        {Array.from({ length: 3 }).map((_, index) => (
          <CommonSkeletonCard key={index} variant="moment" />
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
