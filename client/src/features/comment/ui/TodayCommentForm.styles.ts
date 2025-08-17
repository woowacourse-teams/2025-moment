import { theme } from '@/app/styles/theme';
import styled from '@emotion/styled';

export const TitleWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
`;

export const UserInfoWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;

export const ActionWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;
`;

export const LevelImage = styled.img`
  width: 30px;
  height: 30px;
  object-fit: contain;
`;

export const TimeWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 6px;
  color: ${theme.colors['gray-400']};
  font-size: 16px;
  font-weight: 500;
`;

export const RefreshButton = styled.button`
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
  border: none;
  border-radius: 6px;
  background-color: transparent;
  color: ${theme.colors['gray-600']};
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background-color: ${theme.colors['gray-200']};
    color: ${theme.colors['gray-700']};
  }

  &:active {
    transform: scale(0.95);
  }
`;
