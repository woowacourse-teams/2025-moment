export const theme = {
  colors: {
    primary: '#0F172A',
    secondary: '#F1C40F',
    tertiary: '#1E293B',
    fourth: '#334155',
  },

  background: {
    primary: '#0a0a0f',
    secondary: '#0d162b',
  },

  text: {
    primary: '#FFFFFF',
    secondary: '#F1C40F',
    tertiary: '#F4D03F',
  },

  success: {
    default: '#10B981',
    hover: '#059669',
    background: '#ECFDF5',
    border: '#A7F3D0',
  },

  warning: {
    default: '#F59E0B',
  },

  danger: {
    default: '#EF4444',
  },

  card: {
    background: '#1E293B',
    border: '#334155',
  },

  border: {
    primary: '#334155',
  },
} as const;

export type CustomTheme = typeof theme;
