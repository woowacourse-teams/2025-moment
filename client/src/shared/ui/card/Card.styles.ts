import { CustomTheme } from '@/app/styles/theme';
import styled from '@emotion/styled';

export type cardWidth = 'small' | 'medium' | 'large' | 'full';

export type CardVariant = 'primary' | 'secondary';

const getBackgroundColor = (theme: CustomTheme, variant: CardVariant) => {
  switch (variant) {
    case 'primary':
      return theme.colors['slate-800_60'];
    case 'secondary':
      return theme.colors['slate-900_90'];
    default:
      return theme.colors['slate-800_60'];
  }
};

const CardStyles = {
  card: (theme: CustomTheme, $width: cardWidth, $shadow: boolean, $variant: CardVariant) => `
    display: flex;
    flex-direction: column;
    gap: 15px;
    width: ${theme.typography.cardWidth[$width]};
    padding: 20px 30px;
    background-color: ${getBackgroundColor(theme, $variant)};
    border-radius: 10px;
    border: 1px solid ${theme.colors['gray-700']};
    word-break: keep-all;
    ${
      $shadow &&
      `
      box-shadow: 0px 0px 15px ${theme.colors['yellow-300_80']};
      animation: shadowPulse 2s ease-in-out infinite;
    `
    }

    @keyframes shadowPulse {
      0%, 100% {
        box-shadow: 0px 0px 10px ${theme.colors['yellow-300_80']};
      }
      50% {
        box-shadow: 0px 0px 25px ${theme.colors['yellow-300_80']};
      }
    }

    @media (max-width: 768px) {
      width: 90%;
    }
    `,
};

export const Card = styled.div<{ $width: cardWidth; $shadow: boolean; $variant: CardVariant }>`
  ${({ theme, $width, $shadow, $variant }) => CardStyles.card(theme, $width, $shadow, $variant)}
`;
