import { theme } from '@/app/styles/theme';
import styled from '@emotion/styled';

export const MyMomentsCard = styled.div<{ $shadow: boolean; $hasComment: boolean }>`
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 350px;
  padding: 20px 30px;
  background-color: ${theme.colors['slate-800_60']};
  border-radius: 10px;
  border: 1px solid ${theme.colors['gray-700']};
  word-break: keep-all;
  cursor: ${({ $hasComment }) => ($hasComment ? 'pointer' : 'not-allowed')};

  ${({ $shadow }) =>
    $shadow &&
    `
      box-shadow: 0px 0px 15px ${theme.colors['yellow-300_80']};
      animation: shadowPulse 2s ease-in-out infinite;
    `}

  @keyframes shadowPulse {
    0%,
    100% {
      box-shadow: 0px 0px 10px ${theme.colors['yellow-300_80']};
    }
    50% {
      box-shadow: 0px 0px 25px ${theme.colors['yellow-300_80']};
    }
  }
`;

export const MyMomentsContent = styled.p`
  height: calc(100% - 20px);
  display: flex;
  align-items: center;
  justify-content: center;
`;

export const TitleContainer = styled.div`
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
`;

export const TitleWrapper = styled.div`
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
`;

export const TimeStamp = styled.span`
  font-size: 14px;
  color: ${({ theme }) => theme.colors['gray-400']};
`;

export const Title = styled.span`
  font-size: ${({ theme }) => theme.typography.title.fontSize.small};
  font-weight: ${({ theme }) => theme.typography.fontWeight.large};
  color: ${({ theme }) => theme.colors.white};
`;

export const EmojiContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
`;

export const EmpathyContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
`;
