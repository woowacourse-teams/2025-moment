import { ROUTES } from '@/app/routes/routes';
import { useDelayedVisible } from '@/shared/hooks/useDelayedVisible';
import { Button } from '@/shared/ui/button/Button';
import { Hero } from '@/widgets/hero';
import { useNavigate } from 'react-router';
import * as S from './index.styles';
import { useScrollAnimation } from '@/shared/hooks/useScrollAnimation';
import { PropsWithChildren, useEffect, useState } from 'react';
import { explainData } from './const';
import { useScrollDepth } from '@/shared/lib/ga/hooks/useScrollDepth';
import { track } from '@/shared/lib/ga/track';
import { useModal } from '@/shared/hooks/useModal';
import { Modal } from '@/shared/ui/modal/Modal';
import { NotificationButton } from '@/shared/notifications/NotificationButton';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { isDevice, isPWA } from '@/shared/utils/device';
import { IOSBrowserWarning } from '@/shared/ui/IOSBrowserWarning';

export default function HomePage() {
  useScrollDepth();

  const navigate = useNavigate();
  const { isVisible } = useDelayedVisible({ delay: 100 });
  const { isOpen, handleClose, handleOpen } = useModal();
  const { data: isLoggedIn } = useCheckIfLoggedInQuery();

  const shouldShowNotificationModal =
    isLoggedIn && isDevice() && isPWA() && Notification.permission === 'default';

  useEffect(() => {
    if (shouldShowNotificationModal) {
      handleOpen();
    }
  }, [handleOpen, shouldShowNotificationModal]);

  const handleClick = () => {
    handleOpen();
    track('click_cta', { target: 'today_moment', cta_type: 'primary' });
    navigate(ROUTES.TODAY_MOMENT, { state: { entry: 'cta' } });
  };

  return (
    <>
      <IOSBrowserWarning />
      <S.HomePageWrapper>
        <S.MainContainer>
          <S.HeroSection>
            <Hero />
          </S.HeroSection>
          <S.ContentSection isVisible={isVisible}>
            <Button title="모멘트 작성하기" variant="secondary" onClick={handleClick} />
          </S.ContentSection>
          <S.ContentSection isVisible={isVisible}>
            <S.BottomArrow
              isVisible={isVisible}
              webpSrc="/images/belowAirplane.webp"
              fallbackSrc="/images/fallback/belowAirplane.png"
              alt="아래로 스크롤하세요"
              role="img"
            />
          </S.ContentSection>
          <S.HighlightedTextContainer
            isVisible={isVisible}
            role="region"
            aria-label="서비스 핵심 가치"
          >
            <S.HighlightedText>" 비교 없는 따뜻한 소통의 공간 "</S.HighlightedText>
            <S.HighlightedText>" 익명 속에서 편안하게 마음을 나눌 수 있는 곳 "</S.HighlightedText>
          </S.HighlightedTextContainer>
        </S.MainContainer>
        <AnimatedIntroSection>
          <S.IntroSection>
            <S.IntroTitleLogo
              webpSrc="/images/momentLogo.webp"
              fallbackSrc="/images/fallback/momentLogo.png"
              alt=""
            />
            <h2 id="intro-title" style={{ position: 'absolute', left: '-10000px' }}>
              moment 소개
            </h2>
            <S.IntroText>
              "모멘트(Moment)"는 사용자들이 삶의 모든 순간(Moment)을 공유하며 서로에게 따뜻한 칭찬과
              위로를 건네는 소셜 네트워크 서비스입니다. <br />
              힘든 순간, 뿌듯한 순간, 위로받고 싶은 순간, 칭찬받고 싶은 모든 순간을 짧은 기록으로
              남기고, 긍정적인 상호작용을 통해 정서적 지지와 유대감을 나눌 수 있는 공간을
              제공합니다.
            </S.IntroText>
            <S.IntroImagesWrapper>
              <S.IntroIcon
                webpSrc="/images/rocket.webp"
                fallbackSrc="/images/fallback/rocket.png"
                alt=""
              />
              <S.IntroIcon
                webpSrc="/images/paperAirplane.webp"
                fallbackSrc="/images/fallback/paperAirplane.png"
                alt=""
              />
              <S.IntroIcon
                webpSrc="/images/spaceMan.webp"
                fallbackSrc="/images/fallback/spaceMan.png"
                alt=""
              />
            </S.IntroImagesWrapper>
          </S.IntroSection>
        </AnimatedIntroSection>
        {explainData.map(section => (
          <AnimatedIntroSection key={section.title}>
            <ExplainSection {...section} />
          </AnimatedIntroSection>
        ))}
      </S.HomePageWrapper>
      <Modal size="small" isOpen={isOpen} onClose={handleClose} titleId="notification-modal-title">
        <Modal.Header
          title="모멘트와 코멘트 알림을 받아보세요!"
          showCloseButton={true}
          id="notification-modal-title"
        />
        <Modal.Content>
          <NotificationButton onClose={handleClose} />
        </Modal.Content>
      </Modal>
    </>
  );
}

const AnimatedIntroSection = ({ children }: PropsWithChildren) => {
  const [isVisible, setIsVisible] = useState(false);
  const ref = useScrollAnimation({
    onVisible: () => setIsVisible(true),
  });

  return (
    <S.IntroSectionWrapper ref={ref} isVisible={isVisible}>
      {children}
    </S.IntroSectionWrapper>
  );
};

const ExplainSection = ({
  image,
  title,
  text1,
  text2,
}: {
  image: string;
  title: string;
  text1?: string;
  text2?: string;
}) => {
  const fallbackImage = image.replace('/images/', '/images/fallback/').replace('.webp', '.png');

  return (
    <S.ExplainSection>
      <S.ExplainImage webpSrc={image} fallbackSrc={fallbackImage} alt="" />
      <S.ExplainContainer>
        <S.ExplainTitle>{title}</S.ExplainTitle>
        <S.ExplainText>{text1}</S.ExplainText>
        <S.ExplainText>{text2}</S.ExplainText>
      </S.ExplainContainer>
    </S.ExplainSection>
  );
};
