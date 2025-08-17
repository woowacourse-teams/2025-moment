import { css, keyframes } from '@emotion/react';
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

export const BlackHoleContainer = styled.aside`
  position: fixed;
  left: 60px;
  transform: translateY(-50%);
  transform: rotate(-20deg);
  z-index: 50;
  cursor: pointer;

  &:hover {
    transform: scale(1.1);
    transition: transform 0.3s ease-in-out;
  }

  @media (max-width: 480px) {
    display: none;
  }
`;

export const BlackHoleImage = styled.img`
  width: 150px;
  height: 150px;
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

export const ClickMeContainer = styled.div<{ isWidgetOpen: boolean; $shadow: boolean }>`
  ${({ theme, $shadow }) =>
    $shadow &&
    css`
      box-shadow: 0px 0px 15px ${theme.colors['yellow-300_80']};
      animation: shadowPulse 2s ease-in-out infinite;

      @keyframes shadowPulse {
        0%,
        100% {
          box-shadow: 0px 0px 10px ${theme.colors['yellow-300_80']};
        }
        50% {
          box-shadow: 0px 0px 25px ${theme.colors['yellow-300_80']};
        }
      }
    `}

  position: fixed;
  left: 90px;
  transform: translateY(-110%);
  z-index: 51;
  opacity: ${({ isWidgetOpen }) => (isWidgetOpen ? 1 : 0)};
  visibility: ${({ isWidgetOpen }) => (isWidgetOpen ? 'visible' : 'hidden')};
  transition:
    opacity 0.3s ease-in-out,
    visibility 0.3s ease-in-out;
`;
