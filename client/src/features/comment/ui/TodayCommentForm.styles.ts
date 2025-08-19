import { css } from '@emotion/react';
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
  ${({ theme }) => css`
    color: ${theme.colors['gray-400']};
  `}
  display: flex;
  align-items: center;
  gap: 6px;
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
  ${({ theme }) => css`
    color: ${theme.colors['yellow-500']};
    &:hover {
      background-color: ${theme.colors['yellow-300_10']};
    }
  `}
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    transform: scale(1.05);
  }
`;

export const NotLoggedIcon = styled.img`
  width: 30px;
  height: 30px;
  object-fit: contain;
`;

export const NotLoggedNickname = styled.p`
  font-size: 16px;
  font-weight: 500;
  color: ${({ theme }) => theme.colors['yellow-500']};
`;
