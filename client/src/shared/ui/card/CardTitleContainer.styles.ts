import { CustomTheme } from '@/app/styles/theme';
import styled from '@emotion/styled';

export const CardTitleStyles = {
  cardTitle: (theme: CustomTheme) => `
    font-size: ${theme.typography.title.fontSize.small};
    font-weight: ${theme.typography.fontWeight.large};
    color: ${theme.colors.white};
    `,
};

export const CardSubtitleStyles = {
  cardSubtitle: (theme: CustomTheme) => `
    font-size: ${theme.typography.subTitle.fontSize.medium};
    color: ${theme.colors['gray-200']};
    `,
};

export const CardTitleWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
`;

export const CardTitleContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 5px;
`;

export const CardTitle = styled.span`
  width: 100%;
  ${({ theme }) => CardTitleStyles.cardTitle(theme)}
`;

export const CardSubtitle = styled.span`
  width: 100%;
  ${({ theme }) => CardSubtitleStyles.cardSubtitle(theme)}
`;
