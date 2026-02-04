import { CustomTheme } from '@/shared/styles/theme';
import styled from '@emotion/styled';

export type ButtonVariant =
  | 'primary'
  | 'secondary'
  | 'tertiary'
  | 'quaternary'
  | 'quinary'
  | 'danger';
export type ExternalVariant = (theme: CustomTheme) => string;

const buttonStyles = {
  primary: (theme: CustomTheme) => `
    background-color: transparent;
    color: #fff;
    border: 1px solid ${theme.colors['slate-700']};
    border-radius: 50px;
    padding: 10px 20px;
    font-size: 16px;
    font-weight: 600;
    white-space: nowrap;

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
    background-color: ${theme.colors['yellow-500']};
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

  tertiary: (theme: CustomTheme) => `
    background-color: ${theme.colors['yellow-500']};
    color: black;
    padding: 10px 20px;
    border-radius: 5px;
    font-size: 1.1rem;
    font-weight: bold;
    display: flex;
    align-items: center;
    gap: 10px;
    transition: all 0.3s ease;

    @media (max-width: 768px) {
        padding: 16px 24px;
    }

    @media (max-width: 480px) {
        padding: 14px 20px;
        font-size: 1rem;
    }

    &:hover {
        filter: brightness(1.1);
        box-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
        transform: translateY(-2px);
    }
    `,

  quaternary: (theme: CustomTheme) => `
    background-color: transparent;
    color: ${theme.colors['gray-200']};
    border: 1px solid ${theme.colors['slate-700']};
    padding: 10px 20px;
    border-radius: 50px;
    font-size: 16px;

    @media (max-width: 1024px) {
      padding: 8px 16px;
      font-size: 14px;
    }

    @media (max-width: 768px) {
      padding: 4px 10px;
      font-size: 12px;
    
      &:hover {
        filter: brightness(1.1);
        box-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
        transform: translateY(-2px);
    }
    }
  `,

  quinary: (theme: CustomTheme) => `
    background-color: ${theme.colors['yellow-300']};
    color: ${theme.colors['slate-700']};
    border: 1px solid ${theme.colors['slate-700']};
    padding: 10px 20px;
    border-radius: 50px;
    font-size: 16px;

    @media (max-width: 1024px) {
      padding: 8px 16px;
      font-size: 14px;
    }

    @media (max-width: 768px) {
      padding: 4px 10px;
      font-size: 12px;
    }`,

  danger: (theme: CustomTheme) => `
    background-color: transparent;
    color: ${theme.colors['red-500']};
    border: 1px solid ${theme.colors['red-500']};
    border-radius: 50px;
    padding: 10px 20px;
    font-size: 16px;
    font-weight: 600;
    white-space: nowrap;

    &:hover {
      background-color: ${theme.colors['red-500']};
      color: #fff;
      transform: scale(1.05);
      transition: all 0.3s ease;
    }

    @media (max-width: 768px) {
      padding: 8px 16px;
      font-size: 14px;
    }
  `,
};

export const Button = styled.button<{
  variant: ButtonVariant;
  externalVariant?: ExternalVariant;
}>`
  ${({ theme, variant }) => buttonStyles[variant](theme)};
  ${({ theme, externalVariant }) => externalVariant && externalVariant(theme)};

  &:disabled {
    cursor: not-allowed;
    transform: none;
    color: ${({ theme }) => theme.colors['gray-600']};
    border: 1px solid ${({ theme }) => theme.colors['slate-800']};
    background-color: ${({ theme }) => theme.colors['slate-900']};
    opacity: 0.5;
  }
`;
