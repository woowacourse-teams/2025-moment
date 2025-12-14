import { CustomTheme } from '@/app/styles/theme';
import styled from '@emotion/styled';

export const TitleStyles = {
  title: (theme: CustomTheme) => `
    font-size: ${theme.typography.fontSize.title.large};
    font-weight: ${theme.typography.fontWeight.large};
    color: ${theme.colors.white};
    margin: 0;
  `,
};

export const SubtitleStyles = {
  subtitle: (theme: CustomTheme) => `
      font-size: ${theme.typography.fontSize.subTitle.medium};
      color: ${theme.colors.white};
      margin: 0;
    `,
};

export const TitleContainer = styled.header`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
`;

export const Title = styled.h1`
  ${({ theme }) => TitleStyles.title(theme)}
`;

export const Subtitle = styled.p`
  ${({ theme }) => SubtitleStyles.subtitle(theme)}
`;
