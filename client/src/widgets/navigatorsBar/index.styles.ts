import styled from '@emotion/styled';

export const NavigatorsBarContainer = styled.div<{ $isNavBar?: boolean }>`
  display: flex;
  flex-direction: ${({ $isNavBar }) => ($isNavBar ? 'row' : 'column')};
  align-items: center;
  justify-content: center;
  gap: 60px;

  @media (max-width: 1024px) {
    gap: 30px;
  }
`;

export const LinkContainer = styled.div<{ $isNavBar?: boolean }>`
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: ${({ $isNavBar }) => ($isNavBar ? 'row' : 'column')};
  gap: 12px;

  &:hover {
    transform: scale(1.1);
    transition: transform 0.3s ease-in-out;
  }
`;

export const IconImage = styled.img`
  width: 40px;
  height: 40px;
`;

export const IconText = styled.p`
  font-size: 1.3rem;
  font-weight: 600;
  line-height: 14.52px;
  letter-spacing: -0.01em;

  @media (max-width: 1024px) {
    font-size: 1.1rem;
  }
`;
