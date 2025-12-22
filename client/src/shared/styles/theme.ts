import { colors } from './tokens/colors';
import { typography } from './tokens/typography';
import { spacing } from './tokens/spacing';
import { breakpoints } from './tokens/breakpoints';

export const theme = {
  colors,
  typography,
  spacing,
  breakpoints,
} as const;

export type CustomTheme = typeof theme;
