/**
 * Spacing Scale (4px 기반)
 *
 * 숫자 키: 4px 배수 (Tailwind/Material 표준)
 *   theme.spacing.scale[4]  → '16px'
 *   theme.spacing.scale[8]  → '32px'
 *
 * 별칭 키: 의미 기반 참조
 *   theme.spacing.scale.sm  → '8px'
 *   theme.spacing.scale.md  → '16px'
 *   theme.spacing.scale.lg  → '24px'
 */
const scale = {
  0: '0px',
  1: '4px',
  2: '8px',
  3: '12px',
  4: '16px',
  5: '20px',
  6: '24px',
  8: '32px',
  10: '40px',
  12: '48px',
  16: '64px',
  20: '80px',
  // 의미론적 별칭
  xs: '4px',
  sm: '8px',
  md: '16px',
  lg: '24px',
  xl: '32px',
  '2xl': '48px',
  '3xl': '64px',
} as const;

export const spacing = {
  /** 범용 4px 기반 간격 — padding, margin, gap에 사용 */
  scale,
} as const;
