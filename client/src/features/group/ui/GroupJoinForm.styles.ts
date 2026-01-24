import styled from '@emotion/styled';

export const FormContainer = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
`;

export const Header = styled.div`
  margin-bottom: 8px;
  text-align: center;
`;

export const Title = styled.h2`
  font-size: 24px;
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
  gap: 16px;
`;

export const LoadingState = styled.div`
  display: flex;
  justify-content: center;
  padding: 40px 20px;
  font-size: 16px;
  color: ${({ theme }) => theme.colors['gray-400']};
`;

export const ErrorState = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 40px 20px;
  text-align: center;
`;

export const ErrorText = styled.p`
  font-size: 16px;
  color: ${({ theme }) => theme.colors['gray-400']};
  margin: 0;
`;
