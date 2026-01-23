import styled from '@emotion/styled';

export const ModalContent = styled.div`
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 8px 0;
`;

export const Description = styled.p`
  color: ${({ theme }) => theme.colors['gray-400']};
  font-size: 14px;
  line-height: 1.6;
  text-align: center;
`;

export const ButtonGroup = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
`;

export const Divider = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 8px 0;

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: ${({ theme }) => theme.colors['slate-700']};
  }
`;

export const DividerText = styled.span`
  color: ${({ theme }) => theme.colors['gray-600']};
  font-size: 12px;
`;

export const InviteCodeInput = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
`;

export const Label = styled.label`
  font-size: 14px;
  font-weight: 500;
  color: ${({ theme }) => theme.colors.white};
`;
