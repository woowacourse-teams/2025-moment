import { CustomTheme } from '@/app/styles/theme';
import styled from '@emotion/styled';

export const CardTitleStyles = {
  cardTitle: (theme: CustomTheme) => `
    font-size: ${theme.typography.title.fontSize.large};
    font-weight: ${theme.typography.title.fontWeight.large};
    color: ${theme.colors.white};
    `,
};

export const CardSubtitleStyles = {
  cardSubtitle: (theme: CustomTheme) => `
    font-size: ${theme.typography.title.fontSize.medium};
    font-weight: ${theme.typography.title.fontWeight.medium};
    color: ${theme.colors.gray200};
    `,
};

export const CardTitleWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
`;

export const CardTitle = styled.span`
  ${({ theme }) => CardTitleStyles.cardTitle(theme)}
`;

export const CardSubtitle = styled.span`
  ${({ theme }) => CardSubtitleStyles.cardSubtitle(theme)}
`;
