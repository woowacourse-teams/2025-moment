import styled from '@emotion/styled';

export const Container = styled.div`
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 24px;
  border: 1px solid ${({ theme }) => theme.colors['slate-700']};
  border-radius: 12px;
  background: ${({ theme }) => theme.colors['slate-800']};
`;

export const Title = styled.h3`
  font-size: 18px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors.white};
  margin: 0;
`;

export const InviteCodeContainer = styled.div`
  display: flex;
  gap: 12px;
  align-items: center;
`;

export const CodeDisplay = styled.div`
  flex: 1;
  padding: 12px 16px;
  border: 1px solid ${({ theme }) => theme.colors['slate-700']};
  border-radius: 8px;
  background: ${({ theme }) => theme.colors['slate-900']};
  font-family: 'Courier New', monospace;
  font-size: 16px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors['yellow-500']};
  text-align: center;
  letter-spacing: 2px;
`;

export const Description = styled.p`
  font-size: 14px;
  color: ${({ theme }) => theme.colors['gray-400']};
  margin: 0;
  line-height: 1.6;
`;

export const ButtonGroup = styled.div`
  display: flex;
  gap: 12px;
`;
