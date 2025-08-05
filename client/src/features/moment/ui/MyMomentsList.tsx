import { CommonSkeletonCard, NotFound } from '@/shared/ui';
import { Clock } from 'lucide-react';
import { useMomentsWithNotifications } from '../hook/useMomentsWithNotifications';
import { MomentWithNotifications } from '../types/momentsWithNotifications';
import { MyMomentsCard } from './MyMomentsCard';
import * as S from './MyMomentsList.styles';

export const MyMomentsList = () => {
  const { momentWithNotifications, isLoading } = useMomentsWithNotifications();

  const hasMoments = momentWithNotifications?.length && momentWithNotifications.length > 0;

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
        momentWithNotifications?.map((myMoment: MomentWithNotifications) => (
          <MyMomentsCard key={myMoment.id} myMoment={myMoment} />
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
