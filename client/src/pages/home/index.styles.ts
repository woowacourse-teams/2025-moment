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
  margin-top: 50px;
  padding: 20px;
  overflow: hidden;

  opacity: ${({ isVisible }) => (isVisible ? 1 : 0)};
  animation: ${({ isVisible }) => (isVisible ? fadeInUp : 'none')} 0.8s ease-out 0.8s backwards;

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

export const IntroTextWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 20px;
`;

export const IntroImage = styled.img`
  width: 200px;
  height: 200px;
`;

export const BlackHoleContainer = styled.aside`
  position: fixed;
  left: max(60px, 6vw);
  transform: translateY(-50%);
  transform: rotate(-20deg);
  z-index: 50;
  cursor: pointer;

  &:hover {
    transform: scale(1.1);
    transition: transform 0.3s ease-in-out;
  }

  @media (max-width: 1200px) {
    left: max(40px, 4vw);
  }

  @media (max-width: 768px) {
    display: none;
  }
`;

export const BlackHoleImage = styled.img`
  width: 150px;
  height: 150px;

  @media (max-width: 1200px) {
    width: 100px;
    height: 100px;
  }
`;

export const BlackHoleText = styled.p`
  position: absolute;
  left: -20px;
  top: -10px;
  margin-top: 0;
  font-size: 24px;
  font-weight: 600;
  pointer-events: none;
`;

export const ClickMeContainer = styled.div<{ isWidgetOpen: boolean }>`
  position: fixed;
  left: max(100px, 10vw);
  transform: translateY(-110%);
  z-index: 51;
  opacity: ${({ isWidgetOpen }) => (isWidgetOpen ? 1 : 0)};
  visibility: ${({ isWidgetOpen }) => (isWidgetOpen ? 'visible' : 'hidden')};
  transition:
    opacity 0.3s ease-in-out,
    visibility 0.3s ease-in-out;
`;
