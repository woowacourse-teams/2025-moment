import styled from '@emotion/styled';

export const PaginationContainer = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  width: 100%;
`;

export const PageInfo = styled.span`
  font-size: 16px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors['gray-200']};
  min-width: 80px;
  text-align: center;
`;
