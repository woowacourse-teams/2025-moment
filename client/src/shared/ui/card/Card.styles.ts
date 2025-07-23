import { CustomTheme } from '@/app/styles/theme';
import styled from '@emotion/styled';

export type cardWidth = 'small' | 'medium' | 'large';

const CardStyles = {
  card: (theme: CustomTheme, $width: cardWidth) => `
    display: flex;
    flex-direction: column;
    gap: 15px;
    width: ${theme.typography.cardWidth[$width]};
    padding: 20px 30px;
    background-color: ${theme.colors['slate-800_60']};
    border-radius: 10px;
    border: 1px solid ${theme.colors['gray-700']};

    @media (max-width: 768px) {
      width: 90%;
    }
    `,
};

export const Card = styled.div<{ $width: cardWidth }>`
  ${({ theme, $width }) => CardStyles.card(theme, $width)}
`;
