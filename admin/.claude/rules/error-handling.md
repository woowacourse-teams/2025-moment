---
paths:
  - "src/**/*.{ts,tsx}"
---

# 에러 처리 규칙

## API 에러

`shared/api/client.ts`의 전역 인터셉터에서 처리:
- **401**: `/login`으로 리다이렉트
- **403**: 권한 없음 메시지 표시
- **4xx/5xx**: 에러 형식 정규화

## 데이터 fetching 컴포넌트 상태

데이터를 불러오는 컴포넌트는 반드시 아래 4가지 상태를 모두 처리합니다:

```tsx
if (isLoading) return <Loading />;
if (isError) return <Error onRetry={refetch} />;
if (!data?.length) return <Empty />;
return <Content data={data} />;
```

## 뮤테이션 에러

- 에러 메시지를 toast/alert로 표시
- 에러가 발생해도 모달을 닫지 않음
- 디버깅을 위해 에러 상세 내용 로깅

## Error Boundary

- 모든 페이지를 ErrorBoundary로 감쌀 것
- 재시도 버튼이 있는 fallback UI 제공
