import styled from '@emotion/styled';

export const LoginFormWrapper = styled.form`
  width: 80%;
  max-width: 500px;
  min-height: 550px;
  padding: 30px;
  background-color: ${({ theme }) => theme.colors['slate-800_60']}; // #1E293B
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 25px;
`;

export const LoginFormTitleContainer = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
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
  font-size: 38px;
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
  gap: 20px;
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

export const Input = styled.input`
  width: 100%;
  padding: 12px 16px;
  border: 1px solid ${({ theme }) => theme.colors['slate-700']};
  border-radius: 8px;
  background-color: ${({ theme }) => theme.colors['gray-600_20']};
  color: ${({ theme }) => theme.colors.white};
  font-size: 16px;

  &:focus {
    outline: none;
    border-color: ${({ theme }) => theme.colors['yellow-500']};
    box-shadow: 0 0 0 2px ${({ theme }) => theme.colors['yellow-300']}40; /* 40은 hex opacity */
  }

  &::placeholder {
    color: ${({ theme }) => theme.colors['gray-400']};
    opacity: 0.6;
  }
`;

export const ErrorMessage = styled.span`
  font-size: 12px;
  color: ${({ theme }) => theme.colors['red-500']};
  margin-top: 4px;
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
