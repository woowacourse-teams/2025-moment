import { colors } from './tokens/colors';
import { semantic } from './tokens/semantic';
import { typography } from './tokens/typography';
import { spacing } from './tokens/spacing';
import { sizes } from './tokens/sizes';
import { breakpoints } from './tokens/breakpoints';

export const theme = {
  /** Scale 토큰: 원시 색상 팔레트. 직접 참조보다 semantic 사용을 권장 */
  colors,
  /** Semantic 토큰: 색상의 역할(의미) 기반 참조 — 이걸 우선 사용하세요 */
  semantic,
  typography,
  /** 범용 4px 기반 간격 — padding, margin, gap에 사용 */
  spacing,
  /** 컴포넌트 고유 크기 — width/height에 사용 */
  sizes,
  breakpoints,
} as const;

export type CustomTheme = typeof theme;
