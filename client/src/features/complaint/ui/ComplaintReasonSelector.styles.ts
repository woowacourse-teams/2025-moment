import styled from '@emotion/styled';

export const Container = styled.div`
  padding: 20px;
  box-sizing: border-box;
`;

export const Title = styled.h3`
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: ${({ theme }) => theme.colors['white']};
  margin-top: 0;
`;

export const ReasonList = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-sizing: border-box;
`;

export const ReasonItem = styled.div<{ isSelected: boolean }>`
  padding: 16px;
  border: 2px solid
    ${({ isSelected, theme }) =>
      isSelected ? theme.colors['yellow-500'] : theme.colors['gray-200']};
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  background-color: ${({ isSelected, theme }) =>
    isSelected ? theme.colors['yellow-300_10'] : theme.colors['gray-200']};
  box-sizing: border-box;

  &:hover {
    border-color: ${({ theme }) => theme.colors['yellow-300']};
  }
`;

export const ReasonHeader = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
`;

export const RadioButton = styled.div<{ isSelected: boolean }>`
  width: 20px;
  height: 20px;
  border: 2px solid
    ${({ isSelected, theme }) =>
      isSelected ? theme.colors['yellow-500'] : theme.colors['gray-400']};
  border-radius: 50%;
  position: relative;
  flex-shrink: 0;

  ${({ isSelected, theme }) =>
    isSelected &&
    `
    &::after {
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 10px;
      height: 10px;
      background-color: ${theme.colors['yellow-500']};
      border-radius: 50%;
    }
  `}
`;

export const ReasonLabel = styled.span`
  font-size: 16px;
  font-weight: 500;
  color: ${({ theme }) => theme.colors['slate-900']};
  word-break: keep-all;
`;

export const ReasonDescription = styled.p`
  font-size: 14px;
  color: ${({ theme }) => theme.colors['gray-600']};
  line-height: 1.5;
  margin: 0;
  margin-left: 32px;
  word-break: keep-all;
`;
