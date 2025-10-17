import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';

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

export const HomePageWrapper = styled.main`
  width: 100%;
  overflow: hidden;
`;

export const MainContainer = styled.div`
  min-height: calc(100vh - 80px);
  display: flex;
  flex-direction: column;
  justify-content: center;
`;

export const HeroSection = styled.section`
  position: relative;
  width: 100%;

  margin-top: 80px;

  @media (max-width: 768px) {
    margin-top: 110px;
  }
`;

export const ContentSection = styled.section<{ isVisible: boolean }>`
  width: 100%;
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding: 20px;
  overflow: hidden;

  opacity: ${({ isVisible }) => (isVisible ? 1 : 0)};
  animation: ${({ isVisible }) => (isVisible ? fadeInUp : 'none')} 0.8s ease-out 1.2s backwards;

  @media (max-width: 768px) {
    margin-top: 60px;
    padding: 16px;
  }

  @media (max-width: 480px) {
    margin-top: 80px;
    padding: 12px;
  }
`;

export const IntroSection = styled.section`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 50px;
  position: relative;
`;

export const IntroText = styled.div`
  font-size: 1.5rem;
  font-weight: 600;
`;

export const IntroTitleLogo = styled.img`
  width: 300px;
  @media (max-width: 768px) {
    width: 200px;
  }
`;

export const IntroSectionWrapper = styled.section<{ isVisible: boolean }>`
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);

  ${({ isVisible }) =>
    isVisible &&
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

  @media (max-width: 768px) {
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

export const IntroIcon = styled.img`
  width: 120px;
  height: 120px;

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
`;

export const ExplainImage = styled.img`
  object-fit: cover;
`;

export const ExplainContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
`;

export const ExplainTitle = styled.div`
  font-size: 3rem;
  font-weight: 600;
`;

export const ExplainText = styled.div`
  font-size: 1.5rem;
  font-weight: 400;
`;
