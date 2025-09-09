import styled from '@emotion/styled';

export const ButtonContainer = styled.div`
  display: flex;
  gap: 12px;
  justify-content: flex-end;
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
