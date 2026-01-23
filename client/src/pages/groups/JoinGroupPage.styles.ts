import styled from '@emotion/styled';

export const PageContainer = styled.div`
  max-width: 800px;
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

export const Description = styled.p`
  font-size: 14px;
  color: ${({ theme }) => theme.colors['gray-400']};
  margin: 0;
`;

export const Content = styled.div`
  display: flex;
  flex-direction: column;
  gap: 24px;
`;

export const LoadingState = styled.div`
  display: flex;
  justify-content: center;
  padding: 60px 20px;
  font-size: 16px;
  color: ${({ theme }) => theme.colors['gray-400']};
`;

export const ErrorState = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 60px 20px;
  text-align: center;
`;

export const ErrorText = styled.p`
  font-size: 16px;
  color: ${({ theme }) => theme.colors['gray-400']};
  margin: 0;
`;
