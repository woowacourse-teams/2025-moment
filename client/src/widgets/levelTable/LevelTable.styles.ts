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

  @media (max-width: 768px) {
    height: 50vh;
    font-size: 0.8rem;
  }
`;

export const LevelImage = styled.img`
  width: 50px;
  height: 50px;

  @media (max-width: 768px) {
    width: 25px;
    height: 25px;
  }
`;
