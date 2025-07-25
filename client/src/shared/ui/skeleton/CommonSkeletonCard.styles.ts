import { theme } from '@/app/styles/theme';
import styled from '@emotion/styled';

export const SkeletonCard = styled.div`
  display: flex;
  flex-direction: column;
  gap: 15px;
  width: ${theme.typography.cardWidth.large};
  padding: 20px 30px;
  background-color: ${theme.colors['slate-800_60']};
  border-radius: 10px;
  border: 1px solid ${theme.colors['gray-700']};
  word-break: keep-all;

  @media (max-width: 768px) {
    width: 90%;
  }
`;

export const SkeletonCardTitle = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

export const SkeletonTitleRow = styled.div`
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
`;

export const SkeletonCardContent = styled.div`
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 10px;
`;

export const SkeletonSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 10px;

  &:last-child {
    margin-bottom: 0;
  }
`;

export const SkeletonSectionHeader = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;

export const SkeletonContentHeader = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;

export const SkeletonSimpleCard = styled.div`
  display: flex;
  width: 100%;
  padding: 10px 16px;
  background-color: ${theme.colors['gray-600_20']};
  border-radius: 5px;
  height: ${theme.typography.textAreaHeight.small};
  border: 1px solid ${theme.colors['gray-700']};
  align-items: center;
`;

export const SkeletonYellowCard = styled.div`
  display: flex;
  width: 100%;
  padding: 10px 16px;
  background-color: ${theme.colors['yellow-300_10']};
  border-radius: 5px;
  height: ${theme.typography.textAreaHeight.small};
  border: 1px solid ${theme.colors['gray-700']};
  align-items: center;
`;

export const SkeletonCardAction = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

export const SkeletonActionButtons = styled.div`
  display: flex;
  gap: 8px;
`;

export const SkeletonEmojiContainer = styled.div`
  display: flex;
  gap: 8px;
`;
