import { css } from '@emotion/react';
import styled from '@emotion/styled';

export const MyMomentsCard = styled.div<{ $shadow: boolean; $hasComment: boolean }>`
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 350px;
  padding: 20px 30px;
  background-color: ${({ $hasComment, theme }) =>
    $hasComment ? theme.colors['slate-800_60'] : theme.colors['gray-600_20']};
  border-radius: 10px;
  border: 1px solid ${({ theme }) => theme.colors['gray-700']};
  word-break: keep-all;
  cursor: ${({ $hasComment }) => ($hasComment ? 'pointer' : 'not-allowed')};

  ${({ $shadow, theme }) =>
    $shadow &&
    `
      box-shadow: 0px 0px 15px ${theme.colors['yellow-300_80']};
      animation: shadowPulse 2s ease-in-out infinite;
    `}

  @keyframes shadowPulse {
    0%,
    100% {
      box-shadow: 0px 0px 10px ${({ theme }) => theme.colors['yellow-300_80']};
    }
    50% {
      box-shadow: 0px 0px 25px ${({ theme }) => theme.colors['yellow-300_80']};
    }
  }
`;

export const MyMomentsTitleWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

export const CommentCountWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
  color: ${({ theme }) => theme.colors['gray-400']};
`;

export const CommentContentWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 100%;
`;

export const MyMomentsContent = styled.p`
  height: 100%;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: ${({ theme }) => theme.typography.fontSize.content.medium};
  ${({ theme }) => theme.breakpoints.mobile} {
    font-size: ${({ theme }) => theme.typography.fontSize.mobileContent.medium};
  }
  text-align: center;
  word-break: break-all;
`;

export const MyMomentsBottomWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
`;

export const MyMomentsTagWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: flex-end;
  flex-wrap: wrap;
`;

export const MyMomentsModalContent = styled.div`
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 12px;
  }

  &::-webkit-scrollbar-track {
    background: rgba(0, 0, 0, 0.05);
    border-radius: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.6);
    border-radius: 6px;

    &:hover {
      background: rgba(0, 0, 0, 0.8);
    }
  }

  scrollbar-width: auto;
  scrollbar-color: rgba(0, 0, 0, 0.3) rgba(0, 0, 0, 0.1);
`;

export const CommentContainer = styled.div`
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  padding: 10px 20px;
`;

export const CommentContent = styled.div`
  width: 100%;
  height: 100%;
  text-align: center;
  line-height: 1.6;
  padding: 0 20px;

  & > div {
    word-break: break-all;
    font-size: ${({ theme }) => theme.typography.fontSize.content.medium};
    ${({ theme }) => theme.breakpoints.mobile} {
      font-size: ${({ theme }) => theme.typography.fontSize.mobileContent.medium};
    }
  }
`;

export const ComplaintButton = styled.button`
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
  border: none;
  cursor: pointer;
  background-color: transparent;
  ${({ theme }) => css`
    color: ${theme.colors['red-500']};
  `}
`;

export const ActionWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;

export const DeleteButton = styled.button`
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  border: none;
  cursor: pointer;
  background-color: transparent;
  color: ${({ theme }) => theme.colors['red-500']};
  transition: opacity 0.2s;

  &:hover {
    opacity: 0.7;
  }
`;

export const IconButton = styled.button`
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
  border: none;
  cursor: pointer;
  background-color: transparent;
  transition: all 0.2s ease;

  &:hover {
    opacity: 0.7;
  }

  &:active {
    transform: scale(1.1);
  }
`;

export const CommentNavigationButton = styled.button<{ position: 'left' | 'right' }>`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background-color: ${({ theme }) => theme.colors['gray-700']};
  border: 1px solid ${({ theme }) => theme.colors['gray-600']};
  border-radius: 50%;
  color: ${({ theme }) => theme.colors.white};
  cursor: pointer;
  transition: all 0.2s ease;
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  ${({ position }) => (position === 'left' ? 'left: 0;' : 'right: 0;')}

  &:hover {
    background-color: ${({ theme }) => theme.colors['gray-600']};
    border-color: ${({ theme }) => theme.colors['gray-400']};
  }

  &:active {
    transform: translateY(-50%) scale(0.95);
  }
`;

export const MyMomentsModalHeader = styled.div`
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

export const WriterInfoWrapper = styled.div`
  width: 100%;
`;

export const TitleContainer = styled.div`
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
`;

export const TitleWrapper = styled.div`
  width: 50%;
  display: flex;
  justify-content: flex-end;
`;

export const Title = styled.span`
  font-size: ${({ theme }) => theme.typography.fontSize.title.small};
  font-weight: ${({ theme }) => theme.typography.fontWeight.large};
  color: ${({ theme }) => theme.colors.white};
`;

export const NavigationContainer = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 10px 0;
  margin-bottom: 10px;
`;

export const NavigationButton = styled.button`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background-color: ${({ theme }) => theme.colors['gray-700']};
  border: 1px solid ${({ theme }) => theme.colors['gray-600']};
  border-radius: 50%;
  color: ${({ theme }) => theme.colors.white};
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background-color: ${({ theme }) => theme.colors['gray-600']};
    border-color: ${({ theme }) => theme.colors['gray-400']};
  }

  &:active {
    transform: scale(0.9);
  }
`;

export const CommentIndicator = styled.div`
  color: ${({ theme }) => theme.colors['gray-400']};
  width: 40%;
  text-align: center;
  font-size: 0.9rem;
`;

export const CommentImageContainer = styled.div`
  display: flex;
  justify-content: center;
  margin-top: 12px;
`;

export const CommentImage = styled.img`
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid ${({ theme }) => theme.colors['gray-600']};
  cursor: pointer !important;
  transition: transform 0.2s ease;

  &:hover {
    transform: scale(1.05);
  }
`;

export const MomentImageContainer = styled.div`
  display: flex;
  justify-content: center;
  margin: 8px 0;
`;

export const MomentImage = styled.img`
  width: 80px;
  height: 80px;
  border-radius: 6px;
  object-fit: cover;
  border: 1px solid ${({ theme }) => theme.colors['gray-600']};
  cursor: pointer;
  transition: transform 0.2s ease;

  &:hover {
    transform: scale(1.05);
  }
`;

export const ImageOverlay = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  cursor: pointer;
`;

export const FullscreenImage = styled.img`
  max-width: 80vw;
  max-height: 80vh;
  object-fit: contain;
`;
