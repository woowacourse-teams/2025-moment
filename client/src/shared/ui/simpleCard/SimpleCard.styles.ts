import { CustomTheme } from '@/app/styles/theme';
import styled from '@emotion/styled';

export type SimpleCardHeight = 'small' | 'medium' | 'large';
export type ColorKey = keyof CustomTheme['colors'];

const SimpleCardStyles = {
  simpleCard: (theme: CustomTheme, $height: SimpleCardHeight, $backgroundColor?: ColorKey) => `
    display: flex;
    width: 100%;
    padding: 10px 16px;
    background-color: ${$backgroundColor ? theme.colors[$backgroundColor] : theme.colors['gray-600_20']};
    border-radius: 5px;
    height: ${theme.typography.textAreaHeight[$height]};
    color: ${theme.colors['gray-200']};
    border: 1px solid ${theme.colors['gray-700']};
    `,
};

export const SimpleCard = styled.div<{ $height: SimpleCardHeight; $backgroundColor?: ColorKey }>`
  ${({ theme, $height, $backgroundColor }) =>
    SimpleCardStyles.simpleCard(theme, $height, $backgroundColor)}
`;
