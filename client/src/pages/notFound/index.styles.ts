import styled from '@emotion/styled';

export const NotFoundContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 70vh;
  padding: 2rem;
`;

export const NotFoundContent = styled.div`
  text-align: center;
  max-width: 500px;
`;

export const ErrorCode = styled.h1`
  font-size: 6rem;
  font-weight: bold;
  color: ${({ theme }) => theme.colors['yellow-500']};
  margin: 0;
  line-height: 1;
`;

export const ErrorTitle = styled.h2`
  font-size: 1.5rem;
  font-weight: 600;
  color: ${({ theme }) => theme.colors['red-500']};
  margin: 1rem 0;
`;

export const ErrorDescription = styled.p`
  font-size: 1rem;
  color: ${({ theme }) => theme.colors['gray-600']};
  line-height: 1.6;
  margin: 1rem 0 2rem;
`;

export const ButtonContainer = styled.div`
  margin-top: 2rem;
`;
