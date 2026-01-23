import styled from '@emotion/styled';

export const PageContainer = styled.div`
  max-width: 600px;
  margin: 0 auto;
  padding: 40px 20px;
`;

export const Header = styled.div`
  margin-bottom: 32px;
`;

export const Title = styled.h1`
  font-size: 28px;
  font-weight: 700;
  color: ${({ theme }) => theme.colors.white};
  margin: 0 0 8px 0;
`;

export const Subtitle = styled.p`
  font-size: 14px;
  color: ${({ theme }) => theme.colors['gray-400']};
  margin: 0;
`;
