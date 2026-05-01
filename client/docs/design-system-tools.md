# 디자인 시스템 분석 & Codemod 도구

이 프로젝트에는 세 가지 개발 도구가 포함되어 있다.

| 도구     | 명령어                               | 역할                               |
| -------- | ------------------------------------ | ---------------------------------- |
| 분석 CLI | `pnpm run analyze:design`            | 코드를 읽고 지표를 JSON으로 저장   |
| Codemod  | `pnpm run codemod` / `codemod:apply` | 하드코딩 색상을 토큰으로 자동 교체 |
| 대시보드 | `localhost/design-audit` (개발 서버) | 분석 결과를 차트로 시각화          |

---

## 왜 만들었나

디자인 시스템을 리팩토링하거나 2.0으로 마이그레이션할 때 가장 먼저 필요한 것은 **현재 상태 파악**이다.

"어디에 하드코딩이 많은가?", "토큰이 얼마나 쓰이고 있는가?", "어떤 컴포넌트가 실제로 사용되는가?"  
이 질문들에 감이 아닌 **데이터**로 답하기 위해 만들었다.

---

## 1. 분석 CLI (`pnpm run analyze:design`)

### 무엇을 하는가

`src/` 하위의 모든 `.ts` `.tsx` `.js` `.jsx` 파일을 읽고 다음 세 가지를 측정한다.

#### ① 공통 컴포넌트 사용량

`@/shared/design-system/*`, `@/shared/ui/*` 경로의 import 구문을 찾아 집계한다.

```tsx
// 이런 구문을 탐지
import { Button } from '@/shared/design-system/button';
import { Modal } from '@/shared/design-system/modal';
```

**출력**: 컴포넌트별 import 횟수 + 어떤 파일에서 쓰이는지 목록

#### ② 하드코딩 스타일 값 탐지

CSS로 사용된 하드코딩 값을 세 가지 패턴으로 탐지한다.

| 종류            | 예시                            |
| --------------- | ------------------------------- |
| hex 색상        | `#ffffff`, `#F1C40F`, `#fff`    |
| px 단위 값      | `16px`, `24px`, `1.5rem`은 제외 |
| Tailwind 임의값 | `p-[13px]`, `text-[#123456]`    |

탐지 대상 컨텍스트:

- `styled.div`, `css`, `keyframes` 등 **tagged template literal** 안의 CSS 문자열
- JSX의 `style={{ }}` 속성 안의 문자열 값
- JSX의 `css={{ }}` 속성 안의 문자열 값

```tsx
// 탐지됨
export const Card = styled.div`
  background: #1E293B;   ← 탐지
  padding: 16px;          ← 탐지
`;

<div style={{ color: '#fff' }} />  ← 탐지
```

#### ③ 디자인 토큰 사용률

`theme.colors`, `theme.semantic`, `theme.spacing`, `theme.typography`, `theme.breakpoints`, `theme.sizes` 접근 패턴을 찾아 집계한다.

```tsx
// 이런 접근 패턴을 탐지
theme.colors['yellow-500']     // theme.colors 카테고리 +1
theme.semantic.color.text.primary  // theme.semantic 카테고리 +1
({ theme }) => theme.spacing.scale.md  // theme.spacing 카테고리 +1
```

**토큰 채택률** = 토큰 사용 횟수 / (토큰 사용 횟수 + 하드코딩 개수) × 100

---

### 어떻게 동작하는가 — AST 분석

단순 정규식 검색이 아니라 **AST(Abstract Syntax Tree)** 분석을 사용한다.

```
소스 코드 (.tsx)
     ↓  @babel/parser (파싱)
   AST (트리 구조)
     ↓  @babel/traverse (탐색)
  노드 방문 (ImportDeclaration, TaggedTemplateExpression, MemberExpression ...)
     ↓
  지표 집계
```

AST를 쓰는 이유: 정규식은 `//주석 속의 #fff`나 `'url(#icon)'` 같은 CSS가 아닌 문자열도 잡아버린다. AST는 코드 구조를 이해하고 **CSS 컨텍스트 안에 있는 값만** 정확하게 추출한다.

---

### 파일 구조

```
scripts/analyze-design/
├── index.ts            # 진입점: 파일 수집 → 분석 → 리포트
├── types.ts            # 리포트 데이터 인터페이스
├── fileCollector.ts    # src/ 하위 파일 수집 (제외 패턴 적용)
├── componentAnalyzer.ts # ImportDeclaration AST 노드 분석
├── styleAnalyzer.ts    # TaggedTemplateExpression + JSX style 분석
├── tokenAnalyzer.ts    # MemberExpression 체인 분석
└── reporter.ts         # JSON 출력 + 콘솔 요약
```

---

### 출력

```
design-system-report/
├── report.json        # 전체 분석 결과
└── token-candidates.json  # (codemod 실행 후) 신규 토큰 후보
```

`report.json` 구조:

```json
{
  "generatedAt": "2026-04-29T10:00:00.000Z",
  "analyzedFiles": 318,

  "components": {
    "@/shared/design-system/button": {
      "importCount": 22,
      "usedInFiles": ["src/features/moment/ui/..."]
    }
  },

  "hardcodedStyles": {
    "total": 931,
    "byType": { "hexColor": 23, "pxValue": 908, "tailwindArbitrary": 0 },
    "byFile": {
      "src/pages/home/index.styles.ts": 83
    }
  },

  "tokenUsage": {
    "total": 459,
    "byCategory": {
      "colors": 390, "typography": 35, "breakpoints": 25, ...
    }
  },

  "adoptionRate": {
    "tokenCount": 459,
    "hardcodedCount": 931,
    "tokenAdoptionPercent": 33
  }
}
```

---

## 2. Codemod (`pnpm run codemod` / `codemod:apply`)

### 무엇을 하는가

분석에서 그치지 않고, **발견한 하드코딩 색상값을 디자인 토큰으로 자동 교체**한다.

```tsx
// Before
export const Card = styled.div`
  background: #1e293b;
  color: #ffffff;
`;

// After (codemod:apply 실행 후)
export const Card = styled.div`
  background: ${({ theme }) => theme.colors['slate-800']};
  color: ${({ theme }) => theme.colors.white};
`;
```

JSX inline style의 경우:

```tsx
// Before
<Heart color="#ef4444" />;

// After
import { theme } from '@/shared/styles/theme'; // 자동 추가
<Heart color={theme.colors['red-500']} />;
```

---

### 어떻게 동작하는가

#### Step 1. 역방향 토큰 맵 생성

`src/shared/styles/tokens/colors.ts`를 직접 읽어서 hex값 → 토큰 키 역방향 맵을 만든다.

```
colors.ts에서 추출:
  'slate-800': '#1E293B'  →  역방향 맵: '#1e293b' → 'slate-800'
  white: '#FFFFFF'        →  역방향 맵: '#ffffff' → 'white'
  ...
```

`color-mix()` 값은 hex가 아니므로 자동으로 제외된다.

#### Step 2. AST로 교체 위치 탐색

`@babel/traverse`로 파일을 순회하며 두 가지 컨텍스트에서 hex를 찾는다.

**컨텍스트 A — styled template literal**

```
TaggedTemplateExpression (styled.div, css, keyframes)
  └─ TemplateLiteral
       └─ quasis[*].value.raw  ← 여기서 hex 탐색
```

**컨텍스트 B — JSX style 속성**

```
JSXAttribute (name === 'style')
  └─ JSXExpressionContainer
       └─ ObjectExpression
            └─ ObjectProperty.value (StringLiteral)  ← 여기서 hex 탐색
```

#### Step 3. 소스 문자열 직접 교체

`@babel/generator`(전체 파일 재생성)를 사용하지 않고, **문자 위치(start, end)를 기록해 소스 문자열을 직접 수정**한다.

```
원본:  "background: #1E293B; padding: 16px;"
위치:             [12, 19]

교체 (뒤에서 앞으로 진행 → 위치 보정 불필요):
결과:  "background: ${({ theme }) => theme.colors['slate-800']}; padding: 16px;"
```

뒤에서 앞으로 진행하는 이유: 앞에서부터 교체하면 이후 문자들의 위치가 밀려서 좌표가 맞지 않게 된다.

#### Step 4. 토큰에 없는 색상 → 신규 토큰 후보 리포트

현재 토큰 팔레트에 없는 색상은 교체하지 않고 `token-candidates.json`으로 따로 기록한다. 이 목록이 **디자인 시스템 2.0의 신규 토큰 후보**가 된다.

```json
{
  "unmatchedColors": {
    "#0b0b0b": { "count": 1, "files": ["src/app/layout/ui/BottomNavbar.styles.ts"] },
    "#fbbf24": { "count": 1, "files": ["src/widgets/hero/Hero.styles.ts"] }
  }
}
```

---

### 파일 구조

```
scripts/codemod/
├── index.ts        # 진입점: 미리보기/적용 모드 분기, 결과 출력
├── tokenMap.ts     # colors.ts 파싱 → hex→토큰키 역방향 맵 생성
└── transformer.ts  # AST 탐색 + 교체 위치 계산 + 소스 수정
```

---

### 미리보기 vs 적용

```bash
pnpm run codemod          # 미리보기: 파일을 수정하지 않고 어떻게 바뀌는지 출력
pnpm run codemod:apply    # 적용: 실제로 파일을 수정
```

미리보기 출력 예시:

```
src/features/auth/ui/AppleLoginButton.styles.ts  (2개)
  L3  #000000 → ${({ theme }) => theme.colors.black}
  L3  #ffffff → ${({ theme }) => theme.colors.white}

src/shared/ui/errorBoundary/ErrorFallback.tsx  (1개)
  L11  '#ef4444' → theme.colors['red-500']
  + import { theme } from '@/shared/styles/theme' 추가 예정

신규 토큰 후보 (11개) — 토큰에 없는 색상값:
  #0b0b0b  (1회)  ← src/app/layout/ui/BottomNavbar.styles.ts
  #fbbf24  (1회)  ← src/widgets/hero/Hero.styles.ts
  ...
```

---

## 3. 대시보드 (`localhost/design-audit`)

### 무엇을 하는가

`report.json`의 숫자들을 차트로 시각화한다. **개발 환경에서만 접근 가능**하다.

| 차트                        | 내용                                 |
| --------------------------- | ------------------------------------ |
| Donut — 토큰 채택률         | 토큰 사용 vs 하드코딩 비율           |
| Donut — 하드코딩 유형       | hex / px / Tailwind 임의값 분포      |
| 수평 Bar — 컴포넌트 사용    | Button·Modal 등 사용 횟수 순위       |
| 수평 Bar — 토큰 카테고리    | theme.colors / semantic / spacing 등 |
| 수평 Bar — 집중 파일 TOP 10 | 하드코딩이 많은 파일                 |
| Bar — 피처 영역별 분포      | features / pages / shared 등         |

### 접근 조건

프로덕션 빌드에는 포함되지 않는다. `webpack.prod.js`에서 `NODE_ENV=production`이 설정되면 webpack이 해당 라우트와 `recharts` 청크를 번들에서 제거한다.

```
개발 서버: localhost:3000/design-audit  → 대시보드 표시
프로덕션:  /design-audit               → 404 (라우트 자체가 없음)
```

### 사용 방법

```bash
# 1. 분석 실행 (report.json 생성 + public/에도 복사)
pnpm run analyze:design

# 2. 개발 서버 실행
pnpm dev

# 3. 브라우저 접속
http://localhost:3000/design-audit
```

`report.json`이 없으면 `pnpm run analyze:design`을 먼저 실행하라는 안내 화면이 뜬다.

---

## 전체 흐름 요약

```
pnpm run analyze:design
        │
        ├─→ design-system-report/report.json   (분석 결과 전체)
        └─→ public/design-system-report/report.json  (대시보드용 복사본)

pnpm run codemod          (미리보기)
pnpm run codemod:apply    (적용)
        │
        ├─→ .styles.ts 파일들 수정 (하드코딩 → 토큰 표현식)
        └─→ design-system-report/token-candidates.json  (신규 토큰 후보)

개발 서버 + /design-audit
        │
        └─→ report.json 읽어서 Recharts 차트로 시각화
```

---

## 기술 스택

| 역할            | 사용 기술                                   |
| --------------- | ------------------------------------------- |
| TypeScript 실행 | `tsx` (별도 컴파일 없이 직접 실행)          |
| AST 파싱        | `@babel/parser` (TypeScript + JSX 플러그인) |
| AST 탐색        | `@babel/traverse`                           |
| 시각화          | `recharts` (개발 환경 전용)                 |
| 스타일          | Emotion (`@emotion/styled`)                 |

`@babel/generator`를 사용하지 않는 이유: 전체 파일을 AST에서 재생성하면 들여쓰기·따옴표 스타일 등 불필요한 포맷 변경이 발생한다. 대신 AST로 교체 위치(character offset)만 계산하고 소스 문자열을 직접 수정해 **최소한의 변경만 일어나도록** 설계했다.
