export const theme = {
  colors: {
    slate900: '#0F172A',
    slate800: '#1E293B',
    slate700: '#334155',
    white: '#FFFFFF',
    yellow500: '#F1C40F',
    yellow300: '#F4D03F',
    emerald500: '#10B981',
    emerald600: '#059669',
    emerald50: '#ECFDF5',
    emerald200: '#A7F3D0',
    amber500: '#F59E0B',
    red500: '#EF4444',
    navy900: '#0a0a0f',
    indigo950: '#0d162b',
    gray200: '#CBD5E1',
    gray600: '#536872',
    gray700: '#334155',
  },
  typography: {
    title: {
      fontSize: {
        small: '16px',
        medium: '18px',
        large: '32px',
      },
      fontWeight: {
        small: '400',
        medium: '500',
        large: '600',
      },
    },
    textAreaHeight: {
      small: '100px',
      medium: '200px',
      large: '300px',
    },
    cardWidth: {
      small: '30%',
      medium: '50%',
      large: '100%',
    },
  },
} as const;

export type CustomTheme = typeof theme;
