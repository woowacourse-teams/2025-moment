import styled from '@emotion/styled';

export const PaginationContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 1rem 0;
`;

export const PageButton = styled.button<{ $active?: boolean }>`
  min-width: 2rem;
  height: 2rem;
  padding: 0 0.5rem;
  border-radius: 4px;
  font-size: 0.875rem;
  font-weight: ${({ $active }) => ($active ? '600' : '400')};
  color: ${({ $active }) => ($active ? '#3b82f6' : '#9ca3af')};
  background-color: ${({ $active }) => ($active ? 'rgba(59, 130, 246, 0.1)' : 'transparent')};
  border: 1px solid ${({ $active }) => ($active ? '#3b82f6' : 'transparent')};
  cursor: pointer;
  transition: all 0.15s ease;

  &:hover:not(:disabled) {
    background-color: #2a2a2a;
    color: white;
  }

  &:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }
`;
