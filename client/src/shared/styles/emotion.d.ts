import '@emotion/react';
import type { CustomTheme } from './theme';

declare module '@emotion/react' {
  export interface Theme extends CustomTheme {}
}
