/**
 * Design System 공통 타입
 *
 * 각 컴포넌트에서 중복 정의하던 타입을 한 곳에서 관리합니다.
 */
import type { CustomTheme } from '@/shared/styles/theme';

/** styled component에서 theme 기반 CSS 문자열을 반환하는 함수 타입 */
export type StyleFn = (theme: CustomTheme) => string;

/** theme.colors의 키 타입 */
export type ColorKey = keyof CustomTheme['colors'];

