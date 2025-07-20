import { CustomTheme } from '@/app/styles/theme';
import styled from '@emotion/styled';

export const CardSuccessTitleStyles = {
  cardSuccessTitle: (theme: CustomTheme) => `
    font-size: ${theme.typography.title.fontSize.small};
    font-weight: ${theme.typography.fontWeight.large};
    color: ${theme.colors.white};
    `,
};

export const CardSuccessSubtitleStyles = {
  cardSuccessSubtitle: (theme: CustomTheme) => `
    font-size: ${theme.typography.subTitle.fontSize.medium};
    color: ${theme.colors['gray-200']};
    white-space: pre-line;
    `,
};

export const CardSuccessContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
`;

export const CardSuccessTitle = styled.span`
  ${({ theme }) => CardSuccessTitleStyles.cardSuccessTitle(theme)}
`;

export const CardSuccessSubtitle = styled.span`
  ${({ theme }) => CardSuccessSubtitleStyles.cardSuccessSubtitle(theme)}
`;
