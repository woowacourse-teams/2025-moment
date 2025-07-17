export const theme = {

  colors: {
    slate900:  '#0F172A', // theme.colors.primary
    slate800:  '#1E293B', // theme.colors.tertiary, card.background
    slate700:  '#334155', // theme.colors.fourth, card.border, border.primary
    white:    '#FFFFFF', // theme.text.primary
    yellow500:'#F1C40F', // theme.text.secondary
    yellow300:'#F4D03F', // theme.text.tertiary
    emerald500: '#10B981', // theme.success.default
    emerald600: '#059669', // theme.success.hover
    emerald50:  '#ECFDF5', // theme.success.background
    emerald200: '#A7F3D0', // theme.success.border
    amber500: '#F59E0B', // theme.warning.default
    red500:   '#EF4444', 
    navy900:   '#0a0a0f',
    indigo950: '#0d162b',
    gray200: '#CBD5E1',
    gray600: '#536872',
    gray700: '#334155'
    },
    typography: {
      title:{
        fontSize:{
          small:'16px',
          medium:'18px',
          large:'32px',
        },
        fontWeight:{
          small:'400',
          medium:'500',
          large:'600',
        },
      },
      textAreaHeight:{
        small: "100px",
        medium: "200px",
        large: "300px",
      },
      cardWidth:{
        small: "30%",
        medium: "50%",
        large: "100%",
      }
    }
} as const;


export type CustomTheme = typeof theme;
