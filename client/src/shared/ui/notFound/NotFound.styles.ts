import styled from '@emotion/styled';

interface StyledProps {
  size: 'small' | 'large';
}

export const NotFoundWrapper = styled.div<StyledProps>`
  display: flex;
  flex-direction: column;
  height: ${({ size }) => (size === 'large' ? '55vh' : 'auto')};
  align-items: center;
  justify-content: center;
  gap: ${({ size }) => (size === 'large' ? '30px' : '8px')};
`;

export const NotFoundIconWrapper = styled.div<StyledProps>`
  width: ${({ size }) => (size === 'large' ? '30px' : '24px')};
  height: ${({ size }) => (size === 'large' ? '30px' : '24px')};
  color: ${({ theme }) => theme.colors['gray-400']};
`;

export const NotFoundContainer = styled.div<StyledProps>`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: ${({ size }) => (size === 'large' ? '10px' : '8px')};
`;

export const NotFoundTitle = styled.div<StyledProps>`
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: ${({ size }) => (size === 'large' ? '28px' : '14px')};
  font-weight: ${({ size }) => (size === 'large' ? '700' : '400')};
  color: ${({ theme }) => theme.colors['gray-400']};
`;

export const NotFoundSubtitle = styled.div<StyledProps>`
  font-weight: ${({ size }) => (size === 'large' ? '600' : '400')};
  font-size: ${({ size }) => (size === 'large' ? '16px' : '14px')};
  color: ${({ theme }) => theme.colors['gray-400']};
`;
