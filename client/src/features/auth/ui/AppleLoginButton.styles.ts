import styled from '@emotion/styled';

export const AppleLoginButton = styled.button`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  height: 48px;
  border-radius: 10px;
  background-color: #000000;
  color: #ffffff;
  font-size: 16px;
  font-weight: 500;
  transition: opacity 0.2s;
  border: 1px solid rgba(255, 255, 255, 0.1);
  margin-top: 12px;

  &:hover {
    opacity: 0.9;
  }
`;

export const AppleLoginButtonIcon = styled.img`
  width: 20px;
  height: 20px;
  object-fit: contain;
`;
