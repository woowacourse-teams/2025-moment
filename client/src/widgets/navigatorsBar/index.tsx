import { ROUTES } from '@/app/routes/routes';
import { Link } from 'react-router';
import * as S from './index.styles';
import { sendEvent } from '@/shared/lib/ga';

export const NavigatorsBar = ({ $isNavBar }: { $isNavBar?: boolean }) => {
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
    <S.NavigatorsBarContainer $isNavBar={$isNavBar}>
      <Link to={ROUTES.TODAY_MOMENT} onClick={handleTodayMomentClick}>
        <S.LinkContainer $isNavBar={$isNavBar}>
          <S.IconImage src="/images/bluePlanet.png" alt="오늘의 모멘트 페이지로 이동 버튼" />
          <S.IconText>오늘의 모멘트</S.IconText>
        </S.LinkContainer>
      </Link>

      <Link to={ROUTES.TODAY_COMMENT} onClick={handleTodayCommentClick}>
        <S.LinkContainer $isNavBar={$isNavBar}>
          <S.IconImage src="/images/orangePlanet.png" alt="오늘의 코멘트 페이지로 이동 버튼" />
          <S.IconText>오늘의 코멘트</S.IconText>
        </S.LinkContainer>
      </Link>

      <Link to={ROUTES.COLLECTION_MYMOMENT} onClick={handleCollectionClick}>
        <S.LinkContainer $isNavBar={$isNavBar}>
          <S.IconImage src="/images/starPlanet.png" alt="나만의 모음집 페이지로 이동 버튼" />
          <S.IconText>나만의 모음집</S.IconText>
        </S.LinkContainer>
      </Link>
    </S.NavigatorsBarContainer>
  );
};
