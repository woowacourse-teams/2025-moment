import { CustomTheme } from '@/shared/styles/theme';
import styled from '@emotion/styled';

export type textAreaHeight = 'small' | 'medium' | 'large';

const TextAreaStyles = {
  textarea: (theme: CustomTheme, $height: textAreaHeight) => `
    width: 100%;
    padding: 20px;
    background-color: ${theme.colors['gray-600_20']};
    border-radius: 5px;
    height: ${theme.spacing.textAreaHeight[$height]};
    color: ${theme.colors.white};
    border: 1px solid ${theme.colors['gray-700']};
    resize: none;
    font-size: ${theme.typography.fontSize.content.medium};
    ${theme.breakpoints.mobile} {
      font-size: ${theme.typography.fontSize.mobileContent.medium};
    }
    
    &::placeholder {
        color: ${theme.colors['gray-400']};
    }
    `,
};

export const TextArea = styled.textarea<{ $height: textAreaHeight }>`
  ${({ theme, $height }) => TextAreaStyles.textarea(theme, $height)}
`;
