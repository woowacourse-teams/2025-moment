import styled from '@emotion/styled';

export const Container = styled.div`
  width: 100%;
`;

export const Header = styled.div`
  margin-bottom: 16px;
`;

export const Title = styled.h3`
  font-size: 18px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors.white};
`;

export const NotificationCard = styled.div`
  background: ${({ theme }) => theme.colors['gray-800']};
  border: 1px solid ${({ theme }) => theme.colors['gray-700']};
  border-radius: 12px;
  padding: 20px;
`;

export const NotificationInfo = styled.div`
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
`;

export const IconWrapper = styled.div<{ color: string }>`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: ${({ color }) => `${color}20`};
  color: ${({ color }) => color};
  flex-shrink: 0;
`;

export const TextWrapper = styled.div`
  flex: 1;
`;

export const StatusText = styled.div`
  font-size: 16px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors.white};
  margin-bottom: 4px;
`;

export const Description = styled.div`
  font-size: 14px;
  color: ${({ theme }) => theme.colors['gray-400']};
  line-height: 1.5;
`;

export const ToggleButton = styled.button<{ disabled?: boolean }>`
  width: 100%;
  padding: 12px;
  background: ${({ theme }) => theme.colors['yellow-500']};
  color: ${({ theme }) => theme.colors.black};
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;

  &:hover:not(:disabled) {
    opacity: 0.9;
    transform: translateY(-1px);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
`;

export const InfoBox = styled.div`
  margin-top: 12px;
  padding: 12px;
  background: ${({ theme }) => theme.colors['green-700_20']};
  border-radius: 8px;
  font-size: 14px;
  color: ${({ theme }) => theme.colors['emerald-500']};
  text-align: center;
`;

export const HelpText = styled.div`
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 12px;
  padding: 12px;
  background: ${({ theme }) => theme.colors['slate-800']};
  border-radius: 8px;
  font-size: 12px;
  color: ${({ theme }) => theme.colors['gray-400']};
  line-height: 1.6;

  svg {
    flex-shrink: 0;
    margin-top: 2px;
  }
`;

export const ErrorText = styled.div`
  margin-top: 12px;
  padding: 12px;
  background: #ff000020;
  border-radius: 8px;
  color: #f44336;
  font-size: 14px;
`;
