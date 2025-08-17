import { ROUTES } from '@/app/routes/routes';
import { useNotificationsQuery } from '@/features/notification/hooks/useNotificationsQuery';
import { sendEvent } from '@/shared/lib/ga';
import { Link } from 'react-router';
import * as S from './index.styles';

export const NavigatorsBar = ({ $isNavBar }: { $isNavBar?: boolean }) => {
  const { data: notifications } = useNotificationsQuery();

  if (!notifications) {
    return null;
  }
  const isNotificationExisting = notifications?.data.length > 0;

  const handleTodayMomentClick = () => {
    sendEvent({
      category: 'NavigatorsBar',
      action: 'Click TodayMoment Button',
      label: 'TodayMoment Button',
    });
  };

  const handleTodayCommentClick = () => {
    sendEvent({
      category: 'NavigatorsBar',
      action: 'Click TodayComment Button',
      label: 'TodayComment Button',
    });
  };

  const handleCollectionClick = () => {
    sendEvent({
      category: 'NavigatorsBar',
      action: 'Click Collection Button',
      label: 'Collection Button',
    });
  };

  return (
    <S.NavigatorsBarContainer $isNavBar={$isNavBar} $shadow={isNotificationExisting}>
      <Link to={ROUTES.TODAY_MOMENT} onClick={handleTodayMomentClick}>
        <S.LinkContainer $isNavBar={$isNavBar}>
          <S.IconImage src="/bluePlanet.png" alt="오늘의 모멘트 페이지로 이동 버튼"></S.IconImage>
          <S.IconText>오늘의 모멘트</S.IconText>
        </S.LinkContainer>
      </Link>

      <Link to={ROUTES.TODAY_COMMENT} onClick={handleTodayCommentClick}>
        <S.LinkContainer $isNavBar={$isNavBar}>
          <S.IconImage src="/orangePlanet.png" alt="오늘의 코멘트 페이지로 이동 버튼"></S.IconImage>
          <S.IconText>오늘의 코멘트</S.IconText>
        </S.LinkContainer>
      </Link>

      <Link to={ROUTES.COLLECTION_MYMOMENT} onClick={handleCollectionClick}>
        <S.LinkContainer $isNavBar={$isNavBar}>
          <S.IconImage src="/starPlanet.png" alt="나만의 모음집 페이지로 이동 버튼"></S.IconImage>
          <S.IconText>나만의 모음집</S.IconText>
        </S.LinkContainer>
      </Link>
    </S.NavigatorsBarContainer>
  );
};
