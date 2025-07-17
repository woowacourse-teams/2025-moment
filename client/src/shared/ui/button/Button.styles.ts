import { CustomTheme } from '@/app/styles/theme';
import styled from '@emotion/styled';

export type ButtonVariant = 'primary' | 'secondary';

const buttonStyles = {
  primary: (theme: CustomTheme) => `
    background-color: transparent;
    color: #fff;
    border: 1px solid ${theme.colors.slate900};
    border-radius: 50px;
    padding: 10px 20px;
    font-size: 16px;
    font-weight: 600;

    &:hover {
        transform: scale(1.05);
        transition: transform 0.3s ease;
    }

    @media (max-width: 768px) {
        padding: 8px 16px;
        font-size: 14px;
    }
    `,

  secondary: (theme: CustomTheme) => `
    background-color: ${theme.colors.yellow500};
    color: black;
    padding: 18px 30px;
    border-radius: 50px;
    font-size: 24px;
    font-weight: bold;
    transition: all 0.3s ease;

    @media (max-width: 768px) {
        padding: 16px 24px;
        font-size: 20px;
    }

    @media (max-width: 480px) {
        padding: 14px 20px;
        font-size: 18px;
    }

    &:hover {
        filter: brightness(1.1);
        box-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
        transform: translateY(-2px);
    }
    `,
};

export const Button = styled.button<{ variant: ButtonVariant }>`
  ${({ theme, variant }) => buttonStyles[variant](theme)}
`;
