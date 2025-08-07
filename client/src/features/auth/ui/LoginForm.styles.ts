import styled from '@emotion/styled';

export const LoginFormWrapper = styled.div`
  width: 80%;
  max-width: 500px;
  height: 100%;
  padding: 30px;
  background-color: ${({ theme }) => theme.colors['slate-800_60']};
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 20px;
`;

export const LoginFormContainer = styled.form`
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
`;

export const LoginFormTitleContainer = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
`;

export const LoginLogoTitleContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
`;

export const LogoImage = styled.img`
  width: 60px;
  height: 60px;
`;

export const LogoTitle = styled.span`
  font-size: 35px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors.white};
`;

export const LoginTitle = styled.h1`
  font-size: 30px;
  font-weight: 700;
  color: ${({ theme }) => theme.colors['gray-200']};
`;

export const LoginDescription = styled.p`
  font-size: 16px;
  font-weight: 700;
  color: ${({ theme }) => theme.colors['gray-200']};
`;

export const LoginFormContent = styled.div`
  width: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

export const InputGroup = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
`;

export const Label = styled.label`
  font-size: 14px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors.white};
`;

export const ErrorMessage = styled.span`
  font-size: 12px;
  color: ${({ theme }) => theme.colors['red-500']};
  margin-top: 4px;
  min-height: 16px;
  line-height: 16px;
`;

export const LoginFooter = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 15px;
`;

export const LoginButton = styled.button`
  width: 100%;
  padding: 12px 16px;
  border: 1px solid ${({ theme }) => theme.colors['yellow-500']};
  border-radius: 8px;
  background-color: transparent;
  color: ${({ theme }) => theme.colors['yellow-500']};
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  &:hover {
    background-color: ${({ theme }) => theme.colors['yellow-500']};
    color: ${({ theme }) => theme.colors['slate-800']};
  }

  &:disabled {
    cursor: not-allowed;
  }
`;

export const LoginFooterContent = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
`;

export const LoginForgotPassword = styled.span`
  color: ${({ theme }) => theme.colors['yellow-500']};
  cursor: pointer;
  &:hover {
    color: ${({ theme }) => theme.colors['yellow-600']};
  }
`;

export const LoginSignupContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
`;

export const LoginSignupLink = styled.span`
  color: ${({ theme }) => theme.colors['yellow-500']};
  cursor: pointer;
  &:hover {
    color: ${({ theme }) => theme.colors['yellow-600']};
  }
`;
