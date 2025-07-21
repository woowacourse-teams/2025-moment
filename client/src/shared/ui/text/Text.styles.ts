import { CustomTheme } from '@/app/styles/theme';
import styled from '@emotion/styled';

export type textHeight = 'small' | 'medium' | 'large';

const TextStyles = {
  text: (theme: CustomTheme, $height: textHeight) => `
    display: flex;
    width: 100%;
    padding: 10px 16px;
    background-color: ${theme.colors['gray-600_20']};
    border-radius: 5px;
    height: ${theme.typography.textAreaHeight[$height]};
    color: ${theme.colors.white};
    border: 1px solid ${theme.colors['gray-700']};
    `,
};

export const Text = styled.p<{ $height: textHeight }>`
  ${({ theme, $height }) => TextStyles.text(theme, $height)}
`;
