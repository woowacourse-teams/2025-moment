import { theme } from '@/app/styles/theme';
import styled from '@emotion/styled';

export const TitleWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;

export const LevelImage = styled.img`
  width: 30px;
  height: 30px;
  object-fit: contain;
`;

export const TimeWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
  color: ${theme.colors['gray-400']};
  font-size: 14px;
`;
