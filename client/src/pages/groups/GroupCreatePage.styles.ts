import styled from '@emotion/styled';

export const PageContainer = styled.div`
  width: 95%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 3%;
  min-height: 100vh;

  @media (min-width: 768px) {
    width: 90%;
    padding: 48px 5%;
  }

  @media (min-width: 1024px) {
    width: 85%;
    padding: 48px 6%;
  }
`;

export const Header = styled.div`
  margin-bottom: 24px;

  @media (min-width: 768px) {
    margin-bottom: 40px;
  }
`;

export const Title = styled.h1`
  font-size: 24px;
  font-weight: 700;
  color: ${({ theme }) => theme.colors.white};
  margin: 0 0 8px 0;

  @media (min-width: 768px) {
    font-size: 32px;
  }
`;

export const Subtitle = styled.p`
  font-size: 14px;
  color: ${({ theme }) => theme.colors['gray-400']};
  margin: 0;
`;
