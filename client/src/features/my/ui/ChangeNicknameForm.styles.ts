import styled from '@emotion/styled';

export const ChangeNicknameFormWrapper = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
`;

export const InputGroup = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
`;

export const ChangeNicknameContent = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;

export const SuccessMessage = styled.span`
  font-size: 12px;
  color: ${({ theme }) => theme.colors['green-500']};
  margin-top: 4px;
  height: 18px;
  display: block;
  line-height: 1.5;
`;
export const ErrorMessage = styled.p`
  color: ${({ theme }) => theme.colors['red-500']};
`;
