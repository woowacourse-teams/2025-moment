import styled from '@emotion/styled';

export const ListContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
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
