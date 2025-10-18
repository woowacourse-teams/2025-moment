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

const fadeInScale = keyframes`
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
`;

export const HeroWrapper = styled.section<{ isVisible: boolean }>`
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 0 2rem;
  position: relative;
  overflow: hidden;

  opacity: ${({ isVisible }) => (isVisible ? 1 : 0)};
  transform: ${({ isVisible }) => (isVisible ? 'translateY(0)' : 'translateY(20px)')};
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
`;

export const TitleContainer = styled.div<{ isVisible: boolean }>`
  margin-bottom: 2rem;

  opacity: ${({ isVisible }) => (isVisible ? 1 : 0)};
  animation: ${({ isVisible }) => (isVisible ? fadeInUp : 'none')} 0.8s ease-out 0.2s backwards;
`;

export const Title = styled.h1`
  color: white;
  font-size: clamp(2.6rem, 6vw, 4.6rem);
  font-weight: 700;
  line-height: 1.2;
  margin: 0;

  &:first-of-type {
    margin-bottom: 0.5rem;
  }

  &:nth-of-type(2) {
    background: linear-gradient(135deg, #fbbf24 0%, rgb(183, 116, 0) 100%);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  @media (max-width: 768px) {
    font-size: clamp(1.5rem, 6vw, 2.5rem);
  }
`;

export const SubtitleContainer = styled.div<{ isVisible: boolean }>`
  max-width: 600px;

  opacity: ${({ isVisible }) => (isVisible ? 1 : 0)};
  animation: ${({ isVisible }) => (isVisible ? fadeInScale : 'none')} 0.8s ease-out 0.6s backwards;
`;

export const Subtitle = styled.p`
  font-size: clamp(1.2rem, 3vw, 1.6rem);
  font-weight: 400;
  line-height: 1.6;
  margin: 0;
  opacity: 0.9;
  color: #f0f0f0;

  &:first-of-type {
    margin-bottom: 0.5rem;
  }

  @media (max-width: 768px) {
    font-size: clamp(0.9rem, 2.5vw, 1.1rem);
    line-height: 1.5;
  }
`;

export const AccentLine = styled.div<{ isVisible: boolean }>`
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #4f46e5, #06b6d4);
  margin: 1.5rem auto;
  border-radius: 2px;

  opacity: ${({ isVisible }) => (isVisible ? 1 : 0)};
  transform: ${({ isVisible }) => (isVisible ? 'scaleX(1)' : 'scaleX(0)')};
  transition: all 0.6s ease-out 0.4s;
`;

export const HighlightedTextContainer = styled.div<{ isVisible: boolean }>`
  margin: 2rem 0;
  display: flex;
  flex-direction: column;
  gap: 2rem;
  width: 100%;
  max-width: 560px;

  opacity: ${({ isVisible }) => (isVisible ? 1 : 0)};
  animation: ${({ isVisible }) => (isVisible ? fadeInUp : 'none')} 0.8s ease-out 0.8s backwards;
`;

export const HighlightedText = styled.div`
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
    font-size: clamp(1rem, 2vw, 1.2rem);
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
