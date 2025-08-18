import styled from '@emotion/styled';

export const LevelTableWrapper = styled.table`
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #000;
  text-align: center;
  border: 1px solid ${({ theme }) => theme.colors.white};
  th,
  td {
    border-right: 1px solid ${({ theme }) => theme.colors.white};
    padding: 8px;
  }
`;
