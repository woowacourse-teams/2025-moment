import { ErrorFallbackProps } from '@/shared/types/errorBoundary';
import * as S from './ErrorBoundary.styles';

export const ErrorFallback = ({ error, resetError }: ErrorFallbackProps) => {
  return (
    <S.ErrorContainer>
      <S.ErrorTitle>문제가 발생했습니다</S.ErrorTitle>
      <S.ErrorMessage>일시적인 문제가 발생했습니다. 다시 시도해 주세요.</S.ErrorMessage>

      {process.env.NODE_ENV === 'development' && (
        <details style={{ marginTop: '16px', color: '#ef4444' }}>
          <summary>에러 상세 정보 (개발용)</summary>
          <pre style={{ fontSize: '12px', marginTop: '8px' }}>
            {error.message}
            {error.stack}
          </pre>
        </details>
      )}

      <S.ErrorButton onClick={resetError}>다시 시도</S.ErrorButton>
    </S.ErrorContainer>
  );
};
