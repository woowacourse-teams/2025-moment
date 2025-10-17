import { ROUTES } from '@/app/routes/routes';
import { useDelayedVisible } from '@/shared/hooks/useDelayedVisible';
import { sendEvent } from '@/shared/lib/ga';
import { HomePageAnalyticsEvent } from '@/shared/lib/ga/analyticsEvent';
import { Button } from '@/shared/ui/button/Button';
import { Hero } from '@/widgets/hero';
import { useNavigate } from 'react-router';
import * as S from './index.styles';
import { useScrollAnimation } from '@/shared/hooks/useScrollAnimation';
import { PropsWithChildren, useState } from 'react';

export default function HomePage() {
  const navigate = useNavigate();
  const { isVisible } = useDelayedVisible({ delay: 100 });

  const handleClick = () => {
    sendEvent(HomePageAnalyticsEvent.ClickTodayMomentButton);
    navigate(ROUTES.TODAY_MOMENT);
  };

  return (
    <S.HomePageWrapper>
      <S.MainContainer>
        <S.HeroSection>
          <Hero />
        </S.HeroSection>
        <S.ContentSection isVisible={isVisible}>
          <Button title="모멘트 작성하기" variant="secondary" onClick={handleClick} />
        </S.ContentSection>
      </S.MainContainer>
      <AnimatedIntroSection>
        <S.IntroSection>
          <S.IntroTitleLogo src="/images/momentLogo.png" alt="" />
          <S.IntroText>
            "모멘트(Moment)"는 사용자들이 삶의 모든 순간(Moment)을 공유하며 서로에게 따뜻한 칭찬과
            위로를 건네는 소셜 네트워크 서비스입니다. <br />
            힘든 순간, 뿌듯한 순간, 위로받고 싶은 순간, 칭찬받고 싶은 모든 순간을 짧은 기록으로
            남기고, 긍정적인 상호작용을 통해 정서적 지지와 유대감을 나눌 수 있는 공간을 제공합니다.
          </S.IntroText>
          <S.IntroImagesWrapper>
            <S.IntroIcon src="/images/rocket.png" alt="" />
            <S.IntroIcon src="/images/paperAirplane.png" alt="" />
            <S.IntroIcon src="/images/spaceman.png" alt="" />
          </S.IntroImagesWrapper>
        </S.IntroSection>
      </AnimatedIntroSection>
      <AnimatedIntroSection>
        <ExplainSection
          image="/images/intro1.png"
          title="오늘의 모멘트"
          text1="하루에 한번, 당신의 특별한 모멘트를 공유해보세요."
          text2="작성한 모멘트가 익명으로 다른 사용자에게 공유됩니다."
        />
      </AnimatedIntroSection>
      <AnimatedIntroSection>
        <ExplainSection
          image="/images/intro2.png"
          title="오늘의 코멘트"
          text1="특별한 모멘트를 공유받고 따뜻한 공감을 보내보세요."
          text2="다른 사람의 모멘트에 코멘트를 작성할 수 있어요."
        />
      </AnimatedIntroSection>
      <AnimatedIntroSection>
        <ExplainSection
          image="/images/intro3.png"
          title="나의 모멘트 모음집"
          text1="내가 작성했던 모멘트와, 다른 사용자들이 달아준 코멘트를 확인할 수 있어요."
          text2="받은 코멘트에 에코를 보내 따뜻한 마음을 전달해보세요."
        />
      </AnimatedIntroSection>
      <AnimatedIntroSection>
        <ExplainSection
          image="/images/intro4.png"
          title="나의 코멘트 모음집"
          text1="내가 전달한 코멘트와 해당 모멘트를 확인할 수 있어요."
          text2="받은 에코도 확인할 수 있어요."
        />
      </AnimatedIntroSection>
      <AnimatedIntroSection>
        <ExplainSection
          image="/images/intro5.png"
          title="레벨업과 별조각을 모으는 재미"
          text1="모멘트를 작성하고 코멘트를 달며 레벨을 올리고 별조각을 모을 수 있어요."
          text2="별조각을 모아 레벨업과 다양한 기능을 사용할 수 있어요."
        />
      </AnimatedIntroSection>
    </S.HomePageWrapper>
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
  return (
    <S.ExplainSection>
      <S.ExplainImage src={image} alt="" />
      <S.ExplainContainer>
        <S.ExplainTitle>{title}</S.ExplainTitle>
        <S.ExplainText>{text1}</S.ExplainText>
        <S.ExplainText>{text2}</S.ExplainText>
      </S.ExplainContainer>
    </S.ExplainSection>
  );
};
