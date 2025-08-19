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

export const MyMomentsContent = styled.p`
  height: 100%;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
`;

export const MyMomentsModalContent = styled.div`
  height: 100%;
  width: 100%;
  padding: 20px 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;

  @media (max-width: 768px) {
    padding: 16px 0px;
  }

  @media (max-width: 480px) {
    padding: 12px 0px;
  }
`;

export const CommentContainer = styled.div`
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  padding: 20px;
  margin: 20px 0;
`;

export const CommentContent = styled.div`
  width: 100%;
  text-align: center;
  line-height: 1.6;
  padding: 0 40px;
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
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
`;

export const Title = styled.span`
  font-size: ${({ theme }) => theme.typography.title.fontSize.small};
  font-weight: ${({ theme }) => theme.typography.fontWeight.large};
  color: ${({ theme }) => theme.colors.white};
`;

export const EchoContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
`;
export const EchoButtonContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
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
