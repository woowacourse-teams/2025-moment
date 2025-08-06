import styled from '@emotion/styled';

export const GoogleLoginButton = styled.button`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  padding: 12px 16px;
  border: 1px solid white;
  border-radius: 8px;
  background-color: white;
  color: black;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  &:hover {
    background-color: ${({ theme }) => theme.colors['slate-700']};
    color: white;
  }
`;

export const GoogleLoginButtonIcon = styled.img`
  width: 20px;
  height: 20px;
`;
