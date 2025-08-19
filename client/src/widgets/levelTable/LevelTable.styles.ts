import styled from '@emotion/styled';

export const LevelTableWrapper = styled.table`
  width: 100%;
  height: 70vh;
  border-collapse: collapse;
  border: 1px solid #000;
  text-align: center;
  border: 1px solid ${({ theme }) => theme.colors.white};
  tr.last-stage {
    border-bottom: 1px solid ${({ theme }) => theme.colors.white};
  }
  th,
  td {
    border-right: 1px solid ${({ theme }) => theme.colors.white};
    padding: 8px;
  }
`;

export const LevelImage = styled.img`
  width: 50px;
  height: 50px;
`;
