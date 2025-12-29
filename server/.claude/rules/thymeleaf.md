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
