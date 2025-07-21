import { CustomTheme } from '@/app/styles/theme';
import styled from '@emotion/styled';

const InputStyles = {
  input: (theme: CustomTheme) => `
    width: 100%;
    padding: 10px 20px;
    background-color: ${theme.colors['gray-600_20']};
    border-radius: 5px;
    height: 50px;
    color: ${theme.colors.white};
    border: 1px solid ${theme.colors['gray-700']};

    &::placeholder {
        color: ${theme.colors['gray-400']};
        opacity: 0.6;
    }
    `,
};

export const Input = styled.input`
  ${({ theme }) => InputStyles.input(theme)}
`;
