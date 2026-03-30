/**
 * Semantic Token Layer
 *
 * 색상의 "의미(역할)"을 정의합니다.
 * 직접 색상값이 아닌, colors.ts의 Scale 토큰을 참조합니다.
 *
 * 사용법:
 *   theme.semantic.color.text.primary     ← 주요 텍스트
 *   theme.semantic.color.background.surface ← 카드/모달 배경
 *   theme.semantic.color.brand.primary    ← 브랜드 색상
 *
 * colors.ts (scale)를 직접 쓰는 대신 이 레이어를 먼저 고려하세요.
 */
import { colors } from './colors';

export const semantic = {
  color: {
    /**
     * 텍스트 색상
     * - primary: 어두운 배경 위 주요 텍스트
     * - secondary: 보조 정보, 날짜/시간 등
     * - tertiary: 플레이스홀더, 비활성 힌트
     * - muted: 매우 흐린 부가 정보
     * - brand: 브랜드 컬러 텍스트
     * - onBrand: 브랜드(yellow) 배경 위 텍스트
     * - danger: 에러, 경고 텍스트
     */
    text: {
      primary: colors.white,
      secondary: colors['gray-200'],
      tertiary: colors['gray-400'],
      muted: colors['gray-600'],
      brand: colors['yellow-500'],
      onBrand: colors.black,
      danger: colors['red-500'],
    },

    /**
     * 배경/표면 색상
     * - page: 최하단 페이지 배경 (가장 어두움)
     * - surface: 카드, 모달 등 표면 레이어
     * - surfaceOverlay: 반투명 카드 표면
     * - input: 입력 필드 배경
     * - overlay: 모달 딤 처리 오버레이
     */
    background: {
      page: colors['slate-900'],
      surface: colors['slate-800'],
      surfaceOverlay: colors['slate-800_60'],
      input: colors['gray-600_20'],
      overlay: colors.black_70,
    },

    /**
     * 테두리 색상
     * - default: 일반 테두리 (카드, 입력 필드)
     * - subtle: 미묘한 테두리 (ghost 버튼 외곽선)
     * - brand: 브랜드 포인트 테두리
     * - danger: 에러 상태 테두리
     */
    border: {
      default: colors['gray-700'],
      subtle: colors['slate-700'],
      brand: colors['yellow-500'],
      danger: colors['red-500'],
    },

    /**
     * 브랜드 색상
     * - primary: 메인 브랜드 색 (버튼, 아이콘 포인트)
     * - light: 밝은 브랜드 변형 (hover, quinary 버튼)
     * - glow: 빛나는 이펙트용 (카드 shadow, 펄스 애니메이션)
     * - glowSubtle: 은은한 글로우 (배지, 미묘한 강조)
     */
    brand: {
      primary: colors['yellow-500'],
      light: colors['yellow-300'],
      glow: colors['yellow-300_80'],
      glowSubtle: colors['yellow-300_30'],
    },

    /**
     * 상태 색상
     * - success: 성공, 완료
     * - warning: 경고, 주의
     * - danger: 에러, 위험
     */
    status: {
      success: colors['emerald-500'],
      warning: colors['orange-500'],
      danger: colors['red-500'],
    },
  },
} as const;

export type Semantic = typeof semantic;
