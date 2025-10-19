import { ROUTES } from '@/app/routes/routes';
import { useDelayedVisible } from '@/shared/hooks/useDelayedVisible';
import { Button } from '@/shared/ui/button/Button';
import { Hero } from '@/widgets/hero';
import { useNavigate } from 'react-router';
import * as S from './index.styles';
import { useScrollAnimation } from '@/shared/hooks/useScrollAnimation';
import { PropsWithChildren, useState } from 'react';
import { explainData } from './const';
import { useScrollDepth } from '@/shared/lib/ga/hooks/useScrollDepth';
import { track } from '@/shared/lib/ga/track';

export default function HomePage() {
  useScrollDepth();

  const navigate = useNavigate();
  const { isVisible } = useDelayedVisible({ delay: 100 });

  const handleClick = () => {
    track('click_cta', { target: 'today_moment', cta_type: 'primary' });
    navigate(ROUTES.TODAY_MOMENT, { state: { entry: 'cta' } });
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
          <S.IntroTitleLogo src="/images/momentLogo.webp" alt="" />
          <S.IntroText>
            "모멘트(Moment)"는 사용자들이 삶의 모든 순간(Moment)을 공유하며 서로에게 따뜻한 칭찬과
            위로를 건네는 소셜 네트워크 서비스입니다. <br />
            힘든 순간, 뿌듯한 순간, 위로받고 싶은 순간, 칭찬받고 싶은 모든 순간을 짧은 기록으로
            남기고, 긍정적인 상호작용을 통해 정서적 지지와 유대감을 나눌 수 있는 공간을 제공합니다.
          </S.IntroText>
          <S.IntroImagesWrapper>
            <S.IntroIcon src="/images/rocket.webp" alt="" />
            <S.IntroIcon src="/images/paperAirplane.webp" alt="" />
            <S.IntroIcon src="/images/spaceMan.webp" alt="" />
          </S.IntroImagesWrapper>
        </S.IntroSection>
      </AnimatedIntroSection>
      {explainData.map(section => (
        <AnimatedIntroSection key={section.title}>
          <ExplainSection {...section} />
        </AnimatedIntroSection>
      ))}
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
