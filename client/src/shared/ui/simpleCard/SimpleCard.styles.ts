import { CustomTheme } from '@/app/styles/theme';
import styled from '@emotion/styled';

export type SimpleCardHeight = 'small' | 'medium' | 'large';

const SimpleCardStyles = {
  simpleCard: (theme: CustomTheme, $height: SimpleCardHeight) => `
    display: flex;
    width: 100%;
    padding: 10px 16px;
    background-color: ${theme.colors['gray-600_20']};
    border-radius: 5px;
    height: ${theme.typography.textAreaHeight[$height]};
    color: ${theme.colors['gray-400']};
    border: 1px solid ${theme.colors['gray-700']};
    `,
};

export const SimpleCard = styled.p<{ $height: SimpleCardHeight }>`
  ${({ theme, $height }) => SimpleCardStyles.simpleCard(theme, $height)}
`;
