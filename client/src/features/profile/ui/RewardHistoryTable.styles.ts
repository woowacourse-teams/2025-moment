import styled from '@emotion/styled';

export const TableContainer = styled.div`
  width: 100%;
  overflow-x: auto;
`;

export const Table = styled.table`
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
`;

export const TableHeader = styled.thead`
  background-color: ${({ theme }) => theme.colors['gray-800']};
`;

export const HeaderRow = styled.tr`
  border-bottom: 2px solid ${({ theme }) => theme.colors['gray-600']};
`;

export const HeaderCell = styled.th`
  padding: 16px 12px;
  text-align: left;
  font-weight: 600;
  font-size: 14px;
  color: ${({ theme }) => theme.colors['gray-200']};
  text-transform: uppercase;
  letter-spacing: 0.05em;
`;

export const BodyRow = styled.tr`
  border-bottom: 1px solid ${({ theme }) => theme.colors['gray-700']};

  &:last-child {
    border-bottom: none;
  }
`;

export const BodyCell = styled.td<{ $isPositive?: boolean }>`
  padding: 14px 12px;
  font-size: 14px;
  color: ${({ theme, $isPositive }) =>
    $isPositive ? theme.colors['green-500'] : theme.colors['red-500']};
`;
