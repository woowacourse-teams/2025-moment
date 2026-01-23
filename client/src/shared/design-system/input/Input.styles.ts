import { CustomTheme } from '@/shared/styles/theme';
import styled from '@emotion/styled';

const InputStyles = {
  input: (theme: CustomTheme) => `
    width: 100%;
    padding: 14px 16px;
    background-color: ${theme.colors['slate-800']};
    border-radius: 12px;
    height: 52px;
    font-size: 15px;
    color: ${theme.colors.white};
    border: 1.5px solid ${theme.colors['slate-700']};
    transition: border-color 0.2s ease, box-shadow 0.2s ease;

    &:focus {
      outline: none;
      border-color: ${theme.colors['yellow-500']};
      box-shadow: 0 0 0 3px ${theme.colors['yellow-500']}20;
    }

    &::placeholder {
        color: ${theme.colors['gray-600']};
        opacity: 1;
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    @media (min-width: 768px) {
      height: 56px;
      padding: 16px 18px;
      font-size: 16px;
    }
    `,
};

export const Input = styled.input`
  ${({ theme }) => InputStyles.input(theme)}
`;
