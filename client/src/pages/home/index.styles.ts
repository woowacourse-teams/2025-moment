import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';
import { Picture } from '@/shared/design-system/picture';

const fadeInUp = keyframes`
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
`;

const float = keyframes`
  0%, 100% {
    transform:  translateY(0px);
  }
  50% {
    transform:  translateY(-10px);
  }
`;

export const HomePageWrapper = styled.main`
  width: 100%;
  overflow: hidden;
`;

export const MainContainer = styled.div`
  position: relative;
  min-height: calc(100vh - 80px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 30px;
`;

export const HeroSection = styled.section`
  position: relative;
  width: 100%;

  margin-top: 60px;

  @media (max-width: 768px) {
    margin-top: 110px;
  }
`;

export const ContentSection = styled('section', {
  shouldForwardProp: prop => prop !== '$isVisible',
})<{ $isVisible: boolean }>`
  width: 100%;
  display: flex;
  justify-content: center;

  opacity: ${({ $isVisible }) => ($isVisible ? 1 : 0)};
  animation: ${({ $isVisible }) => ($isVisible ? fadeInUp : 'none')} 0.8s ease-out 0.8s backwards;

  @media (max-width: 768px) {
    padding: 16px;
  }

  @media (max-width: 480px) {
    padding: 12px;
  }
`;

export const HighlightedTextContainer = styled('blockquote', {
  shouldForwardProp: prop => prop !== '$isVisible',
})<{ $isVisible: boolean }>`
  margin: 2rem 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2rem;
  width: 100%;
  max-width: 560px;

  opacity: ${({ $isVisible }) => ($isVisible ? 1 : 0)};
  animation: ${({ $isVisible }) => ($isVisible ? fadeInUp : 'none')} 0.8s ease-out 1.2s backwards;
`;

export const HighlightedText = styled.p`
  position: relative;
  display: inline-block;
  padding: 0.8rem 2rem;
  font-size: clamp(1.4rem, 2.5vw, 1.6rem);
  font-weight: 500;
  color: ${({ theme }) => theme.colors['white']};
  line-height: 1.4;
  margin: 0.5rem;

  &:first-of-type {
    align-self: flex-start;
  }

  &:nth-of-type(2) {
    align-self: flex-end;
  }

  &::before {
    content: '';
    position: absolute;
    top: 80%;
    left: 40%;
    width: 80%;
    height: 100%;
    background-image: url('/images/highlighter.webp');
    background-size: 100% 100%;
    background-repeat: no-repeat;
    background-position: center;
    transform: translate(-50%, -50%);
    filter: opacity(0.7);
    z-index: -1;
  }

  &:nth-of-type(2)::before {
    transform: translate(-50%, -50%) scaleX(-1);
    left: 55%;
  }

  @media (max-width: 768px) {
    font-size: clamp(1.1rem, 2vw, 1.4rem);
    margin: 0;

    &:first-of-type,
    &:nth-of-type(2) {
      align-self: center;
    }

    &::before {
      height: 80%;
    }
  }
`;

export const BottomArrow = styled(Picture, {
  shouldForwardProp: prop => prop !== '$isVisible',
})<{ $isVisible: boolean }>`
  width: 100px;
  height: 100px;
  margin-top: 40px;

  opacity: ${({ $isVisible }) => ($isVisible ? 1 : 0)};
  animation: ${({ $isVisible }) => ($isVisible ? float : 'none')} 2s ease-in-out infinite;
  filter: drop-shadow(0 0 15px rgba(255, 255, 255, 0.6))
    drop-shadow(0 0 30px rgba(255, 255, 255, 0.4)) drop-shadow(0 0 45px rgba(255, 255, 255, 0.2));

  & img {
    width: 100%;
    height: 100%;
  }

  @media (max-width: 768px) {
    width: 80px;
    height: 80px;
  }
`;

export const IntroSection = styled.section`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 50px;
  position: relative;

  @media (max-width: 768px) {
    padding: 16px;
  }

  @media (max-width: 480px) {
    padding: 12px;
  }
`;

export const IntroText = styled.p`
  font-size: 1.5rem;
  font-weight: 600;

  @media (max-width: 768px) {
    font-size: 1.3rem;
  }

  @media (max-width: 480px) {
    font-size: 1.1rem;
  }
`;

export const IntroTitleLogo = styled(Picture)`
  width: 300px;

  & img {
    width: 100%;
  }

  @media (max-width: 768px) {
    width: 200px;
  }
`;

export const IntroSectionWrapper = styled('section', {
  shouldForwardProp: prop => prop !== '$isVisible',
})<{ $isVisible: boolean }>`
  width: 100%;
  height: 75vh;
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  margin: 100px 0;

  ${({ $isVisible }) =>
    $isVisible &&
    `
    opacity: 1;
    transform: translateY(0);
  `}

  ${IntroSection} {
    max-width: 700px;
    margin: 0 auto;
    position: relative;
    font-size: 1.3rem;
    line-height: 1.8;
    text-align: center;
    word-break: keep-all;
  }

  @media screen and (max-width: 1440px) {
    height: 90vh;
  }

  @media (max-width: 768px) {
    height: 80vh;

    ${IntroSection} {
      max-width: 80%;
      font-size: 1.1rem;
    }
  }
`;

export const IntroImagesWrapper = styled.div`
  display: flex;
  gap: 20px;

  @media (max-width: 768px) {
    gap: 10px;
  }
`;

export const IntroIcon = styled(Picture)`
  width: 120px;
  height: 120px;

  & img {
    width: 100%;
    height: 100%;
  }

  @media (max-width: 768px) {
    width: 80px;
    height: 80px;
  }
`;

export const ExplainSection = styled.section`
  width: 100%;
  display: flex;
  gap: 100px;
  align-items: center;
  justify-content: center;

  @media screen and (max-width: 1440px) {
    flex-direction: column-reverse;
  }

  @media (max-width: 768px) {
    gap: 50px;
    padding: 16px;
  }
`;

export const ExplainImage = styled(Picture)`
  & img {
    object-fit: cover;
  }

  @media screen and (max-width: 768px) {
    width: 90%;
    height: 90%;

    & img {
      width: 100%;
      height: 100%;
    }
  }
`;

export const ExplainContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;

  @media screen and (max-width: 1440px) {
    gap: 8px;
  }
`;

export const ExplainTitle = styled.h2`
  font-size: 3rem;
  font-weight: 600;

  @media screen and (max-width: 768px) {
    font-size: 2.5rem;
  }

  @media (max-width: 480px) {
    font-size: 2rem;
  }
`;

export const ExplainText = styled.p`
  font-size: 1.5rem;
  font-weight: 400;
  text-align: center;
  word-break: keep-all;

  @media screen and (max-width: 768px) {
    font-size: 1.3rem;
  }

  @media (max-width: 480px) {
    font-size: 1.1rem;
  }
`;
export const ActionButtons = styled.div`
  display: flex;
  gap: 12px;
  margin-top: 16px;
  justify-content: center;
  width: 100%;
`;

export const OnboardingContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24px;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
  text-align: center;
`;

export const OnboardingTitle = styled.h2`
  font-size: 2.5rem;
  font-weight: 700;
  color: ${({ theme }) => theme.colors.white};
  margin-bottom: 8px;

  @media (max-width: 768px) {
    font-size: 2rem;
  }
`;

export const OnboardingDescription = styled.p`
  font-size: 1.25rem;
  color: ${({ theme }) => theme.colors['gray-400']};
  margin-bottom: 32px;

  @media (max-width: 768px) {
    font-size: 1.1rem;
  }
`;

export const OnboardingButtonGroup = styled.div`
  display: flex;
  justify-content: center;
  gap: 24px;
  width: 100%;
  flex-wrap: wrap;

  @media (max-width: 640px) {
    flex-direction: column;
    align-items: center;
  }
`;

export const OnboardingCard = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 280px;
  height: 200px;
  padding: 30px;
  background: ${({ theme }) => theme.colors['slate-800']};
  border: 2px solid ${({ theme }) => theme.colors['slate-700']};
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;

  h3 {
    font-size: 1.5rem;
    font-weight: 600;
    color: ${({ theme }) => theme.colors.white};
    margin-bottom: 12px;
  }

  p {
    font-size: 1rem;
    color: ${({ theme }) => theme.colors['gray-400']};
  }

  &:hover {
    transform: translateY(-5px);
    border-color: ${({ theme }) => theme.colors['yellow-500']};
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);

    h3 {
      color: ${({ theme }) => theme.colors['yellow-500']};
    }
  }

  @media (max-width: 640px) {
    width: 100%;
    max-width: 320px;
    height: 160px;
  }
`;
export const GroupListContainer = styled.div`
  width: 100%;
  max-width: 480px;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 auto;
`;
