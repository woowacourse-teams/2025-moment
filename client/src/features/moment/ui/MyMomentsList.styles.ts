import { css } from '@emotion/react';
import styled from '@emotion/styled';

export const MomentsContainer = styled.section<{ $display?: boolean }>`
  ${({ $display }) =>
    $display
      ? css`
          display: grid;
          grid-template-columns: repeat(3, 1fr);
        `
      : css`
          display: flex;
          justify-content: center;
          align-items: center;
        `}

  gap: 28px;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;

  ${({ theme }) => theme.mediaQueries.tablet} {
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;
  }

  ${({ theme }) => theme.mediaQueries.mobile} {
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 0 12px;
  }
`;
