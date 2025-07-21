export const theme = {
  colors: {
    'slate-900_60': 'color-mix(in srgb, #2B3546 60%, transparent)', // rgba(43, 53, 70, 0.6)와 동일
    'slate-900': '#0F172A',
    'slate-800_60': 'color-mix(in srgb, #1E293B 60%, transparent)',
    'slate-800': '#1E293B',
    'slate-700': '#334155',
    white: '#FFFFFF',
    'yellow-500': '#F1C40F',
    'yellow-300': '#F4D03F',
    'emerald-500': '#10B981',
    'emerald-600': '#059669',
    'emerald-50': '#ECFDF5',
    'emerald-200': '#A7F3D0',
    'amber-500': '#F59E0B',
    'red-500': '#EF4444',
    'navy-900': '#0a0a0f',
    'indigo-950': '#0d162b',
    'gray-200': '#CBD5E1',
    'gray-400': '#93A1B7',
    'gray-600': '#536872',
    'gray-600_20': 'color-mix(in srgb, #536872 20%, transparent)',
    'gray-700': '#334155',
    'blue-600': '#497CBC',
  },
  typography: {
    title: {
      fontSize: {
        small: '32px',
        medium: '40px',
        large: '54px',
      },
    },

    subTitle: {
      fontSize: {
        small: '12px',
        medium: '18px',
        large: '24px',
      },
    },

    fontWeight: {
      small: '400',
      medium: '500',
      large: '600',
    },

    textAreaHeight: {
      small: '100px',
      medium: '200px',
      large: '300px',
    },

    cardWidth: {
      small: '30%',
      medium: '60%',
      large: '80%',
    },
  },
} as const;

export type CustomTheme = typeof theme;
