import styled from '@emotion/styled';

export const ListContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
  max-height: 500px;
  overflow-y: auto;
  padding: 4px;

  /* Custom Scrollbar */
  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background-color: ${({ theme }) => theme.colors['slate-700']};
    border-radius: 4px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background-color: ${({ theme }) => theme.colors['gray-600']};
  }
`;

export const EmptyState = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  gap: 16px;
`;

export const EmptyText = styled.p`
  font-size: 16px;
  color: ${({ theme }) => theme.colors['gray-400']};
  margin: 0;
`;

export const LoadingState = styled.div`
  display: flex;
  justify-content: center;
  padding: 40px;
`;
