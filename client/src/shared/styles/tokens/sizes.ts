/**
 * Component Size Tokens
 *
 * 컴포넌트 고유 크기(width/height)를 정의합니다.
 * 간격(padding, margin, gap)은 spacing.ts를 사용하세요.
 *
 * 사용법:
 *   theme.sizes.textAreaHeight.medium  → '200px'
 *   theme.sizes.cardWidth.large        → '90%'
 *   theme.sizes.modalWidth.small.desktop → '30%'
 */
export const sizes = {
  textAreaHeight: {
    small: '100px',
    medium: '200px',
    large: '300px',
  },

  cardWidth: {
    small: '30%',
    medium: '60%',
    large: '90%',
    full: '100%',
  },

  modalWidth: {
    small: {
      desktop: '30%',
      tablet: '50%',
      mobile: '70%',
    },
    medium: {
      desktop: '60%',
      tablet: '70%',
      mobile: '90%',
    },
    large: {
      desktop: '90%',
      tablet: '95%',
      mobile: '100%',
    },
  },
} as const;

export type Sizes = typeof sizes;
