import { CustomTheme } from '@/app/styles/theme';
import styled from '@emotion/styled';

export type cardWidth = 'small' | 'medium' | 'large';

const CardStyles = {
  card: (theme: CustomTheme, $width: cardWidth, $shadow: boolean) => `
    display: flex;
    flex-direction: column;
    gap: 15px;
    width: ${theme.typography.cardWidth[$width]};
    padding: 20px 30px;
    background-color: ${theme.colors['slate-800_60']};
    border-radius: 10px;
    border: 1px solid ${theme.colors['gray-700']};
    word-break: keep-all;
    box-shadow: ${$shadow && `0px 0px 15px ${theme.colors['yellow-300_80']}`};

    @media (max-width: 768px) {
      width: 90%;
    }
    `,
};

export const Card = styled.div<{ $width: cardWidth; $shadow: boolean }>`
  ${({ theme, $width, $shadow }) => CardStyles.card(theme, $width, $shadow)}
`;
