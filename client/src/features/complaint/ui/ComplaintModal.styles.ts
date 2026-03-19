import styled from '@emotion/styled';

export const ButtonContainer = styled.div`
  display: flex;
  gap: 12px;
  justify-content: flex-end;
`;

export const BlockCheckboxContainer = styled.div`
  padding: 12px 0 0;
`;

export const BlockCheckboxLabel = styled.label`
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: ${({ theme }) => theme.colors['gray-600']};
`;

export const BlockCheckbox = styled.input`
  width: 16px;
  height: 16px;
  cursor: pointer;
`;

export const ModalContent = styled.div`
  max-height: 60vh;
  overflow-y: auto;
  padding: 0;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: ${({ theme }) => theme.colors['gray-200']};
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: ${({ theme }) => theme.colors['gray-200']};
    border-radius: 3px;

    &:hover {
      background: ${({ theme }) => theme.colors['gray-400']};
    }
  }
`;
