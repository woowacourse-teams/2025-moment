# 관리자 페이지 개발 가이드라인

## 기술 스택

- **프론트엔드**: Thymeleaf + Bootstrap 5.3 (CDN) + Bootstrap Icons
- **파일 경로**: `server/src/main/resources/templates/admin`

## UI 일관성 (Bootstrap 5)

시스템 전반에 걸쳐 통일된 사용자 경험을 제공하기 위해 다음 규칙을 준수합니다.

### 1. 레이아웃 & 구조
- **확장 (Inheritance)**: 모든 페이지는 반드시 `admin/layout.html`을 확장하여 사용해야 합니다. (예: `layout:decorate` 또는 `th:replace`)
- **컨테이너**: 주요 콘텐츠는 `.container-fluid` 내부에 배치하여 화면 너비를 효율적으로 사용합니다.
- **카드 스타일**: 데이터 입력 폼이나 목록 조회 영역은 `.card` 컴포넌트로 감쌉니다.
    - 페이지나 섹션의 제목은 반드시 `.card-header` 내부에 배치하여 계층 구조를 명확히 합니다.

### 2. 색상 및 버튼 정책
- **주요 액션 (Primary)**: 등록, 저장, 검색 등 긍정적/주요 작업은 `btn-primary` (Blue)를 사용합니다.
- **위험 액션 (Danger)**: 삭제, 차단, 거절 등 파괴적/부정적 작업은 `btn-danger` (Red)를 사용합니다.
- **보조 액션 (Secondary)**: 취소, 뒤로 가기 등은 `btn-secondary` 또는 `btn-outline-secondary`를 사용합니다.
- **버튼 크기**: 테이블 내부의 버튼은 `btn-sm`을 사용하여 공간을 절약합니다.

### 3. 테이블 (데이터 목록)
- **기본 스타일**: `.table .table-hover .table-bordered .align-middle` 클래스를 조합하여 가독성을 높입니다.
- **헤더 스타일**: `<thead>`에는 `table-light` 클래스를 적용하여 본문과 시각적으로 구분합니다.
- **빈 상태 (Empty State)**: 데이터가 없을 경우(`th:if="${#lists.isEmpty(list)}"`) 단순히 비워두지 말고, "표시할 데이터가 없습니다."라는 메시지를 중앙 정렬(`text-center`)하여 보여줍니다.

### 4. 타이포그래피 & 아이콘
- **폰트**: 시스템 폰트 또는 Noto Sans KR (가용 시)을 우선 사용합니다.
- **아이콘**: Bootstrap Icons를 적극 활용합니다. 버튼 내 아이콘은 텍스트 왼쪽에 배치하고 `me-1` 또는 `me-2` 클래스로 간격을 둡니다.

## Thymeleaf 코딩 컨벤션

- **폼 바인딩**: `th:object`와 `th:field`를 사용하여 서버 측 DTO와 입력 폼을 강력하게 결합합니다.
- **날짜 포맷**: 날짜 및 시간 표시는 `yyyy-MM-dd HH:mm` 형식을 따릅니다 (`#temporals.format` 활용).
- **프래그먼트 재사용**: 네비게이션 바, 사이드바, 푸터 등 공통 요소는 `th:replace`를 사용하여 중복을 제거합니다.
- **URL 처리**: 모든 링크(href, src, action)는 `@{...}` 구문을 사용하여 컨텍스트 경로(Context Path) 변경에 유연하게 대응합니다.

## 모던 디자인 스타일

세련되고 현대적인 UI/UX를 제공하기 위해 다음 디자인 원칙을 추가로 적용합니다.

### 1. 시각적 깊이 & 그림자
- **카드 그림자**: 모든 카드에 `shadow-sm` 클래스를 적용하여 부드러운 입체감을 표현합니다.
    ```html
    <div class="card shadow-sm">...</div>
    ```
- **호버 효과**: 클릭 가능한 카드는 호버 시 그림자를 강화하여 상호작용 피드백을 제공합니다.
    ```css
    .card-hover {
        transition: box-shadow 0.3s ease, transform 0.2s ease;
    }
    .card-hover:hover {
        box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15) !important;
        transform: translateY(-2px);
    }
    ```
- **모달**: 중요한 대화상자는 `shadow-lg` 클래스로 강조합니다.

### 2. 간격 시스템 (Spacing System)
Bootstrap의 Spacing 유틸리티를 활용하여 일관된 8px 단위 간격을 유지합니다.

- **페이지 레벨**:
    - 페이지 상단 여백: `py-4` (24px)
    - 섹션 간 간격: `mb-4` (24px)
- **카드 레벨**:
    - 카드 내부 패딩: `p-4` (24px)
    - 카드 간 간격: `mb-3` (16px)
- **폼 레벨**:
    - 필드 간 간격: `mb-3` (16px)
    - 폼 그룹 간 간격: `mb-4` (24px)

### 3. 둥근 모서리 (Border Radius)
- **기본 원칙**: 모든 카드, 입력 필드, 이미지는 부드러운 둥근 모서리를 사용합니다.
    - 카드: `rounded` (Bootstrap 기본값)
    - 이미지: `rounded` 또는 `rounded-circle` (프로필 이미지)
    - 커스텀 요소: `border-radius: 0.375rem` (6px)

### 4. 부드러운 전환 효과 (Transitions)
모든 인터랙티브 요소에 부드러운 애니메이션을 적용하여 자연스러운 UX를 제공합니다.

- **버튼 전환**:
    ```css
    .btn {
        transition: all 0.2s ease-in-out;
    }
    .btn:active {
        transform: scale(0.98);
    }
    ```
- **링크 호버**:
    ```css
    a {
        transition: color 0.2s ease;
    }
    ```
- **폼 포커스**:
    ```css
    .form-control:focus {
        transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
    }
    ```

### 5. 반응형 디자인
- **모바일 우선**: 작은 화면에서도 최적의 경험을 제공합니다.
    - 테이블: `table-responsive` 클래스로 가로 스크롤 지원
    - 그리드: `col-12 col-md-6 col-lg-4` 패턴으로 화면 크기별 레이아웃 조정
    - 버튼 그룹: `d-flex flex-column flex-md-row gap-2`로 모바일에서 수직 배치
- **간격 조정**: 화면 크기에 따라 여백을 조정합니다.
    ```html
    <div class="mb-3 mb-md-4">...</div>
    ```

### 6. 미니멀리즘 원칙
- **불필요한 요소 제거**: 핵심 정보와 기능에 집중하고 장식적 요소를 최소화합니다.
- **여백 활용**: 충분한 여백(white space)으로 콘텐츠 간 가독성을 높입니다.
- **시각적 노이즈 감소**: 과도한 테두리, 배경색, 구분선 사용을 지양합니다.

### 7. 인터랙션 피드백
사용자 행동에 즉각적인 시각적 피드백을 제공합니다.

- **로딩 상태**: 비동기 작업 중 스피너 표시
    ```html
    <button class="btn btn-primary" type="submit">
        <span class="spinner-border spinner-border-sm me-2" role="status"></span>
        처리 중...
    </button>
    ```
- **성공/오류 알림**: Toast 알림으로 작업 결과 전달
- **비활성화 상태**: `disabled` 속성과 시각적 스타일로 명확히 표시

### 8. 접근성 (Accessibility)
- **색상 대비**: WCAG 2.1 AA 기준 준수 (4.5:1 이상)
- **포커스 표시**: 키보드 네비게이션을 위한 명확한 포커스 링 유지
- **의미론적 HTML**: `<header>`, `<main>`, `<nav>`, `<section>` 등 시맨틱 태그 사용
- **ARIA 레이블**: 스크린 리더를 위한 적절한 레이블 제공

### 모던 디자인 체크리스트

새 페이지 작성 시 다음 항목을 확인하세요:

- [ ] 모든 카드에 `shadow-sm` 적용
- [ ] 일관된 간격 사용 (`mb-3`, `mb-4`, `p-4`)
- [ ] 호버 효과가 있는 인터랙티브 요소 구현
- [ ] 반응형 클래스 적용 (모바일/태블릿/데스크톱)
- [ ] 부드러운 전환 효과 (transition) 추가
- [ ] 불필요한 시각적 요소 제거
- [ ] 로딩/성공/오류 상태 피드백 구현
- [ ] 접근성 고려 (색상 대비, ARIA 레이블)
