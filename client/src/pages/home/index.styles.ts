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
  min-height: 100vh;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 30px;
`;

export const IntroText = styled.div`
  font-size: 1.5rem;
  font-weight: 600;
`;

export const IntroTextWrapper = styled.div<{ isVisible: boolean }>`
  display: flex;
  align-items: center;
  gap: 20px;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);

  ${({ isVisible }) =>
    isVisible &&
    `
    opacity: 1;
    transform: translateY(0);
  `}
`;

export const IntroImage = styled.img`
  width: 200px;
  height: 200px;
`;
